import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';
import { Rol } from '../models/usuario';

// Deja pasar solo a los roles que la ruta declara en su data, por ejemplo:
// { path: 'descuentos', canActivate: [rolGuard], data: { roles: ROLES_VENDEDOR } }
// Sin sesión manda al login; con sesión pero sin el rol, al inicio.
// Es experiencia de usuario: el backend igual responde 403 si el rol no tiene permiso.
export const rolGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const permitidos = (route.data['roles'] ?? []) as Rol[];
  const rol = auth.rol();

  if (!auth.isAuthenticated() || rol === null) {
    return router.createUrlTree(['/login']);
  }
  return permitidos.includes(rol) ? true : router.createUrlTree(['/']);
};
