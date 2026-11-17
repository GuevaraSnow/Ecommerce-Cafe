import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  imports: [RouterLink],
  selector: 'app-footer',
  styleUrl: './footer.css',
  templateUrl: './footer.html',
})
export class Footer {
  // Con sesión, la columna "Cuenta" cambia: ya no tiene sentido ofrecer ingresar o crear cuenta
  protected isLoggedIn = inject(AuthService).isAuthenticated;
}
