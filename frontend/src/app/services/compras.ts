import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { API_URL } from '../api';
import { Cantidad } from '../models/catalogo';
import { AgregarAlCarritoRequest, Compra, DireccionDeEnvio } from '../models/compra';

@Injectable({ providedIn: 'root' })
export class ComprasService {
  private http = inject(HttpClient);
  private url = `${API_URL}/compras`;

  /** Abre un carrito vacío para el comprador del token. */
  crearCarrito(): Observable<Compra> {
    return this.http.post<Compra>(this.url, {});
  }

  agregarAlCarrito(compraId: string, pedido: AgregarAlCarritoRequest): Observable<Compra> {
    return this.http.post<Compra>(`${this.url}/${compraId}/presentaciones`, pedido);
  }

  confirmar(compraId: string, direccion: DireccionDeEnvio): Observable<Compra> {
    return this.http.put<Compra>(`${this.url}/${compraId}/confirmar`, direccion);
  }

  /** Historial de compras del comprador del token. */
  historial(): Observable<Compra[]> {
    return this.http.get<Compra[]>(this.url);
  }

  /** El flujo completo de comprar una presentación: abrir carrito, agregarla y confirmar con la dirección. */
  comprar(presentacionId: string, cantidad: Cantidad, direccion: DireccionDeEnvio): Observable<Compra> {
    return this.crearCarrito().pipe(
      switchMap((carrito) => this.agregarAlCarrito(carrito.id, { presentacionId, cantidad })),
      switchMap((carrito) => this.confirmar(carrito.id, direccion)),
    );
  }
}
