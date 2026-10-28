package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.service.ServicioDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarLoteTest {

    @Test
    void registrarUnLoteLoDejaDisponibleEnElRepositorio() {
        LoteRepositorioMemoria repositorio = new LoteRepositorioMemoria();
        RegistrarLote registrarLote = new RegistrarLote(repositorio , new ServicioDeLote(repositorio));

        Lote lote = registrarLote.ejecutar(
                new Cantidad(200, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));

        assertTrue(repositorio.buscarPorCodigo(lote.getCodigoDeLote()).isPresent());
        assertEquals(lote, repositorio.buscarPorCodigo(lote.getCodigoDeLote()).get());
    }

    @Test
    void dosLotesRegistradosSeguidosDanCodigosConsecutivos(){
        LoteRepositorioMemoria repositorio = new LoteRepositorioMemoria();
        RegistrarLote registrarLote = new RegistrarLote(repositorio , new ServicioDeLote(repositorio));

        Lote lote = registrarLote.ejecutar(
                new Cantidad(200, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));

        Lote lote2 = registrarLote.ejecutar(
                new Cantidad(200,"kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));

        assertEquals(CodigoDeLote.crear(Year.now().getValue(), 1), lote.getCodigoDeLote());
        assertEquals(CodigoDeLote.crear(Year.now().getValue(), 2), lote2.getCodigoDeLote());

    }

    @Test
    void elSiguienteRegistroNoAlteraAlAnterior() {
        LoteRepositorioMemoria repositorio = new LoteRepositorioMemoria();
        RegistrarLote registrarLote = new RegistrarLote(repositorio , new ServicioDeLote(repositorio));

        Lote lote1 = registrarLote.ejecutar(
                new Cantidad(200,"kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));
        lote1.descontar(new Cantidad(100,"kg"));

        Lote lote2 = registrarLote.ejecutar(
                new Cantidad(200,"kg"),
                "finca-1",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA),
                new VariedadDeCafe("Caturra"));

        assertEquals(new Cantidad(100, "kg"), repositorio.buscarPorCodigo(lote1.getCodigoDeLote()).get().getCantidadDisponible());
        assertEquals(new Cantidad(200, "kg"), repositorio.buscarPorCodigo(lote2.getCodigoDeLote()).get().getCantidadDisponible());
    }
}
