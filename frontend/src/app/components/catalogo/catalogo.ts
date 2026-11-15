import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Subscription, debounceTime, distinctUntilChanged, map, skip } from 'rxjs';
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
  private destroyRef = inject(DestroyRef);
  protected auth = inject(AuthService);

  readonly tipos: { valor: TipoDePresentacion; etiqueta: string }[] = [
    { valor: 'CAFE_VERDE', etiqueta: 'Café verde' },
    { valor: 'CAFE_TOSTADO', etiqueta: 'Café tostado' },
    { valor: 'DERIVADO_CONSUMIBLE', etiqueta: 'Derivados' },
    { valor: 'MERCHANDISING', etiqueta: 'Merchandising' },
  ];

  pagina = signal<Pagina<Presentacion> | null>(null);
  cargando = signal(true);
  error = signal('');

  /** Lo que el usuario va escribiendo en el buscador; cada tecla actualiza la señal. */
  termino = signal('');

  private titulo = '';
  private tipo: TipoDePresentacion | undefined;
  private peticion?: Subscription;

  constructor() {
    this.cargar(0);

    // La señal se convierte en observable para esperar a que el usuario deje de escribir.
    // skip(1) ignora el valor inicial (ya se cargó arriba) y distinctUntilChanged evita
    // repetir la búsqueda si el texto quedó igual (por ejemplo, borrar y volver a escribir la misma letra).
    toObservable(this.termino)
      .pipe(
        skip(1),
        map((texto) => texto.trim()),
        debounceTime(ESPERA_AL_ESCRIBIR),
        distinctUntilChanged(),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((texto) => {
        this.titulo = texto;
        this.cargar(0);
      });
  }

  /** El tipo se elige de una lista, así que filtra de inmediato, sin esperar. */
  filtrarPorTipo(tipo: string): void {
    this.tipo = (tipo || undefined) as TipoDePresentacion | undefined;
    this.cargar(0);
  }

  cambiarPagina(numero: number): void {
    this.cargar(numero);
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

  private cargar(numero: number): void {
    this.cargando.set(true);
    this.error.set('');
    // Si llega una búsqueda nueva, se cancela la anterior para que una respuesta vieja no la pise.
    this.peticion?.unsubscribe();
    this.peticion = this.servicio
      .buscar({ titulo: this.titulo, tipo: this.tipo, pagina: numero, tamano: TAMANO_PAGINA })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.pagina.set(pagina);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo cargar el catálogo. Intenta de nuevo.');
          this.cargando.set(false);
        },
      });
  }
}
