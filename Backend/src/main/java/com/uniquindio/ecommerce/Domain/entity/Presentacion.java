package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Presentacion {
    private final String id;
    private final String vendedorId;
    private final String titulo;
    private final TipoDePresentacion tipoPresentacion;
    private Precio precio;
    private Descuento descuento;
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

    public static Presentacion publicar(String id, String vendedorId, String loteId, String transformacionId,
                                        String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                        Cantidad cantidadDisponible, Galeria galeria,
                                        PerfilTueste perfilDeTueste, Rol rolVendedor,
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
        if (tipoPresentacion == TipoDePresentacion.MERCHANDISING) {
            if (rolVendedor != Rol.VENDEDOR_DERIVADOS) {
                throw new ReglaDominioException("Solo un Vendedor de Derivados puede publicar un Artículo de Merchandising");
            }
            if (fechaTueste != null || perfilDeTueste != null) {
                throw new ReglaDominioException("Solo una Presentación Trazable admite fecha y perfil de tueste");
            }
            if ((loteId != null && !loteId.isBlank()) || (transformacionId != null && !transformacionId.isBlank())) {
                throw new ReglaDominioException(
                        "Un Artículo de Merchandising no tiene Origen: no puede provenir de un Lote ni de una Transformación");
            }
            return ArticuloDeMerchandising.publicar(id, vendedorId, titulo, tipoPresentacion, precio,
                    cantidadDisponible, galeria, material, descripcion);
        }
        return PresentacionTrazable.publicar(id, vendedorId, titulo, tipoPresentacion, precio, cantidadDisponible,
                galeria, loteId, transformacionId, perfilDeTueste, fechaTueste, rolVendedor);
    }

    public void descontarCantidad(Cantidad cantidadUsada) {
        validarNoEliminada();
        this.cantidadDisponible = this.cantidadDisponible.restar(cantidadUsada);
        if (this.cantidadDisponible.valor() == 0 && getEstado() == EstadoDePublicacion.ACTIVA) {
            marcarAgotada();
        }
    }

    public void reponerCantidad(Cantidad cantidadNueva) {
        validarNoEliminada();
        this.cantidadDisponible = this.cantidadDisponible.sumar(cantidadNueva);
        if (getEstado() == EstadoDePublicacion.AGOTADA && this.cantidadDisponible.valor() > 0) {
            this.estado = EstadoDePublicacion.ACTIVA;
        }
    }

    public void verificarQueSePuedeReponer(Cantidad cantidad) {
        validarNoEliminada();
        if (cantidad == null || cantidad.valor() <= 0) {
            throw new ReglaDominioException("La cantidad a reponer debe ser mayor a cero");
        }
        cantidadDisponible.sumar(cantidad);
    }

    public void registrarNotaCata(NotaDeCata nota) {
        throw new ReglaDominioException("Solo una Presentación Trazable admite Nota de Cata");
    }

    public void cambiarPrecio(Precio nuevoPrecio) {
        validarNoEliminada();
        if (nuevoPrecio == null) {
            throw new ReglaDominioException("El nuevo Precio no puede ser nulo");
        }
        this.precio = nuevoPrecio;
    }

    public Precio precioVigente(LocalDate hoy) {
        if (descuento != null && descuento.estaVigenteEn(hoy)) {
            return precio.aplicarDescuento(descuento);
        }
        return precio;
    }

    public void programarDescuento(Descuento nuevoDescuento) {
        validarNoEliminada();
        if (nuevoDescuento == null) {
            throw new ReglaDominioException("El Descuento no puede ser nulo");
        }
        this.descuento = nuevoDescuento;
    }

    public void quitarDescuento() {
        validarNoEliminada();
        this.descuento = null;
    }

    public void reemplazarGaleria(Galeria nuevaGaleria) {
        validarNoEliminada();
        if (nuevaGaleria == null) {
            throw new ReglaDominioException("La Galería no puede ser nula");
        }
        this.galeria = nuevaGaleria;
    }

    private void marcarAgotada() {
        this.estado = EstadoDePublicacion.AGOTADA;
    }

    public void desactivar() {
        validarNoEliminada();
        if (getEstado() != EstadoDePublicacion.ACTIVA) {
            throw new ReglaDominioException(
                    "Solo una Presentación Activa puede pausarse (estado actual: " + getEstado() + ")");
        }
        this.estado = EstadoDePublicacion.INACTIVA;
    }

    public void activar() {
        validarNoEliminada();
        if (getEstado() != EstadoDePublicacion.INACTIVA) {
            throw new ReglaDominioException("Solo una Presentación Inactiva puede reactivarse manualmente");
        }
        if (this.cantidadDisponible.valor() == 0) {
            throw new ReglaDominioException("No se puede reactivar una Presentación sin cantidad disponible");
        }
        this.estado = EstadoDePublicacion.ACTIVA;
    }

    protected void marcarVencida() {
        this.estado = EstadoDePublicacion.VENCIDA;
    }

    public boolean estaDisponibleParaVenta() {
        return !eliminada && getEstado() == EstadoDePublicacion.ACTIVA;
    }

    public void verificarQueSePuedeVender(Cantidad cantidad) {
        if (!estaDisponibleParaVenta()) {
            throw new ReglaDominioException("La Presentación " + id
                    + " no está disponible para la venta (dada de baja, pausada, agotada o vencida)");
        }
        cantidadDisponible.restar(cantidad);
    }

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

    public Descuento getDescuento() {return descuento;}

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