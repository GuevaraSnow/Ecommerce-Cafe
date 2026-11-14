// Espejos de los DTO del backend (PresentacionResponse y PaginaResponse). Mismos nombres que el JSON.

export type TipoDePresentacion = 'CAFE_VERDE' | 'CAFE_TOSTADO' | 'DERIVADO_CONSUMIBLE' | 'MERCHANDISING';

export type EstadoDePublicacion = 'ACTIVA' | 'AGOTADA' | 'VENCIDA' | 'INACTIVA';

export type PerfilTueste = 'TRADICIONAL' | 'CLARO' | 'MEDIO' | 'OSCURO';

export interface Precio {
  monto: number;
  moneda: string;
}

export interface Cantidad {
  valor: number;
  unidad: string;
}

export interface Imagen {
  url: string;
  principal: boolean;
}

export interface Descuento {
  porcentaje: number;
  fechaInicio: string;
  fechaFin: string;
}

/** Exactamente uno de los dos viene informado (el otro es null). */
export interface Origen {
  loteId: string | null;
  transformacionId: string | null;
}

/**
 * El backend responde dos variantes: la trazable (café verde, tostado y derivados) trae origen y datos de
 * tueste; el merchandising trae descripción y material. Por eso esos campos son opcionales.
 */
export interface Presentacion {
  id: string;
  vendedorId: string;
  titulo: string;
  tipo: TipoDePresentacion;
  precio: Precio;
  precioVigente: Precio;
  descuento: Descuento | null;
  cantidadDisponible: Cantidad;
  galeria: Imagen[];
  estado: EstadoDePublicacion;
  eliminada: boolean;
  origen?: Origen;
  perfilTueste?: PerfilTueste | null;
  fechaTueste?: string | null;
  notaCata?: string[] | null;
  descripcion?: string;
  material?: string;
}

/** Cuerpo de PUT /presentaciones/{id}/descuento: porcentaje entre 1 y 50 y fechas yyyy-MM-dd. */
export interface ProgramarDescuentoRequest {
  porcentaje: number;
  fechaInicio: string;
  fechaFin: string;
}

/** La primera página es la 0. */
export interface Pagina<T> {
  contenido: T[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface FiltroCatalogo {
  titulo?: string;
  vendedorId?: string;
  tipo?: TipoDePresentacion;
  pagina?: number;
  tamano?: number;
}
