import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_URL } from '../api';
import { LoginRequest, LoginResponse } from '../models/auth';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private url = `${API_URL}/usuarios`;

  isAuthenticated = signal(!!localStorage.getItem('token'));
  nombreUsuario = signal(localStorage.getItem('nombre') || '');

  login(credenciales: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.url}/login`, credenciales);
  }

  guardarSesion(respuesta: LoginResponse): void {
    localStorage.setItem('token', respuesta.token);
    localStorage.setItem('rol', respuesta.usuario.rol);
    localStorage.setItem('nombre', respuesta.usuario.nombre);
    this.nombreUsuario.set(respuesta.usuario.nombre);
    this.isAuthenticated.set(true);
  }

  getToken(): string | null {
    return localStorage.getItem('token');
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('rol');
    localStorage.removeItem('nombre');
    this.nombreUsuario.set('');
    this.isAuthenticated.set(false);
  }
}
