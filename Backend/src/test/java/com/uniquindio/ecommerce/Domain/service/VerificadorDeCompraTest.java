package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.infrastructure.persistence.CompraRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.ResenaRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VerificadorDeCompraTest {

    private final CompraRepositorioMemoria compras = new CompraRepositorioMemoria();
    private final ResenaRepositorioMemoria resenas = new ResenaRepositorioMemoria();
    private final VerificadorDeCompra verificador = new VerificadorDeCompra(compras, resenas);

    private Compra compraHasta(String estado) {
        Compra compra = Compra.abrirCarrito("compra-1", "comprador-1");
        compra.agregarPresentacion("pres-1", new Precio(50000, "COP"), new Cantidad(2, "kg"));
        if (!estado.equals("PENDIENTE")) {
            compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
            compra.enviar();
        }
        if (estado.equals("ENTREGADA")) {
            compra.entregar(LocalDateTime.of(2026, 10, 19, 15, 0));
        }
        compras.guardar(compra);
        return compra;
    }

    @Test
    void quienRecibioLaPresentacionYNoLaHaResenadoPuedeResenarla() {
        compraHasta("ENTREGADA");

        assertDoesNotThrow(() -> verificador.verificarQuePuedeResenar("comprador-1", "pres-1"));
    }

    @Test
    void quienNuncaComproLaPresentacionNoPuedeResenarla() {
        assertThrows(ReglaDominioException.class, () -> verificador.verificarQuePuedeResenar("comprador-1", "pres-1"));
    }

    @Test
    void unaCompraQueAunNoSeEntregaNoPermiteResenar() {
        compraHasta("PENDIENTE");
        assertThrows(ReglaDominioException.class, () -> verificador.verificarQuePuedeResenar("comprador-1", "pres-1"));

        compraHasta("ENVIADA");
        assertThrows(ReglaDominioException.class, () -> verificador.verificarQuePuedeResenar("comprador-1", "pres-1"));
    }

    @Test
    void otroCompradorNoPuedeResenarConLaCompraDeAlguienMas() {
        compraHasta("ENTREGADA");

        assertThrows(ReglaDominioException.class, () -> verificador.verificarQuePuedeResenar("comprador-2", "pres-1"));
    }

    @Test
    void quienYaResenoLaPresentacionNoPuedeResenarlaOtraVez() {
        compraHasta("ENTREGADA");
        resenas.guardar(Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1", new Calificacion(5),
                new TextoDeResena("Excelente"), LocalDateTime.of(2026, 10, 20, 18, 0)));

        assertThrows(ReglaDominioException.class, () -> verificador.verificarQuePuedeResenar("comprador-1", "pres-1"));
    }
}
