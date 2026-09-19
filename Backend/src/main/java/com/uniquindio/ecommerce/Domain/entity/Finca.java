package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;

import java.util.Objects;

/**
 * Terreno de cultivo con nombre, ubicación y altura (msnm), propiedad de
 * un Caficultor. Es el punto de partida de toda la trazabilidad: de una
 * Finca surgen las Cosechas, y de estas los Lotes.
 *
 * Se crea únicamente mediante registrar para garantizar que las reglas
 * de negocio del dominio se cumplan desde su origen.
 */
public class Finca {

    private final String id;
    private final String propietarioId;
    private final String nombre;
    private final Ubicacion ubicacion;
    private final Altitud altitud;
    private boolean eliminadaLogicamente;

    private Finca(String id, String propietarioId, String nombre, Ubicacion ubicacion, Altitud altitud) {
        this.id = id;
        this.propietarioId = propietarioId;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.altitud = altitud;
        this.eliminadaLogicamente = false;
    }

    /**
     * Crea una Finca validando identificador, propietario, nombre,
     * ubicación y altura.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Finca registrar(String id, String propietarioId, String nombre,
                                  Ubicacion ubicacion, Altitud altitud) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un identificador");
        }
        if (propietarioId == null || propietarioId.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un Caficultor propietario");
        }
        if(ubicacion == null ){
            throw  new ReglaDominioException("La Finca debe tener un Ubicacion");
        }
        if(altitud == null ){
            throw  new ReglaDominioException("La Finca debe tener una altitud");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un nombre");
        }
        return new Finca(id, propietarioId, nombre, ubicacion, altitud);
    }

    /** Marca la Finca como eliminada. */
    public void eliminar() {
        this.eliminadaLogicamente = true;
    }

    public String getId() {
        return id;
    }

    public String getPropietarioId() {
        return propietarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public Altitud getAltitud() {
        return altitud;
    }

    public boolean isEliminada() {
        return eliminadaLogicamente;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Finca)) return false;
        Finca otra = (Finca) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}