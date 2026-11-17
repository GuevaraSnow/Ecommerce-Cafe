import { Rol, UsuarioResponse } from './usuario';

export interface LoginRequest {
  email: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
  usuario: UsuarioResponse;
}

/** Cuerpo de POST /usuarios/registro (RegistrarUsuarioRequest del backend). La fecha va como yyyy-MM-dd. */
export interface RegistroRequest {
  nombre: string;
  email: string;
  contrasena: string;
  telefono: string;
  fechaNacimiento: string;
  rol: Rol;
}
