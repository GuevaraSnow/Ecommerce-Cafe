import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { PresentacionesService } from './presentaciones';

describe('PresentacionesService', () => {
  let servicio: PresentacionesService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(PresentacionesService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('pide el catálogo con los filtros como parámetros', () => {
    servicio.buscar({ titulo: 'huila', tipo: 'CAFE_TOSTADO', pagina: 1, tamano: 8 }).subscribe();

    const peticion = http.expectOne((r) => r.url === `${API_URL}/presentaciones`);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('titulo')).toBe('huila');
    expect(peticion.request.params.get('tipo')).toBe('CAFE_TOSTADO');
    expect(peticion.request.params.get('pagina')).toBe('1');
    expect(peticion.request.params.get('tamano')).toBe('8');
    peticion.flush({ contenido: [], pagina: 1, tamano: 8, totalElementos: 0, totalPaginas: 0 });
  });

  it('no manda parámetros vacíos', () => {
    servicio.buscar({ titulo: '' }).subscribe();

    const peticion = http.expectOne((r) => r.url === `${API_URL}/presentaciones`);
    expect(peticion.request.params.keys()).toEqual([]);
    peticion.flush({ contenido: [], pagina: 0, tamano: 10, totalElementos: 0, totalPaginas: 0 });
  });
});
