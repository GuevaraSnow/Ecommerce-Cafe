package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

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
     * MERCHANDISING delega en {@link ArticuloDeMerchandising#publicar}; en
     * cualquier otro caso delega en {@link PresentacionTrazable#publicar},
     * que valida sus propias reglas (Origen, frescura, perfil de tueste).
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                         String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                         Cantidad cantidadDisponible, Galeria galeria,
                                         PerfilTueste perfilDeTueste, RolVendedor rolVendedor,
                                         FechaDeTueste fechaTueste, String material, String descripcion) {
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
        if (rolVendedor == null) {
            throw new ReglaDominioException("La Presentación debe indicar el Rol del Vendedor que la publica");
        }
        if (rolVendedor == RolVendedor.FORMADOR) {
            throw new ReglaDominioException("Un Formador no puede publicar Presentaciones");
        }
        if (tipoPresentacion == TipoDePresentacion.MERCHANDISING) {
            if (rolVendedor != RolVendedor.VENDEDOR_DERIVADOS) {
                throw new ReglaDominioException("Solo un Vendedor de Derivados puede publicar un Artículo de Merchandising");
            }
            if (fechaTueste != null || perfilDeTueste != null) {
                throw new ReglaDominioException("Solo una Presentación Trazable admite fecha y perfil de tueste");
            }
            return ArticuloDeMerchandising.publicar(id, vendedorId, titulo, tipoPresentacion, precio,
                    cantidadDisponible, galeria, material, descripcion);
        }
        return PresentacionTrazable.publicar(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible,
                galeria, loteId, transformacionId, perfilDeTueste, fechaTueste, rolVendedor);
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
