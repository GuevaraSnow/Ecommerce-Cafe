package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Hecho histórico de que uno o varios Lotes fueron usados como insumo para
 * producir café tostado u otro derivado. El descuento de cantidad disponible
 * sobre cada Lote de origen es responsabilidad del propio Lote
 * (Lote.descontar); esta entidad solo deja constancia del hecho para
 * alimentar la Ficha de Origen.
 */
public class Transformacion {

    private final String id;
    private final List<String> loteIds;
    private final Cantidad cantidadUsada;
    private final String tostadorId;
    private final LocalDate fecha;
    private final PerfilTueste perfilTuesteResultante;

    private Transformacion(String id, List<String> loteIds, Cantidad cantidadUsada, String tostadorId,
                            LocalDate fecha, PerfilTueste perfilTuesteResultante) {
        this.id = id;
        this.loteIds = loteIds;
        this.cantidadUsada = cantidadUsada;
        this.tostadorId = tostadorId;
        this.fecha = fecha;
        this.perfilTuesteResultante = perfilTuesteResultante;
    }

    /**
     * Registra una Transformación validando identificador, Lotes de origen,
     * cantidad usada, quién la ejecuta y el Perfil de Tueste resultante.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Transformacion registrar(String id, List<String> loteIds, Cantidad cantidadUsada,
                                            String tostadorId, LocalDate fecha,
                                            PerfilTueste perfilTuesteResultante) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Transformación debe tener un identificador");
        }
        if (loteIds == null || loteIds.isEmpty()) {
            throw new ReglaDominioException("La Transformación debe usar al menos un Lote de origen");
        }
        if (cantidadUsada == null) {
            throw new ReglaDominioException("La Transformación debe indicar la Cantidad usada");
        }
        if (cantidadUsada.valor() <= 0) {
            throw new ReglaDominioException("La cantidad usada en una Transformación debe ser mayor a cero");
        }
        if (tostadorId == null || tostadorId.isBlank()) {
            throw new ReglaDominioException("La Transformación debe indicar quién la ejecutó");
        }
        if (fecha == null) {
            throw new ReglaDominioException("La Transformación debe tener una fecha");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new ReglaDominioException("La fecha de la Transformación no puede ser futura");
        }
        if (perfilTuesteResultante == null) {
            throw new ReglaDominioException("La Transformación debe indicar el Perfil de Tueste resultante");
        }
        return new Transformacion(id, List.copyOf(loteIds), cantidadUsada, tostadorId, fecha, perfilTuesteResultante);
    }

    public String getId() {return id;}

    public List<String> getLoteIds() {return loteIds;}

    public Cantidad getCantidadUsada() {return cantidadUsada;}

    public String getTostadorId() {return tostadorId;}

    public LocalDate getFecha() {return fecha;}

    public PerfilTueste getPerfilTuesteResultante() {return perfilTuesteResultante;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transformacion)) return false;
        Transformacion otra = (Transformacion) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
