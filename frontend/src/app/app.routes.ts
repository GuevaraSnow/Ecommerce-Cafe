import { Routes } from '@angular/router';
import { Inicio } from './components/inicio/inicio';
import { Registro } from './components/registro/registro';
import { Login } from './components/login/login';
import { Catalogo } from './components/catalogo/catalogo';
import { Comprar } from './components/comprar/comprar';
import { authGuard } from './guards/auth-guard';
import { guestGuard } from './guards/guest-guard';

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'registro', component: Registro },
  { path: 'catalogo', component: Catalogo },
  { path: 'comprar/:id', component: Comprar, canActivate: [authGuard] },
  { path: '**', pathMatch: 'full', redirectTo: '/' },
];
