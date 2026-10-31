package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

/** El repositorio en memoria guarda Lotes por código y calcula el número más alto de cada año. */
class LoteRepositorioMemoriaTest {

    private final LoteRepositorioMemoria repositorioMemoria = new LoteRepositorioMemoria();

    private Lote loteConCodigo(String codigo) {
        return Lote.registrar(new CodigoDeLote(codigo), new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void buscarNoEliminadoNoDevuelveUnLoteDadoDeBaja(){
        Lote vigente = loteConCodigo("LOT-2090-001");
        Lote dadoDeBaja = loteConCodigo("LOT-2090-002");
        dadoDeBaja.eliminarLogicamente();
        repositorioMemoria.guardar(vigente);
        repositorioMemoria.guardar(dadoDeBaja);

        assertTrue(repositorioMemoria.buscarNoEliminadoPorCodigo(vigente.getCodigoDeLote()).isPresent());
        assertTrue(repositorioMemoria.buscarNoEliminadoPorCodigo(dadoDeBaja.getCodigoDeLote()).isEmpty());
        assertTrue(repositorioMemoria.buscarPorCodigo(dadoDeBaja.getCodigoDeLote()).isPresent());
    }

    @Test
    void devuelve0CuandoNoHayLotes(){
        assertEquals(0,repositorioMemoria.obtenerNumeroMasAlto(2025));
    }

    @Test
    void soloTraeElNumeroMasAltoDelAnioPedido(){
        Lote lote1 = loteConCodigo("LOT-2026-005");
        Lote lote2 = loteConCodigo("LOT-2026-006");
        Lote lote3 = loteConCodigo("LOT-2025-008");
        repositorioMemoria.guardar(lote1);
        repositorioMemoria.guardar(lote2);
        repositorioMemoria.guardar(lote3);

        assertEquals(6,repositorioMemoria.obtenerNumeroMasAlto(2026));

    }

    @Test
    void elNumeroMasAltoNoEsElUltimoLoteGuardado(){
        Lote lote1 = loteConCodigo("LOT-2026-006");
        Lote lote2 = loteConCodigo("LOT-2026-002");

        repositorioMemoria.guardar(lote1);
        repositorioMemoria.guardar(lote2);

        assertEquals(6, repositorioMemoria.obtenerNumeroMasAlto(2026));
    }

}
