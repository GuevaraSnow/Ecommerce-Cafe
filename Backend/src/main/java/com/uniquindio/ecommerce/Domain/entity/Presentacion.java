package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import com.uniquindio.ecommerce.Domain.valueobject.TipoProducto;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Producto publicado para la venta, originado a partir de un Lote (café
 * verde/pergamino) o de una Transformación (café tostado/derivado), nunca
 * de ambos. Se crea únicamente mediante publicar para garantizar
 * que las reglas de negocio del dominio se cumplan desde su origen.
 */
public class Presentacion {
    private static final int DIAS_MAXIMOS_FRESCURA = 30;

    private final String id;
    private final String idLote;
    private final String idTransformacion;
    private final String titulo;
    private final Precio precio;
    private final TipoProducto tipoProducto;
    private final LocalDate fechaTueste;
    private boolean eliminada;

    private Presentacion(String id, String idLote, String idTransformacion, String titulo, Precio precio,
                        TipoProducto tipoProducto, LocalDate fechaTueste) {
        this.id = id;
        this.idLote = idLote;
        this.idTransformacion = idTransformacion;
        this.titulo = titulo;
        this.precio = precio;
        this.tipoProducto = tipoProducto;
        this.fechaTueste = fechaTueste;
        this.eliminada = false;
    }

    /**
     * Crea una Presentación validando identificador, precio y tipo de producto;
     * origen exclusivo desde un Lote
     * o una Transformación según el TipoProducto; y, para café
     * tostado, fecha de tueste dentro de los días de frescura.
     *
     * @throws ReglaDominioException si se incumple alguna regla de negocio
     */
    public static Presentacion publicar(String id, String loteId, String transformacionId,
                                        String titulo, Precio precio, TipoProducto tipoProducto,
                                        LocalDate fechaTueste) {
        if (id == null || id.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un identificador");
        }
        if (precio == null) {
            throw new ReglaDominioException("La Presentación debe tener un Precio");
        }
        if (tipoProducto == null) {
            throw new ReglaDominioException("La Presentación debe especificar un TipoProducto");
        }

        boolean vieneDeLote = loteId != null && !loteId.isBlank();
        boolean vieneDeTransformacion = transformacionId != null && !transformacionId.isBlank();

        if (vieneDeLote == vieneDeTransformacion) {
            throw new ReglaDominioException(
                    "La Presentación debe provenir de un Lote directo o de una Transformación, no de ambos ni de ninguno");
        }

        if (tipoProducto == TipoProducto.CAFE_VERDE || tipoProducto == TipoProducto.CAFE_PERGAMINO) {
            if (!vieneDeLote) {
                throw new ReglaDominioException(
                        "Una Presentación de café verde o pergamino debe provenir directamente de un Lote");
            }
        }

        if (tipoProducto == TipoProducto.CAFE_TOSTADO || tipoProducto == TipoProducto.DERIVADO) {
            if (!vieneDeTransformacion) {
                throw new ReglaDominioException(
                        "Una Presentación tostada o derivada debe provenir de una Transformación");
            }
        }

        if (tipoProducto == TipoProducto.CAFE_TOSTADO) {
            if (fechaTueste == null) {
                throw new ReglaDominioException("El café tostado debe indicar su fecha de tueste");
            }
            long dias = java.time.temporal.ChronoUnit.DAYS.between(fechaTueste, LocalDate.now());
            if (dias > DIAS_MAXIMOS_FRESCURA) {
                throw new ReglaDominioException(
                        "No se puede publicar: han pasado " + dias + " días desde el tueste (máximo "
                                + DIAS_MAXIMOS_FRESCURA + ")");
            }
        }

        if (titulo == null || titulo.isBlank()) {
            throw new ReglaDominioException("La Presentación debe tener un título");
        }

        return new Presentacion(id, loteId, transformacionId, titulo, precio, tipoProducto, fechaTueste);
    }

    /** Marca la Presentación como eliminada. */
    public void eliminar() {this.eliminada = true;}

    public String getId() {return id;}

    public String getIdLote() {return idLote;}

    public String getIdTransformacion() {return idTransformacion;}

    public String getTitulo() {return titulo;}

    public Precio getPrecio() {return precio;}

    public TipoProducto getTipoProducto() {return tipoProducto;}

    public LocalDate getFechaTueste() {return fechaTueste;}

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