import { Routes } from '@angular/router';
import { Inicio } from './components/inicio/inicio';
import { Registro } from './components/registro/registro';
import { Login } from './components/login/login';
import { Catalogo } from './components/catalogo/catalogo';
import { Comprar } from './components/comprar/comprar';

export const routes: Routes = [
  { path: '', component: Inicio },
  { path: 'login', component: Login },
  { path: 'registro', component: Registro },
  { path: 'catalogo', component: Catalogo },
  { path: 'comprar/:id', component: Comprar },
  { path: '**', pathMatch: 'full', redirectTo: '/' },
];
