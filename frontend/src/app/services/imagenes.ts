import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api';
import { ImagenSubida } from '../models/catalogo';

@Injectable({ providedIn: 'root' })
export class ImagenesService {
  private http = inject(HttpClient);
  private url = `${API_URL}/imagenes`;

  /** Solo vendedores: sube una imagen (JPG, PNG o WebP de hasta 2 MB) y devuelve su URL pública. */
  subir(archivo: File): Observable<ImagenSubida> {
    // Con FormData el navegador arma solo el Content-Type multipart; el campo se llama "archivo"
    const cuerpo = new FormData();
    cuerpo.append('archivo', archivo);
    return this.http.post<ImagenSubida>(this.url, cuerpo);
  }
}
