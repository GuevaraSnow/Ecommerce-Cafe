package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarcarEnviadaTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");

    @Mock
    private CompraRepositorio compraRepositorio;

    @InjectMocks
    private MarcarEnviada marcarEnviada;

    private Compra compra;

    @BeforeEach
    void preparar() {
        compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
    }

    @Test
    void unaCompraConfirmadaQuedaEnviadaYSeGuarda() {
        compra.confirmar(DIRECCION);
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        marcarEnviada.ejecutar("compra-1");

        assertEquals(EstadoDeCompra.ENVIADA, compra.getEstado());
        verify(compraRepositorio).guardar(compra);
    }

    @Test
    void unCarritoSinConfirmarNoSePuedeEnviarYNoSeGuardaNada() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        assertThrows(ReglaDominioException.class, () -> marcarEnviada.ejecutar("compra-1"));

        assertEquals(EstadoDeCompra.PENDIENTE, compra.getEstado());
        verify(compraRepositorio, never()).guardar(any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> marcarEnviada.ejecutar("no-existe"));

        verify(compraRepositorio, never()).guardar(any());
    }
}
