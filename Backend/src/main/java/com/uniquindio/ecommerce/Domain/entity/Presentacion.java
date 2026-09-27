package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Unidad final empacada que el Comprador puede adquirir. Proviene
 * directamente de un Lote (verde/pergamino, venta B2B) o de una
 * Transformación (tostada/derivada), nunca de ambos. Se crea únicamente
 * mediante publicar para garantizar que las reglas de negocio del dominio
 * se cumplan desde su origen.
 */
public class Presentacion {
    private static final int DIAS_MAXIMOS_FRESCURA = 30;

    private final String id;
    private final String idLote;
    private final String idTransformacion;
    private final String vendedorId;
    private final String titulo;
    private final TipoDePresentacion tipoPresentacion;
    private final PerfilTueste perfilDeTueste;
    private final FechaDeTueste fechaTueste;
    private Precio precio;
    private Cantidad cantidadDisponible;
    private Galeria galeria;
    private NotaDeCata notaDeCata;
    private EstadoDePublicacion estado;
    private boolean eliminada;

    private Presentacion(String id, String idLote, String idTransformacion, String vendedorId, String titulo,
                          TipoDePresentacion tipoPresentacion, Precio precio, Cantidad cantidadDisponible, Galeria galeria,
                          PerfilTueste perfilDeTueste, FechaDeTueste fechaTueste) {
        this.id = id;
        this.idLote = idLote;
        this.idTransformacion = idTransformacion;
        this.vendedorId = vendedorId;
        this.titulo = titulo;
        this.tipoPresentacion = tipoPresentacion;
        this.precio = precio;
        this.cantidadDisponible = cantidadDisponible;
        this.galeria = galeria;
        this.perfilDeTueste = perfilDeTueste;
        this.fechaTueste = fechaTueste;
        this.notaDeCata = null;
        this.estado = EstadoDePublicacion.ACTIVA;
        this.eliminada = false;
    }

    /**
     * Crea una Presentación validando identificador, vendedor, precio, cantidad
     * inicial y galería; origen exclusivo desde un Lote o una Transformación
     * según el Tipo de Presentación; y, para café tostado, fecha y perfil de
     * tueste vigentes (regla D) y compatibles con el rol de quien publica (regla A).
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                         String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                         Cantidad cantidadDisponible, Galeria galeria,
                                         PerfilTueste perfilDeTueste, RolVendedor rolVendedor,
                                         FechaDeTueste fechaTueste) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un identificador");
        }
        if (vendedorId == null || vendedorId.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un Vendedor");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un título");
        }
        if (precio == null) {
            throw new ReglaDominioException("La Presentación debe tener un Precio");
        }
        if (tipoPresentacion == null) {
            throw new ReglaDominioException("La Presentación debe especificar un Tipo de Presentación");
        }
        if (cantidadDisponible == null) {
            throw new ReglaDominioException("La Presentación debe tener una Cantidad disponible");
        }
        if (cantidadDisponible.valor() <= 0) {
            throw new ReglaDominioException("La cantidad inicial de una Presentación debe ser mayor a cero");
        }
        if (galeria == null) {
            throw new ReglaDominioException("La Presentación debe tener una Galería");
        }
        if (rolVendedor == null) {
            throw new ReglaDominioException("La Presentación debe indicar el Rol del Vendedor que la publica");
        }

        boolean vieneDeLote = loteId != null && !loteId.isBlank();
        boolean vieneDeTransformacion = transformacionId != null && !transformacionId.isBlank();

        if (vieneDeLote == vieneDeTransformacion) {
            throw new ReglaDominioException(
                    "La Presentación debe provenir de un Lote directo o de una Transformación, no de ambos ni de ninguno");
        }

        if (tipoPresentacion == TipoDePresentacion.CAFE_VERDE || tipoPresentacion == TipoDePresentacion.CAFE_PERGAMINO) {
            if (!vieneDeLote) {
                throw new ReglaDominioException(
                        "Una Presentación de café verde o pergamino debe provenir directamente de un Lote");
            }
        }

        if (tipoPresentacion == TipoDePresentacion.CAFE_TOSTADO || tipoPresentacion == TipoDePresentacion.DERIVADO) {
            if (!vieneDeTransformacion) {
                throw new ReglaDominioException(
                        "Una Presentación tostada o derivada debe provenir de una Transformación");
            }
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

        return new Presentacion(id, loteId, transformacionId, vendedorId, titulo, tipoPresentacion, precio,
                cantidadDisponible, galeria, perfilDeTueste, fechaTueste);
    }

    /**
     * Descuenta cantidad disponible (p. ej. al confirmarse una compra).
     * Si la cantidad llega a cero, la Presentación queda AGOTADA.
     */
    public void descontarCantidad(Cantidad cantidadUsada) {
        validarNoEliminada();
        this.cantidadDisponible = this.cantidadDisponible.restar(cantidadUsada);
        if (this.cantidadDisponible.valor() == 0) {
            marcarAgotada();
        }
    }

