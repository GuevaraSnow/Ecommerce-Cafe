import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, debounceTime, distinctUntilChanged, map, of, switchMap, tap } from 'rxjs';
import { AuthService } from '../../services/auth';
import { Pagina, Presentacion, TipoDePresentacion } from '../../models/catalogo';
import { PresentacionesService } from '../../services/presentaciones';

const TAMANO_PAGINA = 8;

/** Milisegundos sin escribir antes de buscar: así no sale una petición por cada tecla. */
const ESPERA_AL_ESCRIBIR = 300;

@Component({
  imports: [CurrencyPipe, RouterLink],
  selector: 'app-catalogo',
  styleUrl: './catalogo.css',
  templateUrl: './catalogo.html',
})
export class Catalogo {
  private servicio = inject(PresentacionesService);
  protected auth = inject(AuthService);

  readonly tipos: { valor: TipoDePresentacion; etiqueta: string }[] = [
    { valor: 'CAFE_VERDE', etiqueta: 'Café verde' },
    { valor: 'CAFE_TOSTADO', etiqueta: 'Café tostado' },
    { valor: 'DERIVADO_CONSUMIBLE', etiqueta: 'Derivados' },
    { valor: 'MERCHANDISING', etiqueta: 'Merchandising' },
  ];

  cargando = signal(true);
  error = signal('');

  /** Lo que el usuario va escribiendo en el buscador; cada tecla actualiza la señal. */
  termino = signal('');
  private tipo = signal<TipoDePresentacion | undefined>(undefined);
  private numeroPagina = signal(0);

  // El texto solo cuenta cuando el usuario deja de escribir: debounceTime espera la pausa
  // y distinctUntilChanged evita repetir la búsqueda si el texto quedó igual.
  private titulo = toSignal(
    toObservable(this.termino).pipe(
      map((texto) => texto.trim()),
      debounceTime(ESPERA_AL_ESCRIBIR),
      distinctUntilChanged(),
    ),
    { initialValue: '' },
  );

  // Junta en un solo objeto todo lo que define la búsqueda: si cambia cualquiera, se busca de nuevo.
  private consulta = computed(() => ({
    titulo: this.titulo(),
    tipo: this.tipo(),
    pagina: this.numeroPagina(),
  }));

  // switchMap cancela la petición anterior cuando llega una consulta nueva,
  // así una respuesta vieja nunca pisa a la más reciente.
  pagina = toSignal(
    toObservable(this.consulta).pipe(
      tap(() => {
        this.cargando.set(true);
        this.error.set('');
      }),
      switchMap((c) =>
        this.servicio.buscar({ ...c, tamano: TAMANO_PAGINA }).pipe(
          catchError(() => {
            this.error.set('No se pudo cargar el catálogo. Intenta de nuevo.');
            return of(null);
          }),
        ),
      ),
      tap(() => this.cargando.set(false)),
    ),
    { initialValue: null as Pagina<Presentacion> | null },
  );

  /** Cada tecla guarda el texto y vuelve a la primera página. */
  escribir(valor: string): void {
    this.termino.set(valor);
    this.numeroPagina.set(0);
  }

  /** El tipo se elige de una lista, así que filtra de inmediato, sin esperar. */
  filtrarPorTipo(tipo: string): void {
    this.tipo.set((tipo || undefined) as TipoDePresentacion | undefined);
    this.numeroPagina.set(0);
  }

  cambiarPagina(numero: number): void {
    this.numeroPagina.set(numero);
  }

  etiquetaDelTipo(tipo: TipoDePresentacion): string {
    return this.tipos.find((t) => t.valor === tipo)?.etiqueta ?? tipo;
  }

  // Cada tipo de presentación tiene su color de etiqueta (clases de Bootstrap)
  badgeTipo(tipo: TipoDePresentacion): string {
    const colores: Record<string, string> = {
      CAFE_VERDE: 'bg-success',
      CAFE_TOSTADO: 'bg-warning text-dark',
      DERIVADO_CONSUMIBLE: 'bg-info text-dark',
      MERCHANDISING: 'bg-secondary',
    };
    return colores[tipo] ?? 'bg-secondary';
  }

  // La foto marcada como principal; sin galería, la tarjeta no muestra imagen
  fotoPrincipal(p: Presentacion): string | null {
    return (p.galeria.find((imagen) => imagen.principal) ?? p.galeria[0])?.url ?? null;
  }

  tieneDescuento(p: Presentacion): boolean {
    return p.precioVigente.monto < p.precio.monto;
  }
}
