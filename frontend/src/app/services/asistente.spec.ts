import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { MensajeChat } from '../models/asistente';
import { AsistenteService } from './asistente';

describe('AsistenteService', () => {
  let servicio: AsistenteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(AsistenteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('manda el mensaje nuevo y el historial con POST', () => {
    const historial: MensajeChat[] = [{ rol: 'usuario', texto: 'hola' }];

    servicio.preguntar('¿qué café me recomiendas?', historial).subscribe();

    const peticion = http.expectOne(`${API_URL}/asistente`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual({ mensaje: '¿qué café me recomiendas?', historial });
    peticion.flush({ respuesta: 'Te recomiendo el Huila.' });
  });

  it('manda solo los últimos 6 mensajes y recorta los textos a 500 caracteres', () => {
    const historial: MensajeChat[] = Array.from({ length: 10 }, (_, i) => ({
      rol: 'usuario',
      texto: i === 9 ? 'x'.repeat(800) : `mensaje ${i + 1}`,
    }));

    servicio.preguntar('hola', historial).subscribe();

    const peticion = http.expectOne(`${API_URL}/asistente`);
    const enviado = peticion.request.body.historial as MensajeChat[];
    expect(enviado.length).toBe(6);
    expect(enviado[0].texto).toBe('mensaje 5');
    expect(enviado[5].texto.length).toBe(500);
    peticion.flush({ respuesta: 'ok' });
  });
});
