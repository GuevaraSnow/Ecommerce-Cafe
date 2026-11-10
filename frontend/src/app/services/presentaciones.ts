import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { FiltroCatalogo, Pagina, Presentacion } from '../models/catalogo';

@Injectable({ providedIn: 'root' })
export class PresentacionesService {
  private http = inject(HttpClient);
  private url = `${API_URL}/presentaciones`;

  /** Catálogo público: presentaciones disponibles, con filtros opcionales y paginado. */
  buscar(filtro: FiltroCatalogo = {}): Observable<Pagina<Presentacion>> {
    let params = new HttpParams();
    if (filtro.titulo) params = params.set('titulo', filtro.titulo);
    if (filtro.tipo) params = params.set('tipo', filtro.tipo);
    if (filtro.pagina !== undefined) params = params.set('pagina', filtro.pagina);
    if (filtro.tamano !== undefined) params = params.set('tamano', filtro.tamano);
    return this.http.get<Pagina<Presentacion>>(this.url, { params });
  }

  consultar(id: string): Observable<Presentacion> {
    return this.http.get<Presentacion>(`${this.url}/${id}`);
  }
}
