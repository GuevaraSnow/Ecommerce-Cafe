package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

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
    private final String ubicacion;
    private final double alturaMsnm;
    private boolean eliminada;

    private Finca(String id, String propietarioId, String nombre, String ubicacion, double alturaMsnm) {
        this.id = id;
        this.propietarioId = propietarioId;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.alturaMsnm = alturaMsnm;
        this.eliminada = false;
    }

    /**
     * Crea una Finca validando identificador, propietario, nombre,
     * ubicación y altura.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Finca registrar(String id, String propietarioId, String nombre,
                                  String ubicacion, double alturaMsnm) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un identificador");
        }
        if (propietarioId == null || propietarioId.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un Caficultor propietario");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener un nombre");
        }
        if (ubicacion == null || ubicacion.isBlank()) {
            throw new ReglaDominioException("La Finca debe tener una ubicación");
        }
        if (alturaMsnm < 0) {
            throw new ReglaDominioException("La altura de la Finca no puede ser negativa");
        }

        return new Finca(id, propietarioId, nombre, ubicacion, alturaMsnm);
    }

    /** Marca la Finca como eliminada. */
    public void eliminar() {
        this.eliminada = true;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public double getAlturaMsnm() {
        return alturaMsnm;
    }

    public boolean isEliminada() {
        return eliminada;
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