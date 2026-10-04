package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.EstadoDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

/**
 * Compra de un Curso: un único Curso a precio congelado, sin Dirección de
 * Envío ni tramo logístico. Una vez confirmada solo se deshace por reembolso
 * del Curso, nunca por cancelación.
 */
public class CompraDigital extends Compra {

    private final String cursoId;
    private final Precio precioCongelado;

    private CompraDigital(String compradorId, String cursoId, Precio precioCongelado) {
        super(compradorId);
        this.cursoId = cursoId;
        this.precioCongelado = precioCongelado;
    }

    public static CompraDigital iniciar(String compradorId, String cursoId, Precio precioCongelado) {
        if (cursoId == null || cursoId.isBlank()) {
            throw new ReglaDominioException("La compra digital debe estar asociada a un curso");
        }
        if (precioCongelado == null) {
            throw new ReglaDominioException("La compra digital debe tener un precio");
        }
        return new CompraDigital(compradorId, cursoId, precioCongelado);
    }

    @Override
    public boolean puedeCancelarse() {
        return getEstado() == EstadoDeCompra.PENDIENTE;
    }

    @Override
    public boolean incluye(String presentacionId) {
        return false;
    }

    public String getCursoId() {
        return cursoId;
    }

    public Precio getPrecioCongelado() {
        return precioCongelado;
    }
}
