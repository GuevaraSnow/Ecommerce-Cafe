package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Presentación con trazabilidad: café verde, tostado y derivados
 * consumibles (licor, miel, cosmético). Siempre tiene un Origen (Lote o
 * Transformación, regla B) y, para café tostado, Perfil de Tueste y Fecha
 * de Tueste. Se crea únicamente desde {@link Presentacion#publicar}.
 */
public class PresentacionTrazable extends Presentacion {

    static final int DIAS_MAXIMOS_FRESCURA = 30;

    private final OrigenDePresentacion origen;
    private final PerfilTueste perfilDeTueste;
    private final FechaDeTueste fechaTueste;
    private NotaDeCata notaDeCata;

    PresentacionTrazable(String id, String vendedorId, String titulo, TipoDePresentacion tipoPresentacion,
                          Precio precio, Cantidad cantidadDisponible, Galeria galeria,
                          OrigenDePresentacion origen, PerfilTueste perfilDeTueste, FechaDeTueste fechaTueste) {
        super(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible, galeria);
        this.origen = origen;
        this.perfilDeTueste = perfilDeTueste;
        this.fechaTueste = fechaTueste;
        this.notaDeCata = null;
    }

    /**
     * Indica si la Presentación sigue dentro de los días de frescura tras el
     * tueste (regla D). Las Presentaciones sin fecha de tueste (café verde)
     * siempre se consideran frescas.
     */
    public boolean estaFresca() {
        if (fechaTueste == null) {
            return true;
        }
        long dias = ChronoUnit.DAYS.between(fechaTueste.valor(), LocalDate.now());
        return dias <= DIAS_MAXIMOS_FRESCURA;
    }

    public void registrarNotaCata(NotaDeCata nota) {
        validarNoEliminada();
        if (nota == null) {
            throw new ReglaDominioException("La Nota de Cata no puede ser nula");
        }
        this.notaDeCata = nota;
    }

    public OrigenDePresentacion getOrigen() {return origen;}

    public PerfilTueste getPerfilDeTueste() {return perfilDeTueste;}

    public FechaDeTueste getFechaTueste() {return fechaTueste;}

    public NotaDeCata getNotaDeCata() {return notaDeCata;}
}
