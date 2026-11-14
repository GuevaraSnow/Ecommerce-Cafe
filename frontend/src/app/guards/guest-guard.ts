import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

// Saca del login a quien ya tiene sesión: lo manda al inicio.
// Igual que authGuard, es experiencia de usuario: la seguridad real la pone el backend con el JWT.
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.isAuthenticated() ? router.createUrlTree(['/']) : true;
};
