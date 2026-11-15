import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Imagen, Presentacion } from '../../models/catalogo';
import { AuthService } from '../../services/auth';
import { ImagenesService } from '../../services/imagenes';
import { NotificacionesService } from '../../services/notificaciones';
import { PresentacionesService } from '../../services/presentaciones';

// Los mismos topes que valida el backend
const MAX_FOTOS = 10;
const MAX_BYTES = 2 * 1024 * 1024;
const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp'];

@Component({
  selector: 'app-mis-fotos',
  templateUrl: './mis-fotos.html',
})
export class MisFotos {
  private presentacionesServicio = inject(PresentacionesService);
  private imagenesServicio = inject(ImagenesService);
  private authService = inject(AuthService);
  private avisos = inject(NotificacionesService);
  private destroyRef = inject(DestroyRef);

  readonly maxFotos = MAX_FOTOS;

  // Señales de pantalla
  presentaciones = signal<Presentacion[]>([]);
  cargando = signal(true);
  seleccionada = signal<Presentacion | null>(null);
  // La galería que se está armando: solo se guarda en el backend al pulsar "Guardar galería"
  fotos = signal<Imagen[]>([]);
  subiendo = signal(false);
  guardando = signal(false);

  puedeSubir = computed(() => this.seleccionada() !== null && !this.subiendo() && this.fotos().length < MAX_FOTOS);
  puedeGuardar = computed(() => this.seleccionada() !== null && this.fotos().length > 0 && !this.guardando());

  constructor() {
    // Solo las presentaciones del vendedor que inició sesión
    this.presentacionesServicio
      .buscar({ vendedorId: this.authService.getUsuarioId(), tamano: 50 })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (pagina) => {
          this.presentaciones.set(pagina.contenido);
          this.cargando.set(false);
        },
        error: () => {
          this.avisos.error('No se pudieron cargar tus presentaciones.');
          this.cargando.set(false);
        },
      });
  }

  // Al elegir una presentación, la galería se llena con sus fotos actuales
  seleccionar(presentacion: Presentacion): void {
    this.seleccionada.set(presentacion);
    this.fotos.set(presentacion.galeria.map((imagen) => ({ ...imagen })));
  }

  elegirArchivo(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const archivo = campo.files?.[0];
    // Se limpia el campo para poder volver a elegir el mismo archivo
    campo.value = '';
    if (!archivo || !this.puedeSubir()) return;

    if (!TIPOS_PERMITIDOS.includes(archivo.type)) {
      this.avisos.error('La imagen debe ser JPG, PNG o WebP.');
      return;
    }
    if (archivo.size > MAX_BYTES) {
      this.avisos.error('La imagen no puede superar 2 MB.');
      return;
    }

    this.subiendo.set(true);
    this.imagenesServicio
      .subir(archivo)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (subida) => {
          // La primera foto de la galería es la principal
          this.fotos.update((lista) => [...lista, { url: subida.url, principal: lista.length === 0 }]);
          this.subiendo.set(false);
        },
        error: (respuesta: HttpErrorResponse) => {
          // 401 (sesión vencida) lo atiende el interceptor
          if (respuesta.status !== 401) {
            this.avisos.error(respuesta.error?.mensaje || 'No se pudo subir la imagen.');
          }
          this.subiendo.set(false);
        },
      });
  }

  hacerPrincipal(indice: number): void {
    this.fotos.update((lista) => lista.map((foto, i) => ({ ...foto, principal: i === indice })));
  }

  quitar(indice: number): void {
    this.fotos.update((lista) => {
      const restantes = lista.filter((_, i) => i !== indice);
      // Si se quitó la principal, la primera que quede pasa a serlo
      if (restantes.length > 0 && !restantes.some((foto) => foto.principal)) {
        restantes[0] = { ...restantes[0], principal: true };
      }
      return restantes;
    });
  }

  guardar(): void {
    const presentacion = this.seleccionada();
    if (!presentacion || !this.puedeGuardar()) return;

    this.guardando.set(true);
    this.presentacionesServicio
      .reemplazarGaleria(presentacion.id, this.fotos())
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (actualizada) => {
          // Deja la lista y la selección con la presentación que devolvió el backend
          this.presentaciones.update((lista) => lista.map((p) => (p.id === actualizada.id ? actualizada : p)));
          this.seleccionada.set(actualizada);
          this.avisos.exito(`Galería guardada en "${actualizada.titulo}".`);
          this.guardando.set(false);
        },
        error: (respuesta: HttpErrorResponse) => {
          if (respuesta.status === 403) {
            this.avisos.error('Solo el dueño de la presentación puede cambiar su galería.');
          } else if (respuesta.status !== 401) {
            this.avisos.error(respuesta.error?.mensaje || 'No se pudo guardar la galería.');
          }
          this.guardando.set(false);
        },
      });
  }
}
