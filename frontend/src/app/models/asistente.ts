/** Un mensaje de la conversación con el asesor; los roles son los que acepta el backend. */
export interface MensajeChat {
  rol: 'usuario' | 'asistente';
  texto: string;
}

/** Cuerpo de POST /asistente: el mensaje nuevo y los últimos de la conversación. */
export interface ConsultarAsistenteRequest {
  mensaje: string;
  historial: MensajeChat[];
}

export interface AsistenteResponse {
  respuesta: string;
}
