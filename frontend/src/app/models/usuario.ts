export type Rol = 'COMPRADOR' | 'CAFICULTOR' | 'TOSTADOR' | 'VENDEDOR_DERIVADOS';

export interface UsuarioResponse {
  id: string;
  nombre: string;
  email: string;
  telefono?: string;
  rol: Rol;
}
