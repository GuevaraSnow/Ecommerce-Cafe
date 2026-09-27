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

    private PresentacionTrazable(String id, String vendedorId, String titulo, TipoDePresentacion tipoPresentacion,
                          Precio precio, Cantidad cantidadDisponible, Galeria galeria,
                          OrigenDePresentacion origen, PerfilTueste perfilDeTueste, FechaDeTueste fechaTueste) {
        super(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible, galeria);
        this.origen = origen;
        this.perfilDeTueste = perfilDeTueste;
        this.fechaTueste = fechaTueste;
        this.notaDeCata = null;
    }

    /**
     * Valida y crea una Presentación Trazable: Origen exclusivo desde un Lote
     * o una Transformación según el Tipo de Presentación (regla B) y, para
     * café tostado, fecha y perfil de tueste vigentes (regla D) y
     * compatibles con el rol de quien publica (regla A).
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    static PresentacionTrazable publicar(String id, String vendedorId, String titulo,
                                          TipoDePresentacion tipoPresentacion, Precio precio,
                                          Cantidad cantidadDisponible, Galeria galeria, String loteId,
                                          String transformacionId, PerfilTueste perfilDeTueste,
                                          FechaDeTueste fechaTueste, RolVendedor rolVendedor) {
        if (rolVendedor == null) {
            throw new ReglaDominioException("La Presentación debe indicar el Rol del Vendedor que la publica");
        }
        if ("unidad".equals(cantidadDisponible.unidad())) {
            throw new ReglaDominioException(
                    "Una Presentación Trazable no se cuenta en 'unidad', debe indicar peso o volumen (kg, g, ml)");
        }

        OrigenDePresentacion origen = OrigenDePresentacion.desde(loteId, transformacionId);

        if (tipoPresentacion == TipoDePresentacion.CAFE_VERDE && origen.transformacionId() != null) {
            throw new ReglaDominioException("Una Presentación de café verde debe provenir directamente de un Lote");
        }
        if ((tipoPresentacion == TipoDePresentacion.CAFE_TOSTADO || tipoPresentacion == TipoDePresentacion.DERIVADO_CONSUMIBLE)
                && origen.loteId() != null) {
            throw new ReglaDominioException("Una Presentación tostada o derivada debe provenir de una Transformación");
        }

        if (tipoPresentacion == TipoDePresentacion.CAFE_TOSTADO) {
            if (fechaTueste == null) {
                throw new ReglaDominioException("El café tostado debe indicar su fecha de tueste");
            }
            if (perfilDeTueste == null) {
                throw new ReglaDominioException("El café tostado debe indicar un Perfil de Tueste");
            }
            long dias = ChronoUnit.DAYS.between(fechaTueste.valor(), LocalDate.now());
            if (dias > DIAS_MAXIMOS_FRESCURA) {
                throw new ReglaDominioException(
                        "No se puede publicar: han pasado " + dias + " días desde el tueste (máximo "
                                + DIAS_MAXIMOS_FRESCURA + ")");
            }
        } else if (fechaTueste != null || perfilDeTueste != null) {
            throw new ReglaDominioException("Solo el café tostado admite fecha y perfil de tueste");
        }

        if (perfilDeTueste != null && rolVendedor == RolVendedor.CAFICULTOR
                && perfilDeTueste != PerfilTueste.TRADICIONAL) {
            throw new ReglaDominioException(
                    "Un Caficultor solo puede publicar café tostado con Perfil de Tueste TRADICIONAL");
        }

        return new PresentacionTrazable(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible,
                galeria, origen, perfilDeTueste, fechaTueste);
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
