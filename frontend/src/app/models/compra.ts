// Espejos de los DTO de compras del backend. Mismos nombres que el JSON.
import { Cantidad, Precio } from './catalogo';

export type EstadoDeCompra = 'PENDIENTE' | 'CONFIRMADA' | 'ENVIADA' | 'ENTREGADA' | 'CANCELADA' | 'REEMBOLSADA';

export interface DetalleDeCompra {
  presentacionId: string;
  precioCongelado: Precio;
  cantidad: Cantidad;
}

/** Cuerpo de PUT /api/compras/{id}/confirmar y parte de la Compra confirmada. */
export interface DireccionDeEnvio {
  ciudad: string;
  direccion: string;
  destinatario: string;
}

/** Un carrito (estado PENDIENTE) o una compra. La dirección y la fecha de entrega vienen en null mientras no existan. */
export interface Compra {
  id: string;
  compradorId: string;
  estado: EstadoDeCompra;
  fechaCompra: string;
  detalles: DetalleDeCompra[];
  direccionEnvio: DireccionDeEnvio | null;
  fechaEntrega: string | null;
}

/** Cuerpo de POST /api/compras/{id}/presentaciones. El comprador sale del token. */
export interface AgregarAlCarritoRequest {
  presentacionId: string;
  cantidad: Cantidad;
}

/** Forma de los errores de la API (400, 401, 403, 404, 422). */
export interface ErrorApi {
  timestamp: string;
  status: number;
  mensaje: string;
}
