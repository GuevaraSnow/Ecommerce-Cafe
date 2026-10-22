package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearCarritoTest {

    @Mock
    private CompraRepositorio compraRepositorio;

    @InjectMocks
    private CrearCarrito crearCarrito;

    @Test
    void abreUnCarritoVacioPendienteYLoGuarda() {
        Compra carrito = crearCarrito.ejecutar("compra-1", "comprador-1");

        assertEquals("compra-1", carrito.getId());
        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertTrue(carrito.getDetalles().isEmpty());
        verify(compraRepositorio).guardar(carrito);
    }

    @Test
    void sinCompradorNoSeAbreElCarritoNiSeGuardaNada() {
        assertThrows(ReglaDominioException.class, () -> crearCarrito.ejecutar("compra-1", " "));

        verify(compraRepositorio, never()).guardar(any());
    }
}
