package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Las consultas de Compra solo delegan en el repositorio y nunca guardan. */
@ExtendWith(MockitoExtension.class)
class ConsultasDeCompraTest {

    @Mock
    private CompraRepositorio repositorio;

    @InjectMocks
    private ConsultarHistorialDeCompras consultarHistorial;

    @InjectMocks
    private ConsultarCompra consultarCompra;

    @Test
    void consultarHistorialDevuelveLasComprasDelComprador() {
        List<Compra> compras = List.of(Compra.abrirCarrito("compra-1", "comprador-1"));
        when(repositorio.buscarPorComprador("comprador-1")).thenReturn(compras);

        List<Compra> historial = consultarHistorial.ejecutar("comprador-1");

        assertEquals(compras, historial);
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void consultarCompraDevuelveLaCompra() {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        when(repositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        assertEquals(compra, consultarCompra.ejecutar("compra-1"));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void consultarUnaCompraQueNoExisteLanzaNoEncontrado() {
        when(repositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> consultarCompra.ejecutar("no-existe"));
        verify(repositorio, never()).guardar(any());
    }
}
