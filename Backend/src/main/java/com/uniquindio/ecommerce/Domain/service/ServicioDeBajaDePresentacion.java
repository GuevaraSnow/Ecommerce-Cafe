package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;

/**
 * Domain Service que protege la regla 3: una Presentación con compras activas
 * no puede darse de baja (solo eliminación lógica, y solo si nadie la tiene
 * en un pedido en curso). La regla cruza agregados (Presentación y Compra),
 * por eso no vive en la entidad. Sin estado y sin anotaciones de Spring.
 */
public class ServicioDeBajaDePresentacion {

    private final CompraRepositorio compraRepositorio;

    public ServicioDeBajaDePresentacion(CompraRepositorio compraRepositorio) {
        this.compraRepositorio = compraRepositorio;
    }

    /**
     * Da de baja la Presentación si ninguna Compra activa la incluye.
     *
     * @throws ReglaDominioException si tiene compras activas; la Presentación no cambia
     */
    public void darDeBaja(Presentacion presentacion) {
        if (compraRepositorio.existeCompraActivaConPresentacion(presentacion.getId())) {
            throw new ReglaDominioException("No se puede eliminar una Presentación con compras activas");
        }
        presentacion.darDeBaja();
    }
}
