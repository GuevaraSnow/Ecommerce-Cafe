import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Presentacion } from '../../models/catalogo';
import { Compra } from '../../models/compra';
import { ComprasService } from '../../services/compras';
import { NotificacionesService } from '../../services/notificaciones';
import { PresentacionesService } from '../../services/presentaciones';

@Component({
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe],
  selector: 'app-comprar',
  styleUrl: './comprar.css',
  templateUrl: './comprar.html',
})
export class Comprar {
  private comprasServicio = inject(ComprasService);
  private presentacionesServicio = inject(PresentacionesService);
  private avisos = inject(NotificacionesService);
  private destroyRef = inject(DestroyRef);
  private presentacionId = inject(ActivatedRoute).snapshot.paramMap.get('id') ?? '';

  // Los nombres de los campos coinciden con DireccionDeEnvioRequest y CantidadRequest del backend.
  compraForm = inject(FormBuilder).nonNullable.group({
    cantidad: [1, [Validators.required, Validators.min(0.01)]],
    destinatario: ['', [Validators.required, Validators.maxLength(100)]],
    ciudad: ['', [Validators.required, Validators.maxLength(80)]],
    direccion: ['', [Validators.required, Validators.maxLength(150)]],
  });

  // Señales de pantalla
  presentacion = signal<Presentacion | null>(null);
  cargando = signal(true);
  isLoading = signal(false);
  compra = signal<Compra | null>(null);

  // Igual que en el login: el estado del formulario como señal y un computed para el botón
  formStatus = toSignal(this.compraForm.statusChanges, { initialValue: 'INVALID' });
  canSubmit = computed(
    () => this.formStatus() === 'VALID' && !this.isLoading() && this.presentacion() !== null,
  );

  // Total de la compra confirmada: precio congelado por cantidad de cada línea
  total = computed(() =>
    (this.compra()?.detalles ?? []).reduce((suma, d) => suma + d.precioCongelado.monto * d.cantidad.valor, 0),
  );

  constructor() {
    this.presentacionesServicio
      .consultar(this.presentacionId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (presentacion) => {
          this.presentacion.set(presentacion);
          this.cargando.set(false);
        },
        error: () => {
          this.avisos.error('No se encontró la presentación que quieres comprar.');
          this.cargando.set(false);
        },
      });
  }

  onSubmit(): void {
    const presentacion = this.presentacion();
    if (!presentacion || !this.canSubmit()) return;

    this.isLoading.set(true);
    const { cantidad, destinatario, ciudad, direccion } = this.compraForm.getRawValue();

    this.comprasServicio
      .comprar(
        presentacion.id,
        { valor: Number(cantidad), unidad: presentacion.cantidadDisponible.unidad },
        { destinatario, ciudad, direccion },
      )
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (compra) => {
          this.compra.set(compra);
          this.avisos.exito(`Compra registrada. Número ${compra.id}, estado ${compra.estado}.`);
          this.isLoading.set(false);
        },
        error: (respuesta: HttpErrorResponse) => {
          // El 401 (sesión vencida) ya lo atiende el interceptor con su propio aviso
          if (respuesta.status === 403) {
            this.avisos.error('Tu rol no puede comprar. Inicia sesión como comprador.');
          } else if (respuesta.status !== 401) {
            this.avisos.error(respuesta.error?.mensaje || 'No se pudo completar la compra.');
          }
          this.isLoading.set(false);
        },
      });
  }
}
