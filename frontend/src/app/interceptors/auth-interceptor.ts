import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth';
import { NotificacionesService } from '../services/notificaciones';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const avisos = inject(NotificacionesService);

  // Mitad 1 (la de antes): si hay token, se pega en la petición que sale
  const token = auth.getToken();
  const peticion = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  // Mitad 2 (nueva): mirar lo que devuelve el backend
  return next(peticion).pipe(
    catchError((error: HttpErrorResponse) => {
      // Sesión vencida = 401 + la petición llevaba token + no es el propio login
      // (el login también responde 401 con la contraseña mal, y eso lo maneja el formulario)
      if (error.status === 401 && token && !req.url.includes('login')) {
        auth.logout();
        avisos.error('Tu sesión venció, inicia sesión nuevamente.');
        router.navigate(['/login']);
      }
      // Pase lo que pase, el error se sigue propagando a quien hizo la petición
      return throwError(() => error);
    }),
  );
};
