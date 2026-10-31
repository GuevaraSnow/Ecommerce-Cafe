package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.application.dto.response.ResenaResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResenaMapperTest {

    private final ResenaMapper mapper = new ResenaMapper();

    private static final LocalDateTime FECHA = LocalDateTime.of(2026, 10, 20, 18, 0);

    private Resena resena() {
        return Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1", new Calificacion(4),
                new TextoDeResena("Notas a panela"), FECHA);
    }

    @Test
    void traduceLaCalificacionYElTextoAlDominio() {
        assertEquals(new Calificacion(5), mapper.toCalificacion(5));
        assertEquals(new TextoDeResena("Excelente"), mapper.toTexto("Excelente"));
        assertNull(mapper.toCalificacion(null));
        assertNull(mapper.toTexto(null));
    }

    @Test
    void unaCalificacionFueraDeRangoLaRechazaElDominio() {
        assertThrows(ReglaDominioException.class, () -> mapper.toCalificacion(6));
    }

    @Test
    void unaResenaSinResponderSeMapeaConRespuestaNula() {
        ResenaResponse response = mapper.toResponse(resena());

        assertEquals("resena-1", response.id());
        assertEquals("comprador-1", response.compradorId());
        assertEquals("pres-1", response.presentacionId());
        assertEquals("vendedor-1", response.vendedorId());
        assertEquals(4, response.calificacion());
        assertEquals("Notas a panela", response.texto());
        assertEquals(FECHA, response.fecha());
        assertNull(response.respuesta());
    }

    @Test
    void unaResenaRespondidaLlevaElTextoYLaFechaDeLaRespuesta() {
        Resena respondida = resena();
        LocalDateTime respuesta = FECHA.plusHours(2);
        respondida.responder("vendedor-1", new TextoDeResena("Gracias por tu compra"), respuesta);

        ResenaResponse response = mapper.toResponse(respondida);

        assertEquals("Gracias por tu compra", response.respuesta().texto());
        assertEquals(respuesta, response.respuesta().fecha());
    }

    @Test
    void toResponsesConservaElOrden() {
        Resena otra = Resena.dejar("resena-2", "comprador-2", "pres-1", "vendedor-1", new Calificacion(2),
                new TextoDeResena("Llego tarde"), FECHA.minusDays(1));

        List<ResenaResponse> responses = mapper.toResponses(List.of(resena(), otra));

        assertEquals(List.of("resena-1", "resena-2"), responses.stream().map(ResenaResponse::id).toList());
    }
}
