import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';
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

  isLoggedIn = this.authService.isAuthenticated;
  nombreUsuario = this.authService.nombreUsuario;

  // Los roles que publican presentaciones (todos menos el comprador) ven "Mis descuentos".
  // Lee la señal del rol, así el menú cambia solo al iniciar o cerrar sesión.
  esVendedor = computed(() => {
    const rol = this.authService.rol();
    return this.isLoggedIn() && rol !== null && ROLES_VENDEDOR.includes(rol);
  });

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
