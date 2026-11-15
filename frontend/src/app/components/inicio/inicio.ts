import { CurrencyPipe } from '@angular/common';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Presentacion } from '../../models/catalogo';
import { PresentacionesService } from '../../services/presentaciones';

@Component({
  imports: [CurrencyPipe, RouterLink],
  selector: 'app-inicio',
  styleUrl: './inicio.css',
  templateUrl: './inicio.html',
})
export class Inicio {
  private servicio = inject(PresentacionesService);
  private destroyRef = inject(DestroyRef);

  // Las cuatro formas de disfrutar el café: una por tipo de presentación
  readonly categorias = [
    { icono: 'fa-seedling', titulo: 'Café verde', detalle: 'Sin tostar, para tostar a tu gusto' },
    { icono: 'fa-fire', titulo: 'Tostado', detalle: 'Listo para tu cafetera' },
    { icono: 'fa-cube', titulo: 'Derivados', detalle: 'Molidos y preparaciones' },
    { icono: 'fa-mug-hot', titulo: 'Merchandising', detalle: 'Tazas y accesorios' },
  ];

  readonly pasos = [
    { icono: 'fa-shield-halved', titulo: 'Origen verificado', detalle: 'Cada lote registra su región y su productor antes de salir a la venta.' },
    { icono: 'fa-fire', titulo: 'Tueste y lote', detalle: 'El tueste queda asociado a cada presentación del catálogo.' },
    { icono: 'fa-truck', titulo: 'Llega a tu puerta', detalle: 'Confirma tu compra con tus datos de entrega y sigue el pedido.' },
  ];

  destacadas = signal<Presentacion[]>([]);

  constructor() {
    // Las primeras presentaciones disponibles del catálogo público
    this.servicio
      .buscar({ tamano: 4 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => this.destacadas.set(pagina.contenido),
        // Si falla, la portada se ve igual, solo sin la sección de destacadas
        error: () => this.destacadas.set([]),
      });
  }

  fotoPrincipal(p: Presentacion): string | null {
    return (p.galeria.find((imagen) => imagen.principal) ?? p.galeria[0])?.url ?? null;
  }

  tieneDescuento(p: Presentacion): boolean {
    return p.descuento !== null && p.precioVigente.monto < p.precio.monto;
  }
}
