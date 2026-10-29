package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.RespuestaAResena;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.ResenaEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** El mapper no pierde nada en el ida y vuelta dominio → tabla → dominio. Sin Spring. */
class ResenaPersistenceMapperTest {

    private static final LocalDateTime ESCRITA = LocalDateTime.of(2026, 10, 20, 18, 0);
    private static final LocalDateTime RESPONDIDA = LocalDateTime.of(2026, 10, 21, 9, 30);

    private final ResenaPersistenceMapper mapper = new ResenaPersistenceMapper();

    private Resena resena() {
        return Resena.dejar("resena-1", "comprador-1", "pres-1", "vendedor-1", new Calificacion(4),
                new TextoDeResena("Notas a panela y buen cuerpo"), ESCRITA);
    }

    @Test
    void unaResenaRespondidaConservaCalificacionTextoYRespuesta() {
        Resena original = resena();
        original.responder("vendedor-1", new TextoDeResena("Gracias por tu compra"), RESPONDIDA);

        Resena reconstruida = mapper.toDomain(mapper.toEntity(original));

        assertEquals("resena-1", reconstruida.getId());
        assertEquals("comprador-1", reconstruida.getCompradorId());
        assertEquals("pres-1", reconstruida.getPresentacionId());
        assertEquals("vendedor-1", reconstruida.getVendedorId());
        assertEquals(new Calificacion(4), reconstruida.getCalificacion());
        assertEquals(new TextoDeResena("Notas a panela y buen cuerpo"), reconstruida.getTexto());
        assertEquals(ESCRITA, reconstruida.getFecha());
        assertEquals(new RespuestaAResena(new TextoDeResena("Gracias por tu compra"), RESPONDIDA),
                reconstruida.getRespuesta().orElseThrow());
    }

    @Test
    void unaResenaSinRespuestaSeGuardaSinRespuestaYElVendedorPuedeResponderDespues() {
        ResenaEntity entity = mapper.toEntity(resena());
        Resena reconstruida = mapper.toDomain(entity);

        assertNull(entity.getRespuesta());
        assertTrue(reconstruida.getRespuesta().isEmpty());
        reconstruida.responder("vendedor-1", new TextoDeResena("Gracias"), RESPONDIDA);
        assertTrue(reconstruida.getRespuesta().isPresent());
    }

    @Test
    void laCalificacionSeGuardaComoUnEnteroEnLaTabla() {
        assertEquals(4, mapper.toEntity(resena()).getCalificacion());
    }
}
