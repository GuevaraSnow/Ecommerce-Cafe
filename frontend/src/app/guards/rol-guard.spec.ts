import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { rolGuard } from './rol-guard';
import { AuthService } from '../services/auth';
import { ROLES_VENDEDOR, Rol } from '../models/usuario';

describe('rolGuard', () => {
  const rutaDeVendedores = { data: { roles: ROLES_VENDEDOR } } as unknown as ActivatedRouteSnapshot;

  const ejecutar = () =>
    TestBed.runInInjectionContext(() => rolGuard(rutaDeVendedores, {} as RouterStateSnapshot));

  const conSesion = (rol: Rol | null) =>
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { isAuthenticated: signal(rol !== null), rol: signal(rol) } },
      ],
    });

  const destino = (resultado: unknown) =>
    TestBed.inject(Router).serializeUrl(resultado as UrlTree);

  it('deja pasar a un rol permitido', () => {
    conSesion('TOSTADOR');

    expect(ejecutar()).toBe(true);
  });

  it('un comprador no entra a una ruta de vendedores: va al inicio', () => {
    conSesion('COMPRADOR');

    expect(destino(ejecutar())).toBe('/');
  });

  it('sin sesión manda al login', () => {
    conSesion(null);

    expect(destino(ejecutar())).toBe('/login');
  });
});
