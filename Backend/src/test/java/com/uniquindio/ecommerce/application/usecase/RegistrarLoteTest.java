package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrarLoteTest {

    @Test
    void registrarUnLoteLoDejaDisponibleEnElRepositorio() {
        LoteRepositorioMemoria repositorio = new LoteRepositorioMemoria();
        RegistrarLote registrarLote = new RegistrarLote(repositorio);
        CodigoDeLote codigo = new CodigoDeLote("LOT-2024-045");

        Lote lote = registrarLote.ejecutar(
                codigo,
                new Cantidad(200, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));

        assertTrue(repositorio.buscarPorCodigo(codigo).isPresent());
        assertEquals(lote, repositorio.buscarPorCodigo(codigo).get());
    }
}
