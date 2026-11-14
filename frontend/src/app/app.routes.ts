import { Routes } from '@angular/router';
import { Inicio } from './components/inicio/inicio';
import { Registro } from './components/registro/registro';
import { Login } from './components/login/login';
import { Catalogo } from './components/catalogo/catalogo';
import { Comprar } from './components/comprar/comprar';
<<<<<<< HEAD
import { authGuard } from './guards/auth-guard';
import { guestGuard } from './guards/guest-guard';
=======
import { Descuentos } from './components/descuentos/descuentos';
>>>>>>> 5c3490110bc8f7c16694989dbe04a8b41c765713

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'registro', component: Registro },
  { path: 'catalogo', component: Catalogo },
<<<<<<< HEAD
  { path: 'comprar/:id', component: Comprar, canActivate: [authGuard] },
=======
  { path: 'comprar/:id', component: Comprar },
  { path: 'descuentos', component: Descuentos },
>>>>>>> 5c3490110bc8f7c16694989dbe04a8b41c765713
  { path: '**', pathMatch: 'full', redirectTo: '/' },
];
