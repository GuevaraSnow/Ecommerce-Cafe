import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { Pagina, Presentacion, TipoDePresentacion } from '../../models/catalogo';
import { PresentacionesService } from '../../services/presentaciones';

const TAMANO_PAGINA = 8;

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

  private titulo = '';
  private tipo: TipoDePresentacion | undefined;

  constructor() {
    this.cargar(0);
  }

  buscar(titulo: string, tipo: string): void {
    this.titulo = titulo.trim();
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

  tieneDescuento(p: Presentacion): boolean {
    return p.precioVigente.monto < p.precio.monto;
  }

  private cargar(numero: number): void {
    this.cargando.set(true);
    this.error.set('');
    this.servicio
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
