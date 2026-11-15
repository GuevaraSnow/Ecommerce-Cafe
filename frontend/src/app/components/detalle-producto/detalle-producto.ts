import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FichaDeOrigen, Imagen, Presentacion, TipoDePresentacion } from '../../models/catalogo';
import { AuthService } from '../../services/auth';
import { PresentacionesService } from '../../services/presentaciones';

@Component({
  selector: 'app-detalle-producto',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './detalle-producto.html',
  styleUrl: './detalle-producto.css',
})
export class DetalleProducto implements OnInit {
  private route = inject(ActivatedRoute);
  private presentacionesService = inject(PresentacionesService);
  protected auth = inject(AuthService);

  presentacion = signal<Presentacion | null>(null);
  fichaOrigen = signal<FichaDeOrigen | null>(null);
  imagenActiva = signal<string | null>(null);

  cargando = signal<boolean>(true);
  cargandoOrigen = signal<boolean>(false);
  error = signal<string | null>(null);
  errorOrigen = signal<string | null>(null);

  tieneDescuento = computed(() => {
    const p = this.presentacion();
    return !!p && p.precioVigente.monto < p.precio.monto;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('No se especificó el identificador del producto.');
      this.cargando.set(false);
      return;
    }

    this.cargarPresentacion(id);
  }

  private cargarPresentacion(id: string): void {
    this.cargando.set(true);
    this.error.set(null);

    this.presentacionesService.consultar(id).subscribe({
      next: (prod) => {
        this.presentacion.set(prod);
        const principal = prod.galeria.find((img) => img.principal) ?? prod.galeria[0];
        this.imagenActiva.set(principal?.url ?? null);
        this.cargando.set(false);

        const transformacionId = prod.origen?.transformacionId;
        if (transformacionId && this.auth.isAuthenticated()) {
          this.cargarFichaOrigen(transformacionId);
        }
      },
      error: (err) => {
        this.error.set(err?.error?.mensaje ?? 'No se pudo cargar el detalle de esta presentación.');
        this.cargando.set(false);
      },
    });
  }

  private cargarFichaOrigen(transformacionId: string): void {
    this.cargandoOrigen.set(true);
    this.errorOrigen.set(null);

    this.presentacionesService.consultarFichaOrigen(transformacionId).subscribe({
      next: (ficha) => {
        this.fichaOrigen.set(ficha);
        this.cargandoOrigen.set(false);
      },
      error: (err) => {
        this.errorOrigen.set(
          err?.error?.mensaje ?? 'No fue posible consultar la ficha de origen de este lote.',
        );
        this.cargandoOrigen.set(false);
      },
    });
  }

  seleccionarImagen(img: Imagen): void {
    this.imagenActiva.set(img.url);
  }

  etiquetaDelTipo(tipo: TipoDePresentacion): string {
    const etiquetas: Record<TipoDePresentacion, string> = {
      CAFE_VERDE: 'Café verde',
      CAFE_TOSTADO: 'Café tostado',
      DERIVADO_CONSUMIBLE: 'Derivado consumible',
      MERCHANDISING: 'Merchandising',
    };
    return etiquetas[tipo] ?? tipo;
  }
}
