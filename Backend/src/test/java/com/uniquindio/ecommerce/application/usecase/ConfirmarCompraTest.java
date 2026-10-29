package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.NotificadorDeCorreo;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.uniquindio.ecommerce.application.usecase.PresentacionesDePrueba.cafeVerde;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Usa el ServicioDeCompra real: así la prueba cubre el descuento de stock y no solo la delegación. */
@ExtendWith(MockitoExtension.class)
class ConfirmarCompraTest {

    private static final DireccionDeEnvio DIRECCION = new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez");
    private static final Email EMAIL = new Email("carlos@uqvirtual.edu.co");

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    @Mock
    private UsuarioRepositorio usuarioRepositorio;

    @Mock
    private NotificadorDeCorreo notificadorDeCorreo;

    private ConfirmarCompra confirmarCompra;
    private Presentacion presentacion;
    private Compra carrito;
    private Usuario comprador;

    @BeforeEach
    void preparar() {
        confirmarCompra = new ConfirmarCompra(compraRepositorio, new ServicioDeCompra(presentacionRepositorio),
                usuarioRepositorio, notificadorDeCorreo);
        presentacion = cafeVerde("pres-1");
        carrito = Compra.abrirCarrito("compra-1", "comprador-1");
        carrito.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(50, "kg"));
        comprador = Usuario.registrar("comprador-1", "Carlos Perez", EMAIL, new Contrasena("Password123!"),
                new Telefono("3001234567"), new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), Rol.COMPRADOR);
    }

    private void conCarritoYComprador() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(usuarioRepositorio.buscarPorId("comprador-1")).thenReturn(Optional.of(comprador));
    }

    @Test
    void confirmaElCarritoDescuentaElStockGuardaYEnviaElCorreoAlComprador() {
        conCarritoYComprador();
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        confirmarCompra.ejecutar("compra-1", DIRECCION);

        assertEquals(EstadoDeCompra.CONFIRMADA, carrito.getEstado());
        assertEquals(DIRECCION, carrito.getDireccionEnvio().orElseThrow());
        assertEquals(new Cantidad(150, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio).guardar(carrito);
        verify(presentacionRepositorio).guardar(presentacion);
        verify(notificadorDeCorreo).enviarConfirmacionDeCompra(EMAIL, carrito);
    }

    @Test
    void siOtroCompradorDejoElStockCortoNoSeConfirmaNiSeGuardaNiSeEnviaCorreo() {
        presentacion.descontarCantidad(new Cantidad(180, "kg"));
        conCarritoYComprador();
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));

        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar("compra-1", DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(20, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
        verify(presentacionRepositorio, never()).guardar(any());
        verify(notificadorDeCorreo, never()).enviarConfirmacionDeCompra(any(), any());
    }

    @Test
    void noSePuedeConfirmarDosVecesNiSeEnviaUnSegundoCorreo() {
        conCarritoYComprador();
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacion));
        confirmarCompra.ejecutar("compra-1", DIRECCION);
        clearInvocations(compraRepositorio, presentacionRepositorio, notificadorDeCorreo);

        assertThrows(ReglaDominioException.class, () -> confirmarCompra.ejecutar("compra-1", DIRECCION));

        assertEquals(new Cantidad(150, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
        verify(notificadorDeCorreo, never()).enviarConfirmacionDeCompra(any(), any());
    }

    @Test
    void siElCompradorNoExisteNoSeConfirmaNiSeTocaElStock() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(carrito));
        when(usuarioRepositorio.buscarPorId("comprador-1")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> confirmarCompra.ejecutar("compra-1", DIRECCION));

        assertEquals(EstadoDeCompra.PENDIENTE, carrito.getEstado());
        assertEquals(new Cantidad(200, "kg"), presentacion.getCantidadDisponible());
        verify(compraRepositorio, never()).guardar(any());
        verify(notificadorDeCorreo, never()).enviarConfirmacionDeCompra(any(), any());
    }

    @Test
    void lanzaNoEncontradoSiLaCompraNoExisteYNoGuardaNada() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> confirmarCompra.ejecutar("no-existe", DIRECCION));

        verify(compraRepositorio, never()).guardar(any());
        verify(notificadorDeCorreo, never()).enviarConfirmacionDeCompra(any(), any());
    }
}
