package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Entidad Transformacion rediseñada.
 * Mantiene el registro de la cantidad de entrada, la merma aplicada,
 * la cantidad resultante producida y la cantidad resultante disponible.
 */
public class Transformacion {

    private final String id;
    private final List loteIds;
    private final Cantidad cantidadEntrada;
    private final PorcentajeDeMerma merma;
    private final Cantidad cantidadResultante;
    private Cantidad cantidadResultanteDisponible;
    private final String tostadorId;
    private final LocalDate fecha;
    private final PerfilTueste perfilTuesteResultante;

    private Transformacion(String id, List loteIds, Cantidad cantidadEntrada, PorcentajeDeMerma merma,
                           Cantidad cantidadResultante, Cantidad cantidadResultanteDisponible,
                           String tostadorId, LocalDate fecha, PerfilTueste perfilTuesteResultante) {
        this.id = id;
        this.loteIds = loteIds;
        this.cantidadEntrada = cantidadEntrada;
        this.merma = merma;
        this.cantidadResultante = cantidadResultante;
        this.cantidadResultanteDisponible = cantidadResultanteDisponible;
        this.tostadorId = tostadorId;
        this.fecha = fecha;
        this.perfilTuesteResultante = perfilTuesteResultante;
    }

    /**
     * Registra una Transformación calculando automáticamente la cantidad resultante con la merma.
     */
    public static Transformacion registrar(String id, List loteIds, Cantidad cantidadEntrada,
                                           PorcentajeDeMerma merma, String tostadorId, LocalDate fecha,
                                           PerfilTueste perfilTuesteResultante) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Transformación debe tener un identificador");
        }
        if (loteIds == null || loteIds.isEmpty()) {
            throw new ReglaDominioException("La Transformación debe usar al menos un Lote de origen");
        }
        if (cantidadEntrada == null) {
            throw new ReglaDominioException("La Transformación debe indicar la Cantidad de entrada");
        }
        if (cantidadEntrada.valor() <= 0) {
            throw new ReglaDominioException("La cantidad de entrada en una Transformación debe ser mayor a cero");
        }
        if (merma == null) {
            throw new ReglaDominioException("La Transformación debe especificar el porcentaje de merma");
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

        // Cálculo de la cantidad resultante aplicando la merma a la cantidad de entrada
        Cantidad resultante = cantidadEntrada.aplicarMerma(merma);

        return new Transformacion(
                id,
                List.copyOf(loteIds),
                cantidadEntrada,
                merma,
                resultante,
                resultante, // Inicialmente la cantidad disponible es igual a la resultante
                tostadorId,
                fecha,
                perfilTuesteResultante
        );
    }

    /**
     * Descuenta del stock disponible de la transformación al publicar/crear una Presentación Trazable.
     */
    public void asignarCantidad(Cantidad cantidadAAsignar) {
        if (cantidadAAsignar == null) {
            throw new ReglaDominioException("La cantidad a asignar no puede ser nula");
        }
        // Utiliza el método restar del Value Object Cantidad
        this.cantidadResultanteDisponible = this.cantidadResultanteDisponible.restar(cantidadAAsignar);
    }

    // Getters
    public String getId() { return id; }
    public List getLoteIds() { return loteIds; }
    public Cantidad getCantidadEntrada() { return cantidadEntrada; }
    public PorcentajeDeMerma getMerma() { return merma; }
    public Cantidad getCantidadResultante() { return cantidadResultante; }
    public Cantidad getCantidadResultanteDisponible() { return cantidadResultanteDisponible; }
    public String getTostadorId() { return tostadorId; }
    public LocalDate getFecha() { return fecha; }
    public PerfilTueste getPerfilTuesteResultante() { return perfilTuesteResultante; }

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