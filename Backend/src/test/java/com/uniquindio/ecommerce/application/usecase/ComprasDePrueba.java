package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

import java.time.LocalDateTime;

/** Compras ya armadas para probar los casos de uso de reembolso sin repetir el recorrido en cada prueba. */
final class ComprasDePrueba {

    private ComprasDePrueba() {
    }

    /** Compra de "comprador-1" con 2 kg de "pres-1", entregada en el momento indicado. */
    static Compra entregada(String compraId, LocalDateTime entrega) {
        Compra compra = Compra.abrirCarrito(compraId, "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        compra.enviar();
        compra.entregar(entrega);
        return compra;
    }
}
