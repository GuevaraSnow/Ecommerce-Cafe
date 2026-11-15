import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, ElementRef, computed, effect, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MensajeChat } from '../../models/asistente';
import { AsistenteService } from '../../services/asistente';
import { NotificacionesService } from '../../services/notificaciones';

@Component({
  imports: [ReactiveFormsModule],
  selector: 'app-asistente',
  templateUrl: './asistente.html',
})
export class Asistente {
  private asistenteServicio = inject(AsistenteService);
  private avisos = inject(NotificacionesService);
  private destroyRef = inject(DestroyRef);

  // El tope de 500 caracteres es el mismo que valida el backend
  chatForm = inject(FormBuilder).nonNullable.group({
    mensaje: ['', [Validators.required, Validators.maxLength(500)]],
  });

  // Señales de pantalla
  abierto = signal(false);
  mensajes = signal<MensajeChat[]>([]);
  isLoading = signal(false);

  // Igual que en login y compra: el estado del formulario como señal y un computed para el botón
  formStatus = toSignal(this.chatForm.statusChanges, { initialValue: 'INVALID' });
  canSubmit = computed(() => this.formStatus() === 'VALID' && !this.isLoading());

  // Zona con los mensajes, para dejarla siempre mostrando el último
  private zonaMensajes = viewChild<ElementRef<HTMLElement>>('zonaMensajes');

  constructor() {
    effect(() => {
      this.mensajes();
      this.isLoading();
      const zona = this.zonaMensajes()?.nativeElement;
      // Espera a que Angular pinte el mensaje nuevo antes de bajar
      if (zona) setTimeout(() => (zona.scrollTop = zona.scrollHeight));
    });
  }

  alternar(): void {
    this.abierto.update((valor) => !valor);
  }

  enviar(): void {
    if (!this.canSubmit()) return;

    const texto = this.chatForm.controls.mensaje.value.trim();
    if (!texto) return;

    // El historial es lo anterior: el mensaje nuevo viaja aparte
    const historial = this.mensajes();
    this.mensajes.update((lista) => [...lista, { rol: 'usuario', texto }]);
    this.chatForm.reset();
    this.isLoading.set(true);

    this.asistenteServicio
      .preguntar(texto, historial)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (respuesta) => {
          this.mensajes.update((lista) => [...lista, { rol: 'asistente', texto: respuesta.respuesta }]);
          this.isLoading.set(false);
        },
        error: (respuesta: HttpErrorResponse) => {
          // 422 trae el mensaje del backend ("no está disponible"); el 401 lo atiende el interceptor
          if (respuesta.status !== 401) {
            this.avisos.error(respuesta.error?.mensaje || 'No se pudo consultar al asesor.');
          }
          this.isLoading.set(false);
        },
      });
  }
}
