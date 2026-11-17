import { Injectable, signal } from '@angular/core';

/** Modo claro u oscuro. Bootstrap lo lee del atributo data-bs-theme de <html>. */
@Injectable({ providedIn: 'root' })
export class TemaService {
  // index.html ya puso el atributo antes de pintar; aquí solo se lee
  oscuro = signal(document.documentElement.getAttribute('data-bs-theme') === 'dark');

  alternar(): void {
    const oscuro = !this.oscuro();
    this.oscuro.set(oscuro);
    document.documentElement.setAttribute('data-bs-theme', oscuro ? 'dark' : 'light');
    try {
      localStorage.setItem('tema', oscuro ? 'oscuro' : 'claro');
    } catch {
      // Sin almacenamiento el tema funciona igual, solo que no se recuerda
    }
  }
}
