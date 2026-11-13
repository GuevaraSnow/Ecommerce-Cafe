import { Component, inject } from '@angular/core';
import { NotificacionesService } from '../../services/notificaciones';

@Component({
  selector: 'app-notificaciones',
  templateUrl: './notificaciones.html',
})
export class Notificaciones {
  // Público (sin private) para que el HTML pueda leer la lista de avisos
  notificaciones = inject(NotificacionesService);
}
