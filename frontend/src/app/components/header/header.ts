import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth';

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

  // Los roles que publican presentaciones (todos menos el comprador) ven "Mis descuentos"
  esVendedor = computed(
    () =>
      this.isLoggedIn() &&
      ['CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS'].includes(localStorage.getItem('rol') ?? ''),
  );

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
