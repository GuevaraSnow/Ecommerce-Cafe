import { CurrencyPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Presentacion } from '../../models/catalogo';
import { AuthService } from '../../services/auth';
import { NotificacionesService } from '../../services/notificaciones';
import { PresentacionesService } from '../../services/presentaciones';

@Component({
  imports: [ReactiveFormsModule, CurrencyPipe],
  selector: 'app-descuentos',
  templateUrl: './descuentos.html',
})
export class Descuentos {
  private presentacionesServicio = inject(PresentacionesService);
  private authService = inject(AuthService);
  private avisos = inject(NotificacionesService);
  private destroyRef = inject(DestroyRef);

  // Los nombres de los campos coinciden con ProgramarDescuentoRequest del backend
  descuentoForm = inject(FormBuilder).nonNullable.group({
    porcentaje: [10, [Validators.required, Validators.min(1), Validators.max(50)]],
    fechaInicio: ['', [Validators.required]],
    fechaFin: ['', [Validators.required]],
  });

  // Señales de pantalla
  presentaciones = signal<Presentacion[]>([]);
  cargando = signal(true);
  seleccionada = signal<Presentacion | null>(null);
  isLoading = signal(false);

  // Igual que en login y compra: el estado del formulario como señal y un computed para el botón
  formStatus = toSignal(this.descuentoForm.statusChanges, { initialValue: 'INVALID' });
  canSubmit = computed(
    () => this.formStatus() === 'VALID' && !this.isLoading() && this.seleccionada() !== null,
  );

  constructor() {
    // Solo las presentaciones del vendedor que inició sesión
    this.presentacionesServicio
      .buscar({ vendedorId: this.authService.getUsuarioId(), tamano: 50 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.presentaciones.set(pagina.contenido);
          this.cargando.set(false);
        },
        error: () => {
          this.avisos.error('No se pudieron cargar tus presentaciones.');
          this.cargando.set(false);
        },
      });
  }

  // Al elegir una presentación, el formulario se llena con su descuento actual (si tiene)
  seleccionar(presentacion: Presentacion): void {
    this.seleccionada.set(presentacion);
    if (presentacion.descuento) {
      this.descuentoForm.patchValue(presentacion.descuento);
    } else {
      this.descuentoForm.reset({ porcentaje: 10, fechaInicio: '', fechaFin: '' });
    }
  }

  programar(): void {
    const presentacion = this.seleccionada();
    if (!presentacion || !this.canSubmit()) return;

    this.isLoading.set(true);
    this.presentacionesServicio
      .programarDescuento(presentacion.id, this.descuentoForm.getRawValue())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizada) => {
          this.reemplazar(actualizada);
          this.avisos.exito(`Descuento programado en "${actualizada.titulo}".`);
          this.isLoading.set(false);
        },
        error: (respuesta: HttpErrorResponse) => this.mostrarError(respuesta),
      });
  }

  quitar(): void {
    const presentacion = this.seleccionada();
    if (!presentacion || this.isLoading()) return;

    this.isLoading.set(true);
    this.presentacionesServicio
      .quitarDescuento(presentacion.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizada) => {
          this.reemplazar(actualizada);
          this.descuentoForm.reset({ porcentaje: 10, fechaInicio: '', fechaFin: '' });
          this.avisos.exito(`Descuento quitado de "${actualizada.titulo}".`);
          this.isLoading.set(false);
        },
        error: (respuesta: HttpErrorResponse) => this.mostrarError(respuesta),
      });
  }

  // Deja la lista y la selección con la presentación que devolvió el backend
  private reemplazar(actualizada: Presentacion): void {
    this.presentaciones.update((lista) => lista.map((p) => (p.id === actualizada.id ? actualizada : p)));
    this.seleccionada.set(actualizada);
  }

  // 400 y 422 traen el mensaje del backend; el 401 (sesión vencida) ya lo atiende el interceptor
  private mostrarError(respuesta: HttpErrorResponse): void {
    if (respuesta.status === 403) {
      this.avisos.error('Solo el dueño de la presentación puede cambiar su descuento.');
    } else if (respuesta.status !== 401) {
      this.avisos.error(respuesta.error?.mensaje || 'No se pudo guardar el descuento.');
    }
    this.isLoading.set(false);
  }
}
