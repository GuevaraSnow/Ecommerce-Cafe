package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsultasDeRepositorioTest {

    @Test
    void elFiltroRechazaUnPrecioMinimoMayorQueElMaximo() {
        assertThrows(ReglaDominioException.class,
                () -> new FiltroDePresentaciones(null, null, null, 50000.0, 1000.0));
    }

    @Test
    void elFiltroAceptaCriteriosAusentesYUnRangoDeUnSoloPrecio() {
        assertDoesNotThrow(() -> new FiltroDePresentaciones(null, null, null, null, null));
        assertDoesNotThrow(() -> new FiltroDePresentaciones(null, null, null, 1000.0, null));
        assertDoesNotThrow(() -> new FiltroDePresentaciones(null, null, null, 1000.0, 1000.0));
    }

    @Test
    void elFiltroRechazaUnaCalificacionMinimaFueraDeRango() {
        assertThrows(ReglaDominioException.class,
                () -> new FiltroDePresentaciones(null, null, null, null, null, 0.5));
        assertThrows(ReglaDominioException.class,
                () -> new FiltroDePresentaciones(null, null, null, null, null, 5.5));
    }

    @Test
    void elFiltroAceptaLosExtremosDeLaCalificacionMinima() {
        assertDoesNotThrow(() -> new FiltroDePresentaciones(null, null, null, null, null, 1.0));
        assertDoesNotThrow(() -> new FiltroDePresentaciones(null, null, null, null, null, 5.0));
    }

    @Test
    void laPaginaCalculaCuantasPaginasHay() {
        assertEquals(0, new Pagina<String>(List.of(), 0, 10, 0).totalPaginas());
        assertEquals(1, new Pagina<>(List.of("a"), 0, 10, 10).totalPaginas());
        assertEquals(2, new Pagina<>(List.of("a"), 0, 10, 11).totalPaginas());
    }

    @Test
    void mapearConvierteElContenidoYConservaLaPaginacion() {
        Pagina<Integer> convertida = new Pagina<>(List.of("a", "bb"), 1, 2, 7).mapear(String::length);

        assertEquals(List.of(1, 2), convertida.contenido());
        assertEquals(1, convertida.pagina());
        assertEquals(2, convertida.tamano());
        assertEquals(7, convertida.totalElementos());
    }
}
