package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * existeCompraEntregada: solo cuenta una Compra ENTREGADA de ese Comprador que incluya esa Presentación.
 * buscarPorComprador: solo las Compras de ese Comprador, de la más reciente a la más antigua.
 */
class CompraRepositorioMemoriaTest {

    private final CompraRepositorioMemoria repositorio = new CompraRepositorioMemoria();

    private Compra compraCon(String compraId, String compradorId, String presentacionId) {
        Compra compra = Compra.abrirCarrito(compraId, compradorId);
        compra.agregarPresentacion(presentacionId, new Precio(50000, "COP"), new Cantidad(2, "kg"));
        return compra;
    }

    private Compra entregada(String compraId, String compradorId, String presentacionId) {
        Compra compra = compraCon(compraId, compradorId, presentacionId);
        compra.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        compra.enviar();
        compra.entregar(LocalDateTime.of(2026, 10, 20, 15, 30));
        return compra;
    }

    @Test
    void existeCompraEntregadaCuandoElCompradorRecibioEsaPresentacion() {
        repositorio.guardar(entregada("compra-1", "comprador-1", "pres-1"));

        assertTrue(repositorio.existeCompraEntregada("comprador-1", "pres-1"));
    }

    @Test
    void noCuentaUnaCompraQueAunNoSeEntrega() {
        Compra enviada = compraCon("compra-1", "comprador-1", "pres-1");
        enviada.confirmar(new DireccionDeEnvio("Montenegro", "Calle 10 # 5-20", "Carlos Perez"));
        enviada.enviar();
        repositorio.guardar(enviada);

        assertFalse(repositorio.existeCompraEntregada("comprador-1", "pres-1"));
    }

    @Test
    void noCuentaLaCompraEntregadaDeOtroCompradorNiDeOtraPresentacion() {
        repositorio.guardar(entregada("compra-1", "comprador-2", "pres-1"));
        repositorio.guardar(entregada("compra-2", "comprador-1", "pres-2"));

        assertFalse(repositorio.existeCompraEntregada("comprador-1", "pres-1"));
    }

    private Compra delDia(String compraId, String compradorId, LocalDate fecha) {
        return Compra.reconstruir(compraId, compradorId, fecha, null, List.of(),
                EstadoDeCompra.PENDIENTE, null);
    }

    @Test
    void buscarPorCompradorDevuelveSoloSusComprasDeLaMasRecienteALaMasAntigua() {
        repositorio.guardar(delDia("compra-vieja", "comprador-1", LocalDate.of(2026, 10, 1)));
        repositorio.guardar(delDia("compra-nueva", "comprador-1", LocalDate.of(2026, 10, 20)));
        repositorio.guardar(delDia("compra-ajena", "comprador-2", LocalDate.of(2026, 10, 15)));

        List<Compra> historial = repositorio.buscarPorComprador("comprador-1");

        assertEquals(List.of("compra-nueva", "compra-vieja"), historial.stream().map(Compra::getId).toList());
    }

    @Test
    void buscarPorCompradorSinComprasDevuelveUnaListaVacia() {
        assertTrue(repositorio.buscarPorComprador("comprador-sin-compras").isEmpty());
    }
}
