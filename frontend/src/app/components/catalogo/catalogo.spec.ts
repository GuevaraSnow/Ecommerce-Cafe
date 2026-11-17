import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { Catalogo } from './catalogo';
import { AuthService } from '../../services/auth';
import { PresentacionesService } from '../../services/presentaciones';

describe('Catalogo: búsqueda reactiva', () => {
  const paginaVacia = { contenido: [], pagina: 0, tamano: 8, totalElementos: 0, totalPaginas: 0 };
  let buscar: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    vi.useFakeTimers();
    buscar = vi.fn().mockReturnValue(of(paginaVacia));
    TestBed.configureTestingModule({
      imports: [Catalogo],
      providers: [
        provideRouter([]),
        { provide: PresentacionesService, useValue: { buscar } },
        { provide: AuthService, useValue: { isAuthenticated: signal(false) } },
      ],
    });
  });

  afterEach(() => vi.useRealTimers());

  const escribir = (componente: Catalogo, texto: string) => {
    componente.escribir(texto);
    TestBed.tick();
  };

  it('espera a que el usuario deje de escribir y busca una sola vez', () => {
    const componente = TestBed.createComponent(Catalogo).componentInstance;
    TestBed.tick();
    buscar.mockClear();

    escribir(componente, 'c');
    vi.advanceTimersByTime(100);
    escribir(componente, 'ca');
    vi.advanceTimersByTime(100);
    escribir(componente, 'caf');
    expect(buscar).not.toHaveBeenCalled();

    vi.advanceTimersByTime(300);
    TestBed.tick();

    expect(buscar).toHaveBeenCalledTimes(1);
    expect(buscar).toHaveBeenCalledWith(expect.objectContaining({ titulo: 'caf', pagina: 0 }));
  });

  it('el filtro por tipo busca de inmediato, sin esperar', () => {
    const componente = TestBed.createComponent(Catalogo).componentInstance;
    TestBed.tick();
    buscar.mockClear();

    componente.filtrarPorTipo('CAFE_VERDE');
    TestBed.tick();

    expect(buscar).toHaveBeenCalledTimes(1);
    expect(buscar).toHaveBeenCalledWith(expect.objectContaining({ tipo: 'CAFE_VERDE', pagina: 0 }));
  });
});
