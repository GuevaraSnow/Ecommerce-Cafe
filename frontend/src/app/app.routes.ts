import { Routes } from '@angular/router';
import { Inicio } from './components/inicio/inicio';
import { Registro } from './components/registro/registro';
import { Login } from './components/login/login';
import { Catalogo } from './components/catalogo/catalogo';
import { Comprar } from './components/comprar/comprar';
import { Descuentos } from './components/descuentos/descuentos';
import { MisCompras } from './components/mis-compras/mis-compras';
import { DetalleProducto } from './components/detalle-producto/detalle-producto';
import { authGuard } from './guards/auth-guard';
import { guestGuard } from './guards/guest-guard';

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'registro', component: Registro },
  { path: 'catalogo', component: Catalogo },
  { path: 'producto/:id', component: DetalleProducto },
  { path: 'comprar/:id', component: Comprar, canActivate: [authGuard] },
  { path: 'mis-compras', component: MisCompras, canActivate: [authGuard] },
  { path: 'descuentos', component: Descuentos, canActivate: [authGuard] },
  { path: '**', pathMatch: 'full', redirectTo: '/' },
];
