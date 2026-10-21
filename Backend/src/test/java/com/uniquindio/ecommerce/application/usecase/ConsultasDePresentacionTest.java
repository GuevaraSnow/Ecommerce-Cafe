package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
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
    void sugerirPideAlRepositorioLasCoincidenciasConElLimite() {
        List<Presentacion> coincidencias = List.of(merchandising("pres-1"));
        when(repositorio.sugerirDisponiblesPorTitulo("man", 5)).thenReturn(coincidencias);

        assertSame(coincidencias, sugerir.ejecutar("man", 5));
        verify(repositorio, never()).guardar(any());
    }
}
