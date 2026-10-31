package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.merchandising;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Las consultas solo delegan en el repositorio y nunca guardan. */
@ExtendWith(MockitoExtension.class)
class ConsultasDePresentacionTest {

    @Mock
    private PresentacionRepositorio repositorio;

    @InjectMocks
    private ConsultarPresentacion consultar;

    @InjectMocks
    private BuscarPresentaciones buscar;

    @InjectMocks
    private SugerirPresentaciones sugerir;

    @Test
    void consultarDevuelveLaPresentacion() {
        Presentacion presentacion = merchandising("pres-1");
        when(repositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertSame(presentacion, consultar.ejecutar("pres-1"));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void consultarUnaPresentacionInexistenteLanzaNoEncontrado() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> consultar.ejecutar("no-existe"));
    }

    @Test
    void buscarPideAlRepositorioLaPaginaConElFiltro() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones("man", null, null, null, null);
        Pagina<Presentacion> pagina = new Pagina<>(List.of(merchandising("pres-1")), 1, 10, 11);
        when(repositorio.buscarDisponibles(filtro, 1, 10)).thenReturn(pagina);

        assertSame(pagina, buscar.ejecutar(filtro, 1, 10));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void buscarConTodosLosFiltrosCombinadosSeLosPasaTalCualAlRepositorio() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones("cafe", TipoDePresentacion.CAFE_TOSTADO,
                "vendedor-1", 10000.0, 40000.0, 4.0);
        Pagina<Presentacion> pagina = new Pagina<>(List.of(merchandising("pres-1")), 0, 10, 1);
        when(repositorio.buscarDisponibles(filtro, 0, 10)).thenReturn(pagina);

        assertSame(pagina, buscar.ejecutar(filtro, 0, 10));
        verify(repositorio).buscarDisponibles(filtro, 0, 10);
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void buscarSinCoincidenciasDevuelveUnaPaginaVacia() {
        FiltroDePresentaciones filtro = new FiltroDePresentaciones("inexistente", null, null, null, null);
        when(repositorio.buscarDisponibles(filtro, 0, 10)).thenReturn(new Pagina<>(List.of(), 0, 10, 0));

        Pagina<Presentacion> resultado = buscar.ejecutar(filtro, 0, 10);

        assertTrue(resultado.contenido().isEmpty());
        assertEquals(0, resultado.totalElementos());
        assertEquals(0, resultado.totalPaginas());
    }

    @Test
    void buscarUnaPaginaFueraDeRangoDevuelveContenidoVacioYConservaElTotal() {
        FiltroDePresentaciones sinFiltros = new FiltroDePresentaciones(null, null, null, null, null);
        when(repositorio.buscarDisponibles(sinFiltros, 9, 10)).thenReturn(new Pagina<>(List.of(), 9, 10, 11));

        Pagina<Presentacion> resultado = buscar.ejecutar(sinFiltros, 9, 10);

        assertTrue(resultado.contenido().isEmpty());
        assertEquals(11, resultado.totalElementos());
        assertEquals(2, resultado.totalPaginas());
    }

    @Test
    void sugerirPideAlRepositorioLasCoincidenciasConElLimite() {
        List<Presentacion> coincidencias = List.of(merchandising("pres-1"));
        when(repositorio.sugerirDisponiblesPorTitulo("man", 5)).thenReturn(coincidencias);

        assertSame(coincidencias, sugerir.ejecutar("man", 5));
        verify(repositorio, never()).guardar(any());
    }
}
