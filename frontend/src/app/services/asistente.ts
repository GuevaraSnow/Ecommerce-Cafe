import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { AsistenteResponse, ConsultarAsistenteRequest, MensajeChat } from '../models/asistente';

// Los mismos topes que valida el backend: 6 mensajes de historial y 500 caracteres por mensaje
const MAX_HISTORIAL = 6;
const MAX_TEXTO = 500;

@Injectable({ providedIn: 'root' })
export class AsistenteService {
  private http = inject(HttpClient);
  private url = `${API_URL}/asistente`;

  /** Pregunta al asesor de ventas. `historial` son los mensajes anteriores, sin el nuevo. */
  preguntar(mensaje: string, historial: MensajeChat[]): Observable<AsistenteResponse> {
    const solicitud: ConsultarAsistenteRequest = {
      mensaje,
      historial: historial
        .slice(-MAX_HISTORIAL)
        // Una respuesta larga del asesor no debe hacer que el backend rechace el historial
        .map((m) => ({ rol: m.rol, texto: m.texto.slice(0, MAX_TEXTO) })),
    };
    return this.http.post<AsistenteResponse>(this.url, solicitud);
  }
}
