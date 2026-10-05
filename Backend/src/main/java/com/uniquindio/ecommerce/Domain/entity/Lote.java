package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.util.Objects;

public class Lote {
    private final CodigoDeLote codigoDeLote;
    private final String fincaID;
    private final Cosecha cosecha;
    private final ProcesoDeBeneficio procesoDeBeneficio;
    private final VariedadDeCafe variedad;
    private final EstadoDelCafe estado;
    private Cantidad cantidadDisponible;
    private boolean eliminadoLogicamente;

    private Lote(CodigoDeLote codigoDeLote, Cantidad cantidadDisponible, String fincaID, ProcesoDeBeneficio
            procesoDeBeneficio, EstadoDelCafe estado, Cosecha cosecha, VariedadDeCafe variedad) {
        this.codigoDeLote = codigoDeLote;
        this.cantidadDisponible = cantidadDisponible;
        this.fincaID = fincaID;
        this.procesoDeBeneficio = procesoDeBeneficio;
        this.estado = estado;
        this.cosecha = cosecha;
        this.variedad = variedad;
        this.eliminadoLogicamente = false;
    }

    /**
     * Registra un nuevo Lote validando que tenga todos sus datos de origen, que no se
     * cree directamente en estado Tostado y que la cantidad inicial sea mayor a cero.
     */
    public static Lote registrar(CodigoDeLote codigoDeLote, Cantidad cantidadDisponible, String fincaid, ProcesoDeBeneficio
            procesoDeBeneficio, EstadoDelCafe estado, Cosecha cosecha, VariedadDeCafe variedad){
        if (codigoDeLote == null) {
            throw new ReglaDominioException("El Lote debe tener un Código de Lote.");
        }
        if (fincaid == null || fincaid.isBlank()) {
            throw new ReglaDominioException("El Lote debe provenir de una Finca.");
        }
        if (cosecha == null) {
            throw new ReglaDominioException("El Lote debe indicar su Cosecha.");
        }
        if (procesoDeBeneficio == null) {
            throw new ReglaDominioException("El Lote debe indicar su Proceso de Beneficio.");
        }
        if (variedad == null) {
            throw new ReglaDominioException("El Lote debe indicar su Variedad de Café.");
        }
        if (estado == null) {
            throw new ReglaDominioException("El Lote debe indicar su Estado del Café.");
        }
        if (cantidadDisponible == null) {
            throw new ReglaDominioException("El Lote debe tener una Cantidad inicial.");
        }
        if (estado == EstadoDelCafe.TOSTADO){
            throw new ReglaDominioException(
                    "Un Lote solo puede registrarse en estado Verde o Pergamino, nunca Tostado.");
        }
        if (cantidadDisponible.valor() <= 0) {
            throw new ReglaDominioException(
                    "La cantidad inicial de un Lote debe ser mayor a cero.");
        }
        return new Lote(codigoDeLote, cantidadDisponible, fincaid, procesoDeBeneficio, estado, cosecha, variedad);
    }

    /**
     * Descuenta cantidad disponible del Lote, ya sea para una Transformación o
     * para publicar café sin tostar como Presentación.
     * No permite operar sobre un Lote ya eliminado lógicamente.
     */
    public void descontar(Cantidad cantidadUsada) {
        if (eliminadoLogicamente) {
            throw new ReglaDominioException("No se puede usar el café de un Lote eliminado.");
        }
        this.cantidadDisponible = this.cantidadDisponible.restar(cantidadUsada);
    }

    /** Marca el Lote como eliminado (borrado lógico, no físico). */
    public void eliminarLogicamente() {
        this.eliminadoLogicamente = true;
    }

    public CodigoDeLote getCodigoDeLote() {
        return codigoDeLote;
    }

    public String getFincaID() {
        return fincaID;
    }

    public Cosecha getCosecha() {
        return cosecha;
    }

    public ProcesoDeBeneficio getProcesoDeBeneficio() {
        return procesoDeBeneficio;
    }

    public VariedadDeCafe getVariedad() {
        return variedad;
    }

    public EstadoDelCafe getEstado() {
        return estado;
    }

    public Cantidad getCantidadDisponible() {
        return cantidadDisponible;
    }

    public boolean isEliminadoLogicamente() {
        return eliminadoLogicamente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Lote)) return false;
        Lote otro = (Lote) o;
        return codigoDeLote.equals(otro.codigoDeLote);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigoDeLote);
    }
}
