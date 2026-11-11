import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../api';
import { Compra } from '../models/compra';
import { ComprasService } from './compras';

function compra(estado: Compra['estado']): Compra {
  return {
    id: 'compra-1',
    compradorId: 'usr-ana',
    estado,
    fechaCompra: '2026-11-10',
    detalles: [],
    direccionEnvio: null,
    fechaEntrega: null,
  };
}

describe('ComprasService', () => {
  let servicio: ComprasService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(ComprasService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('comprar abre el carrito, agrega la presentación y confirma, en ese orden', () => {
    const direccion = { ciudad: 'Armenia', direccion: 'Calle 1 # 2-3', destinatario: 'Ana' };
    let resultado: Compra | undefined;

    servicio.comprar('pres-1', { valor: 2, unidad: 'kg' }, direccion).subscribe((c) => (resultado = c));

    http.expectOne({ method: 'POST', url: `${API_URL}/compras` }).flush(compra('PENDIENTE'));

    const agregar = http.expectOne({ method: 'POST', url: `${API_URL}/compras/compra-1/presentaciones` });
    expect(agregar.request.body).toEqual({ presentacionId: 'pres-1', cantidad: { valor: 2, unidad: 'kg' } });
    agregar.flush(compra('PENDIENTE'));

    const confirmar = http.expectOne({ method: 'PUT', url: `${API_URL}/compras/compra-1/confirmar` });
    expect(confirmar.request.body).toEqual(direccion);
    confirmar.flush(compra('CONFIRMADA'));

    expect(resultado?.estado).toBe('CONFIRMADA');
  });

  it('si agregar al carrito falla, no llega a confirmar', () => {
    let fallo = false;

    servicio
      .comprar('pres-1', { valor: 999, unidad: 'kg' }, { ciudad: 'a', direccion: 'b', destinatario: 'c' })
      .subscribe({ error: () => (fallo = true) });

    http.expectOne({ method: 'POST', url: `${API_URL}/compras` }).flush(compra('PENDIENTE'));
    http
      .expectOne(`${API_URL}/compras/compra-1/presentaciones`)
      .flush({ mensaje: 'Stock insuficiente' }, { status: 422, statusText: 'Unprocessable Entity' });

    http.expectNone(`${API_URL}/compras/compra-1/confirmar`);
    expect(fallo).toBe(true);
  });
});
