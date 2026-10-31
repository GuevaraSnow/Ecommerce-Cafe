package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.FincaRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import com.uniquindio.ecommerce.infrastructure.persistence.TransformacionRepositorioMemoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Year;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarFichaOrigenTest {

    private TransformacionRepositorioMemoria transformacionRepositorio;
    private LoteRepositorioMemoria loteRepositorio;
    private FincaRepositorioMemoria fincaRepositorio;
    private ConsultarFichaOrigen consultarFichaOrigen;

    @BeforeEach
    void setUp() {
        transformacionRepositorio = new TransformacionRepositorioMemoria();
        loteRepositorio = new LoteRepositorioMemoria();
        fincaRepositorio = new FincaRepositorioMemoria();

        consultarFichaOrigen = new ConsultarFichaOrigen(
                transformacionRepositorio,
                loteRepositorio,
                fincaRepositorio
        );
    }

    @Test
    void consultarFichaOrigenRetornaDatosRealesDeFincaYLote() {
        // 1. Guardar Finca Real
        Finca finca = Finca.registrar(
                "finca-101",
                "usr-caficultor",
                "Hacienda El Paraíso",
                new Ubicacion("Quindio", "Calarca"),
                new Altitud(1750)
        );
        fincaRepositorio.guardar(finca);

        // 2. Guardar Lote Real vinculado a la Finca
        Lote lote = Lote.registrar(
                new CodigoDeLote("LOT-2026-999"),
                new Cantidad(120.0, "kg"),
                "finca-101",
                ProcesoDeBeneficio.Lavado,
                EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Geisha")
        );
        loteRepositorio.guardar(lote);

        // 3. Guardar Transformación Real vinculada al Lote
        Transformacion transformacion = Transformacion.registrar(
                "TRANS-999",
                "LOT-2026-999",
                new Cantidad(60.0, "kg"),
                new PorcentajeDeMerma(0.18),
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );
        transformacionRepositorio.guardar(transformacion);

        // ACT
        FichaDeOrigen ficha = consultarFichaOrigen.ejecutar("TRANS-999");

        // ASSERT
        assertNotNull(ficha);
        assertEquals("Hacienda El Paraíso", ficha.fincaNombre());
        assertEquals(TemporadaDeCosecha.PRINCIPAL, ficha.cosecha().temporada());
        assertEquals(Year.now().getValue(), ficha.cosecha().anio());
        assertEquals(1, ficha.lotesOrigen().size());
        assertEquals("LOT-2026-999", ficha.lotesOrigen().get(0).getCodigoDeLote().valor());
        assertEquals("TRANS-999", ficha.transformacionId());
        assertEquals(PerfilTueste.MEDIO, ficha.perfilTuesteResultante());
    }

    @Test
    void unaTransformacionInexistenteLanzaNoEncontrado() {
        assertThrows(NoSuchElementException.class, () -> consultarFichaOrigen.ejecutar("no-existe"));
    }

    @Test
    void siElLoteDeLaTransformacionNoExisteLanzaNoEncontrado() {
        transformacionRepositorio.guardar(transformacionDelLote("LOT-2026-888"));

        assertThrows(NoSuchElementException.class, () -> consultarFichaOrigen.ejecutar("TRANS-888"));
    }

    @Test
    void siLaFincaDelLoteNoExisteLanzaNoEncontrado() {
        loteRepositorio.guardar(Lote.registrar(new CodigoDeLote("LOT-2026-888"), new Cantidad(10.0, "kg"),
                "finca-fantasma", ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL), new VariedadDeCafe("Geisha")));
        transformacionRepositorio.guardar(transformacionDelLote("LOT-2026-888"));

        assertThrows(NoSuchElementException.class, () -> consultarFichaOrigen.ejecutar("TRANS-888"));
    }

    private Transformacion transformacionDelLote(String loteCodigo) {
        return Transformacion.registrar("TRANS-888", loteCodigo, new Cantidad(5.0, "kg"), new PorcentajeDeMerma(0.18),
                "tostador-1", LocalDate.now(), PerfilTueste.MEDIO);
    }
}
