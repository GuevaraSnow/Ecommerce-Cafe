package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

public class ServicioDeLoteTest {
    private final LoteRepositorioMemoria loteRepositorioMemoria = new LoteRepositorioMemoria();
    private final ServicioDeLote servicioDeLote = new ServicioDeLote(loteRepositorioMemoria);

    private Lote loteConCodigo(String codigo) {
        return Lote.registrar(new CodigoDeLote(codigo), new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void elCodigoNuevoEsElSiguienteNumeroDelAnio(){
        loteRepositorioMemoria.guardar(loteConCodigo("LOT-2026-007"));

        Lote nuevo = servicioDeLote.registrar(2026, new Cantidad(200, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));

        assertEquals(8, nuevo.getCodigoDeLote().numero());
    }

}
