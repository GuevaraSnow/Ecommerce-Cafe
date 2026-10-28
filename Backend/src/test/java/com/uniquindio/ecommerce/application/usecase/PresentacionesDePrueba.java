package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.util.List;

/** Presentaciones ya publicadas para probar los casos de uso sin repetir el armado en cada prueba. */
final class PresentacionesDePrueba {

    private PresentacionesDePrueba() {
    }

    static Galeria galeria() {
        return new Galeria(List.of(new ImagenDePresentacion("http://img/1.png", true)));
    }

    /** Artículo de Merchandising con 20 unidades. */
    static Presentacion merchandising(String id) {
        return Presentacion.publicar(id, "vendedor-1", null, null, "Manilla", TipoDePresentacion.MERCHANDISING,
                new Precio(15000, "COP"), new Cantidad(20, "unidad"), galeria(), null,
                Rol.VENDEDOR_DERIVADOS, null, "semilla de café", "Manilla artesanal");
    }

    /** Café verde con 200 kg, proveniente del Lote LOT-2026-001. */
    static Presentacion cafeVerde(String id) {
        return Presentacion.publicar(id, "vendedor-1", "LOT-2026-001", null, "Café Verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(200, "kg"), galeria(), null,
                Rol.CAFICULTOR, null, null, null);
    }
}
