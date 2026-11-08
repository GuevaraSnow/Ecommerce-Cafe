import { UsuarioResponse } from './usuario';

export interface LoginRequest {
  email: string;
  contrasena: string;
}

export interface LoginResponse {
  token: string;
  usuario: UsuarioResponse;
}
