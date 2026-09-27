package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Unidad final empacada que el Comprador puede adquirir. Raíz abstracta del
 * agregado: concentra el ciclo de vida común a sus dos subclases,
 * {@link PresentacionTrazable} (café y derivados consumibles, siempre con
 * Origen) y {@link ArticuloDeMerchandising} (no alimenticio, sin Origen).
 * Se crea únicamente mediante {@link #publicar} para garantizar que las
 * reglas de negocio del dominio se cumplan desde su origen.
 */
public abstract class Presentacion {
    private final String id;
    private final String vendedorId;
    private final String titulo;
    private final TipoDePresentacion tipoPresentacion;
    private Precio precio;
    private Cantidad cantidadDisponible;
    private Galeria galeria;
    private EstadoDePublicacion estado;
    private boolean eliminada;

    protected Presentacion(String id, String vendedorId, String titulo,
                          TipoDePresentacion tipoPresentacion, Precio precio, Cantidad cantidadDisponible, Galeria galeria) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un identificador");
        }
        if (vendedorId == null || vendedorId.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un Vendedor");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un título");
        }
        if (tipoPresentacion == null) {
            throw new ReglaDominioException("La Presentación debe especificar un Tipo de Presentación");
        }
        if (precio == null) {
            throw new ReglaDominioException("La Presentación debe tener un Precio");
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
        this.id = id;
        this.vendedorId = vendedorId;
        this.titulo = titulo;
        this.tipoPresentacion = tipoPresentacion;
        this.precio = precio;
        this.cantidadDisponible = cantidadDisponible;
        this.galeria = galeria;
        this.estado = EstadoDePublicacion.ACTIVA;
        this.eliminada = false;
    }

    /**
     * Crea una Presentación validando identificador, vendedor, precio, cantidad
     * inicial y galería. El Tipo de Presentación decide la subclase: si es
     * MERCHANDISING crea un Artículo de Merchandising (sin Origen); en
     * cualquier otro caso crea una Presentación Trazable, exigiendo Origen
     * exclusivo desde un Lote o una Transformación (regla B) y, para café
     * tostado, fecha y perfil de tueste vigentes (regla D) y compatibles con
     * el rol de quien publica (regla A).
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                         String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                         Cantidad cantidadDisponible, Galeria galeria,
                                         PerfilTueste perfilDeTueste, RolVendedor rolVendedor,
                                         FechaDeTueste fechaTueste) {
        if (rolVendedor == null) {
            throw new ReglaDominioException("La Presentación debe indicar el Rol del Vendedor que la publica");
        }

        if (tipoPresentacion == TipoDePresentacion.MERCHANDISING) {
            if (fechaTueste != null || perfilDeTueste != null) {
                throw new ReglaDominioException("Solo una Presentación Trazable admite fecha y perfil de tueste");
            }
            return new ArticuloDeMerchandising(id, vendedorId, titulo, tipoPresentacion, precio,
                    cantidadDisponible, galeria);
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
            if (dias > PresentacionTrazable.DIAS_MAXIMOS_FRESCURA) {
                throw new ReglaDominioException(
                        "No se puede publicar: han pasado " + dias + " días desde el tueste (máximo "
                                + PresentacionTrazable.DIAS_MAXIMOS_FRESCURA + ")");
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

    public void reemplazarGaleria(Galeria nuevaGaleria) {
        validarNoEliminada();
        if (nuevaGaleria == null) {
            throw new ReglaDominioException("La Galería no puede ser nula");
        }
        this.galeria = nuevaGaleria;
    }

    public void marcarAgotada() {
        validarNoEliminada();
        this.estado = EstadoDePublicacion.AGOTADA;
    }

    /** Da de baja la Presentación (borrado lógico); nunca se elimina físicamente. */
    public void darDeBaja() {
        this.eliminada = true;
    }

    protected void validarNoEliminada() {
        if (eliminada) {
            throw new ReglaDominioException("No se puede operar sobre una Presentación dada de baja");
        }
    }

    public String getId() {return id;}

    public String getVendedorId() {return vendedorId;}

    public String getTitulo() {return titulo;}

    public Precio getPrecio() {return precio;}

    public TipoDePresentacion getTipoPresentacion() {return tipoPresentacion;}

    public Cantidad getCantidadDisponible() {return cantidadDisponible;}

    public Galeria getGaleria() {return galeria;}

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
