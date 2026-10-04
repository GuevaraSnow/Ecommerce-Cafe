package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest.CosechaRequest;
import com.uniquindio.ecommerce.application.dto.response.LoteResponse;
import com.uniquindio.ecommerce.application.usecase.RegistrarLote;
import com.uniquindio.ecommerce.infrastructure.persistence.LoteRepositorioMemoria;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

class LoteMapperTest {

    private final LoteMapper mapper = new LoteMapper();

    private RegistrarLoteRequest request(double valor, EstadoDelCafe estado) {
        return new RegistrarLoteRequest("LOT-2026-045", new CantidadRequest(valor, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, estado,
                new CosechaRequest(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA), "Caturra");
    }

    /** Arma los parámetros de RegistrarLote.ejecutar con el mapper y ejecuta el caso de uso. */
    private Lote registrar(RegistrarLoteRequest r) {
        return new RegistrarLote(new LoteRepositorioMemoria()).ejecutar(
                mapper.toCodigoDeLote(r.codigo()),
                mapper.toCantidad(r.cantidad()),
                r.fincaId(),
                r.procesoDeBeneficio(),
                r.estado(),
                mapper.toCosecha(r.cosecha()),
                mapper.toVariedad(r.variedad()));
    }

    @Test
    void unRequestValidoProduceElLoteEsperado() {
        Lote lote = registrar(request(200, EstadoDelCafe.PERGAMINO));

        assertEquals(new CodigoDeLote("LOT-2026-045"), lote.getCodigoDeLote());
        assertEquals(new Cantidad(200, "kg"), lote.getCantidadDisponible());
        assertEquals("finca-1", lote.getFincaID());
        assertEquals(ProcesoDeBeneficio.Lavado, lote.getProcesoDeBeneficio());
        assertEquals(EstadoDelCafe.PERGAMINO, lote.getEstado());
        assertEquals(new Cosecha(Year.now().getValue(), TemporadaDeCosecha.TRAVIESA), lote.getCosecha());
        assertEquals(new VariedadDeCafe("Caturra"), lote.getVariedad());
    }

    @Test
    void unLoteProduceUnResponseConTodosSusCampos() {
        Lote lote = registrar(request(200, EstadoDelCafe.PERGAMINO));

        LoteResponse response = mapper.toResponse(lote);

        assertEquals("LOT-2026-045", response.codigo());
        assertEquals("finca-1", response.fincaId());
        assertEquals(200, response.cantidadDisponible().valor());
        assertEquals("kg", response.cantidadDisponible().unidad());
        assertEquals("PERGAMINO", response.estado());
        assertEquals("Lavado", response.procesoDeBeneficio());
        assertEquals("Caturra", response.variedad());
        assertEquals(Year.now().getValue(), response.cosecha().anio());
        assertEquals("TRAVIESA", response.cosecha().temporada());
        assertFalse(response.eliminadoLogicamente());
    }

    @Test
    void elResponseReflejaQueElLoteFueEliminadoLogicamente() {
        Lote lote = registrar(request(200, EstadoDelCafe.VERDE));
        lote.eliminarLogicamente();

        assertTrue(mapper.toResponse(lote).eliminadoLogicamente());
    }

    @Test
    void unaCantidadMenorOIgualACeroPropagaReglaDominioException() {
        assertThrows(ReglaDominioException.class, () -> registrar(request(0, EstadoDelCafe.VERDE)));
        assertThrows(ReglaDominioException.class, () -> registrar(request(-5, EstadoDelCafe.VERDE)));
    }

    @Test
    void elEstadoTostadoPropagaReglaDominioException() {
        assertThrows(ReglaDominioException.class, () -> registrar(request(200, EstadoDelCafe.TOSTADO)));
    }

    @Test
    void losValoresInvalidosEnElMapperNoSeTragan() {
        assertThrows(ReglaDominioException.class, () -> mapper.toCantidad(new CantidadRequest(10.0, "toneladas")));
        assertThrows(ReglaDominioException.class, () -> mapper.toCodigoDeLote("ABC"));
        assertThrows(ReglaDominioException.class,
                () -> mapper.toCosecha(new CosechaRequest(Year.now().getValue() - 3, TemporadaDeCosecha.PRINCIPAL)));
    }
}
