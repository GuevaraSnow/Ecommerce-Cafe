package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.RespuestaAResena;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Opinión de un Comprador sobre una Presentación que adquirió: una Calificación de 1 a 5
 * estrellas y un texto. Guarda el vendedor dueño de la Presentación porque solo él puede
 * responderla, y una sola vez. El Comprador, la Presentación y el vendedor se referencian por id.
 */
public class Resena {

    private final String id;
    private final String compradorId;
    private final String presentacionId;
    private final String vendedorId;
    private final Calificacion calificacion;
    private final TextoDeResena texto;
    private final LocalDateTime fecha;
    private RespuestaAResena respuesta;

    private Resena(String id, String compradorId, String presentacionId, String vendedorId,
                   Calificacion calificacion, TextoDeResena texto, LocalDateTime fecha,
                   RespuestaAResena respuesta) {
        this.id = id;
        this.compradorId = compradorId;
        this.presentacionId = presentacionId;
        this.vendedorId = vendedorId;
        this.calificacion = calificacion;
        this.texto = texto;
        this.fecha = fecha;
        this.respuesta = respuesta;
    }

    /**
     * Deja la Reseña de un Comprador sobre una Presentación. Que la haya adquirido lo verifica
     * VerificadorDeCompra antes de llamar aquí.
     *
     * @throws ReglaDominioException si falta algún dato
     */
    public static Resena dejar(String id, String compradorId, String presentacionId, String vendedorId,
                               Calificacion calificacion, TextoDeResena texto, LocalDateTime fecha) {
        validarTexto(id, "La resena debe tener un identificador");
        validarTexto(compradorId, "La resena debe tener el comprador que la escribe");
        validarTexto(presentacionId, "La resena debe ser sobre una presentacion");
        validarTexto(vendedorId, "La resena debe conocer al vendedor de la presentacion");
        if (calificacion == null) {
            throw new ReglaDominioException("La resena debe tener una calificacion");
        }
        if (texto == null) {
            throw new ReglaDominioException("La resena debe tener un texto");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La resena debe registrar la fecha en que se escribio");
        }
        return new Resena(id, compradorId, presentacionId, vendedorId, calificacion, texto, fecha, null);
    }

    /**
     * Revive una Reseña ya guardada, tal como quedó. No valida: solo la usa la persistencia.
     */
    public static Resena reconstruir(String id, String compradorId, String presentacionId, String vendedorId,
                                     Calificacion calificacion, TextoDeResena texto, LocalDateTime fecha,
                                     RespuestaAResena respuesta) {
        return new Resena(id, compradorId, presentacionId, vendedorId, calificacion, texto, fecha, respuesta);
    }

    /**
     * El vendedor dueño de la Presentación responde la Reseña; solo puede hacerlo una vez.
     *
     * @throws ReglaDominioException si quien responde no es el dueño de la Presentación o si ya fue respondida
     */
    public void responder(String vendedorId, TextoDeResena texto, LocalDateTime fecha) {
        if (!this.vendedorId.equals(vendedorId)) {
            throw new ReglaDominioException("Solo el vendedor dueno de la presentacion puede responder sus resenas");
        }
        if (respuesta != null) {
            throw new ReglaDominioException("La resena ya fue respondida");
        }
        this.respuesta = new RespuestaAResena(texto, fecha);
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaDominioException(mensaje);
        }
    }

    public String getId() {
        return id;
    }

    public String getCompradorId() {
        return compradorId;
    }

    public String getPresentacionId() {
        return presentacionId;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public Calificacion getCalificacion() {
        return calificacion;
    }

    public TextoDeResena getTexto() {
        return texto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    /** Vacía mientras el vendedor no haya respondido. */
    public Optional<RespuestaAResena> getRespuesta() {
        return Optional.ofNullable(respuesta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resena)) return false;
        Resena otra = (Resena) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
