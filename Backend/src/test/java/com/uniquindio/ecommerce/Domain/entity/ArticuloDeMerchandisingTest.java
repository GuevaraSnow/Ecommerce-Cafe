package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas propias de ArticuloDeMerchandising: material y
 * descripción obligatorios, y la unidad de Cantidad (regla 7-B: cuenta por
 * pieza, no por peso ni volumen). Llama directamente a
 * ArticuloDeMerchandising.publicar() (visibilidad de paquete).
 */
class ArticuloDeMerchandisingTest {

    private Galeria galeriaValida() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/manilla.png", true)));
    }

    @Test
    void publicarValidoAsignaMaterialYDescripcion() {
        ArticuloDeMerchandising articulo = ArticuloDeMerchandising.publicar("pres-1", "vendedor-1", "Manilla",
                TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), "semilla de café e hilo encerado", "Manilla artesanal");

        assertEquals("semilla de café e hilo encerado", articulo.getMaterial());
        assertEquals("Manilla artesanal", articulo.getDescripcion());
    }

    @Test
    void materialNuloFalla() {
        assertThrows(ReglaDominioException.class, () -> ArticuloDeMerchandising.publicar("pres-2", "vendedor-1",
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), null, "Manilla artesanal"));
    }

    @Test
    void materialEnBlancoFalla() {
        assertThrows(ReglaDominioException.class, () -> ArticuloDeMerchandising.publicar("pres-3", "vendedor-1",
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), "  ", "Manilla artesanal"));
    }

    @Test
    void descripcionNulaFalla() {
        assertThrows(ReglaDominioException.class, () -> ArticuloDeMerchandising.publicar("pres-4", "vendedor-1",
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), "semilla de café", null));
    }

    @Test
    void descripcionEnBlancoFalla() {
        assertThrows(ReglaDominioException.class, () -> ArticuloDeMerchandising.publicar("pres-5", "vendedor-1",
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "unidad"),
                galeriaValida(), "semilla de café", " "));
    }

    @Test
    void noSePuedeMedirEnKgComoElCafe() {
        assertThrows(ReglaDominioException.class, () -> ArticuloDeMerchandising.publicar("pres-6", "vendedor-1",
                "Manilla", TipoDePresentacion.MERCHANDISING, new Precio(15000, "COP"), new Cantidad(20, "kg"),
                galeriaValida(), "semilla de café", "Manilla artesanal"));
    }
}
