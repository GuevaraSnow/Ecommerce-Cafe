package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrecioTest {

    @Test
    void dosPreciosConELMismoValorDebenSerIguales(){
        Precio p1= new Precio(50000,"cop");
        Precio p2= new Precio(50000,"cop");
        assertEquals(p1,p2);

    }

    @Test
    void noDebeCrearPrecioNegativo(){
        assertThrows(ReglaDominioException.class, ()->{
            new Precio(-50000,"cop");
        });

    }

    private final Descuento descuento20 = new Descuento(20, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));

    @Test
    void aplicarDescuentoDevuelveElPrecioReducidoEnElPorcentaje() {
        Precio conDescuento = new Precio(50000, "COP").aplicarDescuento(descuento20);
        assertEquals(new Precio(40000, "COP"), conDescuento);
    }

    @Test
    void aplicarDescuentoNoModificaElPrecioOriginal() {
        Precio original = new Precio(50000, "COP");
        original.aplicarDescuento(descuento20);
        assertEquals(50000, original.monto());
    }

    @Test
    void aplicarDescuentoConservaLaMoneda() {
        assertEquals("USD", new Precio(100, "USD").aplicarDescuento(descuento20).moneda());
    }

    @Test
    void aplicarDescuentoRedondeaACentavos() {
        Descuento descuento33 = new Descuento(33, LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 31));
        assertEquals(6.7, new Precio(10, "USD").aplicarDescuento(descuento33).monto());
        assertEquals(6.71, new Precio(10.01, "USD").aplicarDescuento(descuento33).monto());
    }

    @Test
    void noSePuedeAplicarUnDescuentoNulo() {
        assertThrows(ReglaDominioException.class, () -> new Precio(50000, "COP").aplicarDescuento(null));
    }
}
