export type Rol = 'COMPRADOR' | 'CAFICULTOR' | 'TOSTADOR' | 'VENDEDOR_DERIVADOS';

/** Los roles que publican Presentaciones: todos menos el comprador. */
export const ROLES_VENDEDOR: Rol[] = ['CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS'];

export interface UsuarioResponse {
  id: string;
  nombre: string;
  email: string;
  telefono?: string;
  rol: Rol;
}
