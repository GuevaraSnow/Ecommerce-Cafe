package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Reglas de propiedad de CompraController, sin levantar Spring. */
@ExtendWith(MockitoExtension.class)
class AutorizacionDeCompraTest {

    @Mock
    private CompraRepositorio compraRepositorio;

    @Mock
    private PresentacionRepositorio presentacionRepositorio;

    @InjectMocks
    private AutorizacionDeCompra autorizacion;

    private Compra compra;

    @BeforeEach
    void preparar() {
        compra = Compra.abrirCarrito("compra-1", "usr-ana");
        compra.agregarPresentacion("pres-1", new Precio(28000, "COP"), new Cantidad(2, "kg"));
    }

    private Authentication usuario(String id) {
        Authentication usuario = mock(Authentication.class);
        when(usuario.getName()).thenReturn(id);
        return usuario;
    }

    private Presentacion presentacionDe(String vendedorId) {
        return Presentacion.publicar("pres-1", vendedorId, "LOT-2026-001", null, "Cafe verde",
                TipoDePresentacion.CAFE_VERDE, new Precio(28000, "COP"), new Cantidad(10, "kg"),
                new Galeria(List.of(new ImagenDePresentacion("https://img.cafe/1.jpg", true))),
                null, Rol.CAFICULTOR, null, null, null);
    }

    @Test
    void elCompradorQueAbrioElCarritoEsSuDueno() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        assertTrue(autorizacion.esDelComprador("compra-1", usuario("usr-ana")));
    }

    @Test
    void otroCompradorNoEsDuenoDelCarrito() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));

        assertFalse(autorizacion.esDelComprador("compra-1", usuario("usr-bruno")));
    }

    @Test
    void elVendedorDeUnaPresentacionDeLaCompraPuedeEnviarlaYEntregarla() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacionDe("usr-caficultor")));

        assertTrue(autorizacion.esVendedorDe("compra-1", usuario("usr-caficultor")));
    }

    @Test
    void unVendedorSinPresentacionesEnLaCompraNoPuedeEnviarla() {
        when(compraRepositorio.buscarPorId("compra-1")).thenReturn(Optional.of(compra));
        when(presentacionRepositorio.buscarPorId("pres-1")).thenReturn(Optional.of(presentacionDe("usr-caficultor")));

        assertFalse(autorizacion.esVendedorDe("compra-1", usuario("usr-tostador")));
    }

    @Test
    void siLaCompraNoExisteSeDejaPasarParaQueElCasoDeUsoResponda404() {
        when(compraRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());
        Authentication cualquiera = mock(Authentication.class);

        assertTrue(autorizacion.esDelComprador("no-existe", cualquiera));
        assertTrue(autorizacion.esVendedorDe("no-existe", cualquiera));
    }
}
