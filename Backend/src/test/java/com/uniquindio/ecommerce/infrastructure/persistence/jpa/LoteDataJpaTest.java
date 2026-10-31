package com.uniquindio.ecommerce.infrastructure.persistence.jpa;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper.LotePersistenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * El adaptador JPA de Lote contra la base real: guarda y recupera un Lote sin perder datos (también
 * con una Cosecha antigua) y calcula el número más alto del año. Los años de las pruebas del número
 * más alto (2090, 2091) no los usa ninguna fila de semilla.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({LoteRepositorioJpa.class, LotePersistenceMapper.class})
class LoteDataJpaTest {

    @Autowired
    private LoteRepositorio loteRepositorio;

    private Lote loteConCodigo(String codigo) {
        return Lote.registrar(new CodigoDeLote(codigo), new Cantidad(100, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.VERDE,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    @Test
    void guardaYRecuperaUnLoteConTodosSusCampos() {
        Lote lote = Lote.registrar(new CodigoDeLote("LOT-2026-046"),
                new Cantidad(100, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Honey,
                EstadoDelCafe.VERDE,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Castillo"));

        loteRepositorio.guardar(lote);

        Optional<Lote> loteRecuperado = loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2026-046"));

        assertTrue(loteRecuperado.isPresent());
        assertEquals(lote.getCodigoDeLote(), loteRecuperado.get().getCodigoDeLote());
        assertEquals(lote.getFincaID(), loteRecuperado.get().getFincaID());
        assertEquals(lote.getProcesoDeBeneficio(), loteRecuperado.get().getProcesoDeBeneficio());
        assertEquals(lote.getEstado(), loteRecuperado.get().getEstado());
        assertEquals(lote.getVariedad(), loteRecuperado.get().getVariedad());
        assertEquals(lote.getCosecha().anio(), loteRecuperado.get().getCosecha().anio());
        assertEquals(lote.getCosecha().temporada(), loteRecuperado.get().getCosecha().temporada());
        assertEquals(lote.getCantidadDisponible().valor(), loteRecuperado.get().getCantidadDisponible().valor());
        assertEquals(lote.getCantidadDisponible().unidad(), loteRecuperado.get().getCantidadDisponible().unidad());
    }

    @Test
    void guardarUnLoteConUnaCosechaAntiguaRecuperaElLoteConLaCosechaIntacta(){
        Cosecha cosecha = new Cosecha(Year.now().getValue() - 5, TemporadaDeCosecha.PRINCIPAL);
        Lote lote = Lote.registrar(new CodigoDeLote("LOT-2026-046"),
                new Cantidad(100, "kg"),
                "finca-1",
                ProcesoDeBeneficio.Honey,
                EstadoDelCafe.VERDE,
                cosecha,
                new VariedadDeCafe("Castillo"));
        loteRepositorio.guardar(lote);
        Optional<Lote> loteRecuperado = loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2026-046"));

        assertTrue(loteRecuperado.isPresent());
        assertEquals(cosecha, loteRecuperado.get().getCosecha());

    }

    


    @Test
    void soloTraeElNumeroMasAltoDelAnioPedido(){
        Lote lote1 = loteConCodigo("LOT-2090-005");
        Lote lote2 = loteConCodigo("LOT-2090-006");
        Lote lote3 = loteConCodigo("LOT-2091-008");
        loteRepositorio.guardar(lote1);
        loteRepositorio.guardar(lote2);
        loteRepositorio.guardar(lote3);

        assertEquals(6, loteRepositorio.obtenerNumeroMasAlto(2090));
        assertEquals(8, loteRepositorio.obtenerNumeroMasAlto(2091));
    }

    @Test
    void devuelve0CuandoNoHayLotes(){
        assertEquals(0,loteRepositorio.obtenerNumeroMasAlto(2090));
    }

    @Test
    void elNumeroMasAltoNoEsElUltimoLoteGuardado(){
        Lote lote1 = loteConCodigo("LOT-2090-006");
        Lote lote2 = loteConCodigo("LOT-2090-002");

        loteRepositorio.guardar(lote1);
        loteRepositorio.guardar(lote2);

        assertEquals(6, loteRepositorio.obtenerNumeroMasAlto(2090));
    }

    @Test
    void buscarNoEliminadoNoDevuelveUnLoteDadoDeBajaPeroBuscarPorCodigoSi(){
        Lote vigente = loteConCodigo("LOT-2090-030");
        Lote dadoDeBaja = loteConCodigo("LOT-2090-031");
        dadoDeBaja.eliminarLogicamente();
        loteRepositorio.guardar(vigente);
        loteRepositorio.guardar(dadoDeBaja);

        assertTrue(loteRepositorio.buscarNoEliminadoPorCodigo(vigente.getCodigoDeLote()).isPresent());
        assertTrue(loteRepositorio.buscarNoEliminadoPorCodigo(dadoDeBaja.getCodigoDeLote()).isEmpty());
        assertTrue(loteRepositorio.buscarPorCodigo(dadoDeBaja.getCodigoDeLote()).isPresent());
    }

    @Test
    void buscarUnCodigoQueNoExisteDevuelveVacio(){
        assertTrue(loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2090-999")).isEmpty());
    }

    @Test
    void guardarUnLoteEliminadoLogicamenteLoRecuperaEliminado(){
        Lote lote = loteConCodigo("LOT-2090-010");
        lote.eliminarLogicamente();
        loteRepositorio.guardar(lote);

        Lote recuperado = loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2090-010")).orElseThrow();

        assertTrue(recuperado.isEliminadoLogicamente());
    }

    @Test
    void guardarUnLoteYaExistenteActualizaSuCantidadDisponible(){
        Lote lote = loteConCodigo("LOT-2090-020");
        loteRepositorio.guardar(lote);

        lote.descontar(new Cantidad(30, "kg"));
        loteRepositorio.guardar(lote);

        Lote recuperado = loteRepositorio.buscarPorCodigo(new CodigoDeLote("LOT-2090-020")).orElseThrow();
        assertEquals(70, recuperado.getCantidadDisponible().valor());
    }

}
