import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Footer } from './components/footer/footer';
import { Header } from './components/header/header';
import { Notificaciones } from './components/notificaciones/notificaciones';

@Component({
  imports: [RouterOutlet, Header, Footer, Notificaciones],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {}
