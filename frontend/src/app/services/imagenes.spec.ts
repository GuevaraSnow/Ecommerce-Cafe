import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { ImagenesService } from './imagenes';

describe('ImagenesService', () => {
  let servicio: ImagenesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(ImagenesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sube el archivo con POST multipart en el campo "archivo"', () => {
    const archivo = new File(['contenido'], 'cafe.png', { type: 'image/png' });

    servicio.subir(archivo).subscribe((respuesta) => expect(respuesta.url).toBe('https://img/cafe.png'));

    const peticion = http.expectOne(`${API_URL}/imagenes`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body instanceof FormData).toBe(true);
    expect((peticion.request.body as FormData).get('archivo')).toBe(archivo);
    peticion.flush({ url: 'https://img/cafe.png' });
  });
});
