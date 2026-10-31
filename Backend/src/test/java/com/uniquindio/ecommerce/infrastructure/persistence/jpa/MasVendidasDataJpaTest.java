package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionVendida;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.PresentacionPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El ranking de ventas contra la base real, con las Compras de ejemplo (data-compra.sql). Solo cuentan las
 * Compras confirmadas, enviadas y entregadas:
 * <pre>
 * pres-cafe-1: compra-001 (2 x 28000, entregada) y compra-003 (3 x 28000, confirmada) -> 2 compras, 140000
 * pres-cafe-3: compra-004 (1 x 45000, enviada) y compra-007 (1 x 36000, entregada)     -> 2 compras,  81000
 * pres-cafe-2: compra-007 (1 x 32000, entregada)                                        -> 1 compra,   32000
 * pres-taza-1: compra-001 (1 x 15000, entregada)                                        -> 1 compra,   15000
 * </pre>
 * Quedan fuera compra-002 (pendiente), compra-005 (cancelada) y compra-006 (reembolsada).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PresentacionRepositorioJpa.class, PresentacionPersistenceMapper.class})
class MasVendidasDataJpaTest {

    @Autowired
    private PresentacionRepositorio repositorio;

    private List<String> ids(List<PresentacionVendida> ranking) {
        return ranking.stream().map(PresentacionVendida::presentacionId).toList();
    }

    @Test
    void ordenaPorNumeroDeComprasYDesempataPorIngresos() {
        List<PresentacionVendida> ranking = repositorio.masVendidas(10);

        assertEquals(List.of("pres-cafe-1", "pres-cafe-3", "pres-cafe-2", "pres-taza-1"), ids(ranking));
        assertEquals(new PresentacionVendida("pres-cafe-1", "Cafe tostado Huila", 2, 140000.0), ranking.get(0));
        assertEquals(new PresentacionVendida("pres-cafe-3", "Cafe tostado Narino", 2, 81000.0), ranking.get(1));
        assertEquals(new PresentacionVendida("pres-cafe-2", "Cafe verde Quindio", 1, 32000.0), ranking.get(2));
        assertEquals(new PresentacionVendida("pres-taza-1", "Taza de ceramica", 1, 15000.0), ranking.get(3));
    }

    @Test
    void noCuentaLasComprasCanceladasPendientesNiReembolsadas() {
        List<PresentacionVendida> ranking = repositorio.masVendidas(10);

        // pres-cafe-2 está en 4 compras, pero solo compra-007 es una venta; pres-cafe-3 está en 3 y solo cuentan 2.
        assertEquals(1, ranking.stream().filter(v -> v.presentacionId().equals("pres-cafe-2")).findFirst().orElseThrow().compras());
        assertEquals(2, ranking.stream().filter(v -> v.presentacionId().equals("pres-cafe-3")).findFirst().orElseThrow().compras());
    }

    @Test
    void respetaElLimite() {
        assertEquals(List.of("pres-cafe-1"), ids(repositorio.masVendidas(1)));
        assertEquals(List.of("pres-cafe-1", "pres-cafe-3"), ids(repositorio.masVendidas(2)));
        assertEquals(4, repositorio.masVendidas(50).size());
    }
}
