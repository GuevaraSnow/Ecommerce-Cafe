import { Injectable, signal } from '@angular/core';

// Un aviso es un mensaje corto que se ve unos segundos arriba a la derecha
export interface Aviso {
  id: number;
  tipo: 'exito' | 'error' | 'info';
  texto: string;
}

@Injectable({ providedIn: 'root' })
export class NotificacionesService {
  // Contador para darle un id distinto a cada aviso (solo lo usa el servicio)
  private siguienteId = 0;

  // La lista de avisos que se ven ahora; pública porque el componente la lee
  avisos = signal<Aviso[]>([]);

  // Atajos: quien los usa escribe avisos.error('...') en vez de mostrar('error', '...')
  exito(texto: string): void {
    this.mostrar('exito', texto);
  }

  error(texto: string): void {
    this.mostrar('error', texto);
  }

  info(texto: string): void {
    this.mostrar('info', texto);
  }

  // Deja todos los avisos menos el que tiene ese id
  quitar(id: number): void {
    this.avisos.update((lista) => lista.filter((aviso) => aviso.id !== id));
  }

  private mostrar(tipo: Aviso['tipo'], texto: string): void {
    const id = this.siguienteId++;
    // El spread crea una lista nueva con los avisos de antes más el nuevo
    this.avisos.update((lista) => [...lista, { id, tipo, texto }]);
    // A los 4 segundos el aviso desaparece solo
    setTimeout(() => this.quitar(id), 4000);
  }
}
