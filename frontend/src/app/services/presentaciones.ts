import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import {
  FichaDeOrigen,
  Imagen,
  FiltroCatalogo,
  Pagina,
  Presentacion,
  ProgramarDescuentoRequest,
} from '../models/catalogo';

@Injectable({ providedIn: 'root' })
export class PresentacionesService {
  private http = inject(HttpClient);
  private url = `${API_URL}/presentaciones`;

  /** Catálogo público: presentaciones disponibles, con filtros opcionales y paginado. */
  buscar(filtro: FiltroCatalogo = {}): Observable<Pagina<Presentacion>> {
    let params = new HttpParams();
    if (filtro.titulo) params = params.set('titulo', filtro.titulo);
    if (filtro.vendedorId) params = params.set('vendedorId', filtro.vendedorId);
    if (filtro.tipo) params = params.set('tipo', filtro.tipo);
    if (filtro.pagina !== undefined) params = params.set('pagina', filtro.pagina);
    if (filtro.tamano !== undefined) params = params.set('tamano', filtro.tamano);
    return this.http.get<Pagina<Presentacion>>(this.url, { params });
  }

  consultar(id: string): Observable<Presentacion> {
    return this.http.get<Presentacion>(`${this.url}/${id}`);
  }

  /** Consulta la ficha de origen (finca, cosecha, lote y tostión) de una transformación. */
  consultarFichaOrigen(transformacionId: string): Observable<FichaDeOrigen> {
    return this.http.get<FichaDeOrigen>(
      `${API_URL}/transformaciones/${transformacionId}/ficha-origen`,
    );
  }

  /** Solo el dueño: programa (o reemplaza) el descuento por temporada. */
  programarDescuento(id: string, descuento: ProgramarDescuentoRequest): Observable<Presentacion> {
    return this.http.put<Presentacion>(`${this.url}/${id}/descuento`, descuento);
  }

  /** Solo el dueño: reemplaza la galería (de 1 a 10 imágenes, con exactamente una principal). */
  reemplazarGaleria(id: string, galeria: Imagen[]): Observable<Presentacion> {
    return this.http.put<Presentacion>(`${this.url}/${id}/galeria`, { galeria });
  }

  /** Solo el dueño: quita el descuento programado. */
  quitarDescuento(id: string): Observable<Presentacion> {
    return this.http.delete<Presentacion>(`${this.url}/${id}/descuento`);
  }
}
