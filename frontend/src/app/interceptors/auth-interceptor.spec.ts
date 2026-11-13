import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from './auth-interceptor';
import { NotificacionesService } from '../services/notificaciones';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;
  let avisos: NotificacionesService;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
    avisos = TestBed.inject(NotificacionesService);
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('con un 401 y token cierra la sesión, avisa y manda al login', () => {
    localStorage.setItem('token', 'basura');
    http.get('/api/compras').subscribe({ error: () => {} });
    controlador.expectOne('/api/compras').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(localStorage.getItem('token')).toBeNull();
    expect(avisos.avisos()[0].texto).toContain('sesión venció');
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('con un 401 en el login no hace nada (lo maneja el formulario)', () => {
    localStorage.setItem('token', 'basura');
    http.post('/api/usuarios/login', {}).subscribe({ error: () => {} });
    controlador.expectOne('/api/usuarios/login').flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(avisos.avisos().length).toBe(0);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
