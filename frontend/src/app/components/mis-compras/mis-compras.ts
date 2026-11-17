import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { catchError, forkJoin, of } from 'rxjs';
import { Compra, ErrorApi, EstadoDeCompra } from '../../models/compra';
import { ComprasService } from '../../services/compras';
import { PresentacionesService } from '../../services/presentaciones';

@Component({
  selector: 'app-mis-compras',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './mis-compras.html',
  styleUrl: './mis-compras.css',
})
export class MisCompras implements OnInit {
  private comprasService = inject(ComprasService);
  private presentacionesService = inject(PresentacionesService);

  compras = signal<Compra[]>([]);
  // Título de cada presentación comprada, por su id; mientras llega (o si falla) se muestra el id corto
  titulos = signal<Record<string, string>>({});
  cargando = signal<boolean>(true);
  cancelandoId = signal<string | null>(null);
  error = signal<string | null>(null);
  mensajeExito = signal<string | null>(null);

  ngOnInit(): void {
    this.cargarCompras();
  }

  cargarCompras(): void {
    this.cargando.set(true);
    this.error.set(null);

    this.comprasService.historial().subscribe({
      next: (lista) => {
        this.compras.set(lista);
        this.cargando.set(false);
        this.cargarTitulos(lista);
      },
      error: (err) => {
        const apiError = err?.error as ErrorApi | undefined;
        this.error.set(
          apiError?.mensaje ?? 'No se pudo cargar el historial de compras. Intenta nuevamente.',
        );
        this.cargando.set(false);
      },
    });
  }

  // La compra solo trae el id de la presentación: se consulta cada una una sola vez para mostrar su título
  private cargarTitulos(lista: Compra[]): void {
    const ids = [...new Set(lista.flatMap((c) => c.detalles.map((d) => d.presentacionId)))];
    if (ids.length === 0) return;

    forkJoin(
      ids.map((id) =>
        this.presentacionesService.consultar(id).pipe(catchError(() => of(null))),
      ),
    ).subscribe((presentaciones) => {
      const titulos: Record<string, string> = {};
      presentaciones.forEach((p, i) => {
        if (p) titulos[ids[i]] = p.titulo;
      });
      this.titulos.set(titulos);
    });
  }

  cancelarCompra(compra: Compra): void {
    if (compra.estado !== 'PENDIENTE' || this.cancelandoId()) {
      return;
    }

    this.cancelandoId.set(compra.id);
    this.error.set(null);
    this.mensajeExito.set(null);

    this.comprasService.cancelar(compra.id).subscribe({
      next: (compraActualizada) => {
        this.compras.update((actuales) =>
          actuales.map((item) => (item.id === compraActualizada.id ? compraActualizada : item)),
        );
        this.mensajeExito.set('La compra pendiente fue cancelada correctamente.');
        this.cancelandoId.set(null);
      },
      error: (err) => {
        const apiError = err?.error as ErrorApi | undefined;
        this.error.set(apiError?.mensaje ?? 'No fue posible cancelar la compra seleccionada.');
        this.cancelandoId.set(null);
      },
    });
  }

  calcularTotal(compra: Compra): number {
    return compra.detalles.reduce(
      (acumulado, detalle) => acumulado + detalle.precioCongelado.monto * detalle.cantidad.valor,
      0,
    );
  }

  obtenerMoneda(compra: Compra): string {
    return compra.detalles[0]?.precioCongelado.moneda ?? 'COP';
  }

  claseEstado(estado: EstadoDeCompra): string {
    switch (estado) {
      case 'PENDIENTE':
        return 'bg-warning text-dark';
      case 'CONFIRMADA':
        return 'bg-primary';
      case 'ENVIADA':
        return 'bg-info text-dark';
      case 'ENTREGADA':
        return 'bg-success';
      case 'CANCELADA':
        return 'bg-danger';
      case 'REEMBOLSADA':
        return 'bg-secondary';
    }
  }
}
