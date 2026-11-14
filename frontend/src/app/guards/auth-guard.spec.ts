import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { authGuard } from './auth-guard';
import { AuthService } from '../services/auth';

describe('authGuard', () => {
  const ejecutar = () =>
    TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot));

  const conSesion = (activa: boolean) =>
    TestBed.configureTestingModule({
      providers: [{ provide: AuthService, useValue: { isAuthenticated: signal(activa) } }],
    });

  it('deja pasar a quien tiene sesión', () => {
    conSesion(true);

    expect(ejecutar()).toBe(true);
  });

  it('sin sesión manda al login', () => {
    conSesion(false);

    const resultado = ejecutar() as UrlTree;

    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login');
  });
});
