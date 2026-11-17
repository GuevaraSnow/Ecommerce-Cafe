import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { API_URL } from '../../api';
import { Registro } from './registro';

describe('Registro', () => {
  let componente: Registro;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([{ path: 'login', component: Registro }])],
    });
    componente = TestBed.createComponent(Registro).componentInstance;
    http = TestBed.inject(HttpTestingController);
  });

  const llenar = (cambios: Record<string, string> = {}) =>
    componente.registroForm.patchValue({
      nombre: 'Ana Pérez',
      email: 'ana@correo.com',
      contrasena: 'Cafe2026*',
      confirmar: 'Cafe2026*',
      telefono: '3001234567',
      fechaNacimiento: '2000-01-15',
      ...cambios,
    });

  it('rechaza una contraseña sin mayúscula o sin número', () => {
    llenar({ contrasena: 'cafe2026*', confirmar: 'cafe2026*' });
    expect(componente.registroForm.controls.contrasena.invalid).toBe(true);

    llenar({ contrasena: 'CafeSinNumero', confirmar: 'CafeSinNumero' });
    expect(componente.registroForm.controls.contrasena.invalid).toBe(true);
  });

  it('marca cuando las contraseñas no coinciden', () => {
    llenar({ confirmar: 'Otra2026*' });

    expect(componente.registroForm.controls.confirmar.hasError('noCoincide')).toBe(true);
  });

  it('rechaza a un menor de edad y un teléfono que no tiene 10 dígitos', () => {
    llenar({ fechaNacimiento: new Date().toISOString().slice(0, 10), telefono: '123' });

    expect(componente.registroForm.controls.fechaNacimiento.hasError('menorDeEdad')).toBe(true);
    expect(componente.registroForm.controls.telefono.invalid).toBe(true);
  });

  it('un comprador se registra con el rol COMPRADOR y sin el campo de confirmación', () => {
    llenar();

    componente.onSubmit();

    const peticion = http.expectOne(`${API_URL}/usuarios/registro`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body.rol).toBe('COMPRADOR');
    expect(peticion.request.body.confirmar).toBeUndefined();
    peticion.flush({});
  });

  it('un vendedor se registra con el rol que eligió', () => {
    llenar();
    componente.elegirTipo('VENDEDOR');
    componente.registroForm.controls.rolVendedor.setValue('TOSTADOR');

    componente.onSubmit();

    const peticion = http.expectOne(`${API_URL}/usuarios/registro`);
    expect(peticion.request.body.rol).toBe('TOSTADOR');
    peticion.flush({});
  });
});
