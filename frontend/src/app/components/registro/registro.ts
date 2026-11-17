import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Rol } from '../../models/usuario';
import { AuthService } from '../../services/auth';
import { NotificacionesService } from '../../services/notificaciones';

// Reglas del backend: contraseña de 8 o más con una mayúscula y un número; teléfono de 10 dígitos; mayor de edad
const CONTRASENA = /^(?=.*[A-Z])(?=.*[0-9]).{8,}$/;

function mayorDeEdad(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const limite = new Date();
  limite.setFullYear(limite.getFullYear() - 18);
  return new Date(control.value) <= limite ? null : { menorDeEdad: true };
}

function contrasenasIguales(grupo: AbstractControl): ValidationErrors | null {
  const confirmar = grupo.get('confirmar');
  const coinciden = grupo.get('contrasena')?.value === confirmar?.value;
  // El error va en el campo de confirmación, para mostrarlo junto a él
  if (confirmar) {
    const { noCoincide, ...otros } = confirmar.errors ?? {};
    confirmar.setErrors(coinciden ? (Object.keys(otros).length ? otros : null) : { ...otros, noCoincide: true });
  }
  return null;
}

@Component({
  imports: [ReactiveFormsModule, RouterLink],
  selector: 'app-registro',
  styleUrl: './registro.css',
  templateUrl: './registro.html',
})
export class Registro {
  private authService = inject(AuthService);
  private router = inject(Router);
  private avisos = inject(NotificacionesService);
  private destroyRef = inject(DestroyRef);

  readonly vendedores: { valor: Rol; etiqueta: string }[] = [
    { valor: 'CAFICULTOR', etiqueta: 'Caficultor' },
    { valor: 'TOSTADOR', etiqueta: 'Tostador' },
    { valor: 'VENDEDOR_DERIVADOS', etiqueta: 'Vendedor de derivados' },
  ];

  // Los nombres de nombre, email, contrasena, telefono y fechaNacimiento coinciden con el backend
  registroForm = inject(FormBuilder).nonNullable.group(
    {
      nombre: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required, Validators.pattern(CONTRASENA)]],
      confirmar: ['', [Validators.required]],
      telefono: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      fechaNacimiento: ['', [Validators.required, mayorDeEdad]],
      // Quien se registra elige comprar o vender; si vende, también qué vende
      tipoCuenta: ['COMPRADOR' as 'COMPRADOR' | 'VENDEDOR'],
      rolVendedor: ['CAFICULTOR' as Rol],
    },
    { validators: contrasenasIguales },
  );

  isLoading = signal(false);
  verContrasena = signal(false);

  formStatus = toSignal(this.registroForm.statusChanges, { initialValue: 'INVALID' as const });
  canSubmit = computed(() => this.formStatus() === 'VALID' && !this.isLoading());

  // Cuántos caracteres de contraseña faltan o qué regla incumple, para mostrarlo debajo del campo
  tipoCuenta = toSignal(this.registroForm.controls.tipoCuenta.valueChanges, { initialValue: 'COMPRADOR' as const });

  elegirTipo(tipo: 'COMPRADOR' | 'VENDEDOR'): void {
    this.registroForm.controls.tipoCuenta.setValue(tipo);
  }

  alternarContrasena(): void {
    this.verContrasena.update((visible) => !visible);
  }

  onSubmit(): void {
    if (!this.canSubmit()) return;

    this.isLoading.set(true);
    const { nombre, email, contrasena, telefono, fechaNacimiento, tipoCuenta, rolVendedor } =
      this.registroForm.getRawValue();
    const rol: Rol = tipoCuenta === 'COMPRADOR' ? 'COMPRADOR' : rolVendedor;

    this.authService
      .registrar({ nombre: nombre.trim(), email: email.trim(), contrasena, telefono, fechaNacimiento, rol })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.avisos.exito('Cuenta creada. Ya puedes iniciar sesión.');
          this.router.navigate(['/login']);
        },
        error: (respuesta: HttpErrorResponse) => {
          // 400 y 422 traen el mensaje del backend (por ejemplo, correo ya registrado)
          this.avisos.error(respuesta.error?.mensaje || 'No se pudo crear la cuenta.');
          this.isLoading.set(false);
        },
      });
  }
}