    /** Repone cantidad disponible, reactivando la Presentación si estaba AGOTADA. */
    public void reponerCantidad(Cantidad cantidadNueva) {
        validarNoEliminada();
        this.cantidadDisponible = this.cantidadDisponible.sumar(cantidadNueva);
        if (this.estado == EstadoDePublicacion.AGOTADA && this.cantidadDisponible.valor() > 0) {
            this.estado = EstadoDePublicacion.ACTIVA;
        }
    }

    public void cambiarPrecio(Precio nuevoPrecio) {
        validarNoEliminada();
        if (nuevoPrecio == null) {
            throw new ReglaDominioException("El nuevo Precio no puede ser nulo");
        }
        this.precio = nuevoPrecio;
    }

    public void registrarNotaCata(NotaDeCata nota) {
        validarNoEliminada();
        if (nota == null) {
            throw new ReglaDominioException("La Nota de Cata no puede ser nula");
        }
        this.notaDeCata = nota;
    }

    public void reemplazarGaleria(Galeria nuevaGaleria) {
        validarNoEliminada();
        if (nuevaGaleria == null) {
            throw new ReglaDominioException("La Galería no puede ser nula");
        }
        this.galeria = nuevaGaleria;
    }

    /**
     * Indica si la Presentación sigue dentro de los días de frescura tras el
     * tueste (regla D). Las Presentaciones sin fecha de tueste (café
     * verde/pergamino) siempre se consideran frescas.
     */
    public boolean estaFresca() {
        if (fechaTueste == null) {
            return true;
        }
        long dias = ChronoUnit.DAYS.between(fechaTueste.valor(), LocalDate.now());
        return dias <= DIAS_MAXIMOS_FRESCURA;
    }

    public void marcarAgotada() {
        validarNoEliminada();
        this.estado = EstadoDePublicacion.AGOTADA;
    }

    /** Da de baja la Presentación (borrado lógico); nunca se elimina físicamente. */
    public void darDeBaja() {
        this.eliminada = true;
    }

    private void validarNoEliminada() {
        if (eliminada) {
            throw new ReglaDominioException("No se puede operar sobre una Presentación dada de baja");
        }
    }

    public String getId() {return id;}

    public String getIdLote() {return idLote;}

    public String getIdTransformacion() {return idTransformacion;}

    public String getVendedorId() {return vendedorId;}

    public String getTitulo() {return titulo;}

    public Precio getPrecio() {return precio;}

    public TipoDePresentacion getTipoPresentacion() {return tipoPresentacion;}

    public Cantidad getCantidadDisponible() {return cantidadDisponible;}

    public Galeria getGaleria() {return galeria;}

    public PerfilTueste getPerfilDeTueste() {return perfilDeTueste;}

    public FechaDeTueste getFechaTueste() {return fechaTueste;}

    public NotaDeCata getNotaDeCata() {return notaDeCata;}

    public EstadoDePublicacion getEstado() {return estado;}

    public boolean isEliminada() {return eliminada;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Presentacion)) return false;
        Presentacion otra = (Presentacion) o;
        return id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
