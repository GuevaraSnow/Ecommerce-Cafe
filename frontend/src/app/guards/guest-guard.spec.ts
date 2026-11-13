import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { guestGuard } from './guest-guard';
import { AuthService } from '../services/auth';

describe('guestGuard', () => {
  const ejecutar = () =>
    TestBed.runInInjectionContext(() =>
      guestGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  const conSesion = (activa: boolean) =>
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: { isAuthenticated: signal(activa) } }],
    });

  it('deja ver el login a quien no tiene sesión', () => {
    conSesion(false);

    expect(ejecutar()).toBe(true);
  });

  it('con sesión manda al inicio', () => {
    conSesion(true);

    const resultado = ejecutar() as UrlTree;

    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/');
  });
});
