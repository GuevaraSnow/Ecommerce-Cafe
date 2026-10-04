package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import com.uniquindio.ecommerce.Domain.valueobject.NotaDeCata;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;

/**
 * Caso de uso: ActualizarPresentacion.
 * (Actor: Vendedor) — modifica precio, stock o estado de una Presentación
 * ya publicada. Cada parámetro es opcional: solo se aplica el cambio si no
 * viene nulo.
 */
public class ActualizarPresentacion {

    private final PresentacionRepositorio repositorio;

    public ActualizarPresentacion(PresentacionRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * @param nuevaCantidadTotal cantidad total que debe quedar disponible (no un delta);
     *                           el caso de uso calcula si repone o descuenta
     * @param activar true para reactivar una Presentación Inactiva, false para pausarla,
     *                null para no tocar el estado
     * @param nuevaNotaCata solo aplica a una Presentación Trazable
     */
    public Presentacion ejecutar(String id, Precio nuevoPrecio, Cantidad nuevaCantidadTotal, Boolean activar,
                                  Galeria nuevaGaleria, NotaDeCata nuevaNotaCata) {
        Presentacion presentacion = repositorio.buscarPorId(id)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + id));

        if (presentacion instanceof PresentacionTrazable trazable) {
            trazable.verificarVencimiento();
        }

        if (nuevoPrecio != null) {
            presentacion.cambiarPrecio(nuevoPrecio);
        }

        if (nuevaCantidadTotal != null) {
            Cantidad actual = presentacion.getCantidadDisponible();
            double diferencia = nuevaCantidadTotal.valor() - actual.valor();
            if (diferencia > 0) {
                presentacion.reponerCantidad(new Cantidad(diferencia, nuevaCantidadTotal.unidad()));
            } else if (diferencia < 0) {
                presentacion.descontarCantidad(new Cantidad(-diferencia, nuevaCantidadTotal.unidad()));
            }
        }

        if (nuevaGaleria != null) {
            presentacion.reemplazarGaleria(nuevaGaleria);
        }

        if (nuevaNotaCata != null) {
            if (!(presentacion instanceof PresentacionTrazable trazable)) {
                throw new ReglaDominioException("Solo una Presentación Trazable admite Nota de Cata");
            }
            trazable.registrarNotaCata(nuevaNotaCata);
        }

        if (activar != null) {
            if (activar) {
                presentacion.activar();
            } else {
                presentacion.desactivar();
            }
        }

        repositorio.guardar(presentacion);
        return presentacion;
    }
}
