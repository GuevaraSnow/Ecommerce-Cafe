import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';
import { TemaService } from '../../services/tema';
import { ROLES_VENDEDOR } from '../../models/usuario';

@Component({
  imports: [RouterLink, RouterLinkActive],
  selector: 'app-header',
  styleUrl: './header.css',
  templateUrl: './header.html',
})
export class Header {
  private authService = inject(AuthService);
  private router = inject(Router);
  protected tema = inject(TemaService);

  isLoggedIn = this.authService.isAuthenticated;
  nombreUsuario = this.authService.nombreUsuario;

  // Leen la señal del rol, así el menú cambia solo al iniciar o cerrar sesión.
  esComprador = computed(() => this.isLoggedIn() && this.authService.rol() === 'COMPRADOR');

  // Los roles que publican presentaciones (todos menos el comprador) ven "Mis descuentos".
  esVendedor = computed(() => {
    const rol = this.authService.rol();
    return this.isLoggedIn() && rol !== null && ROLES_VENDEDOR.includes(rol);
  });

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
