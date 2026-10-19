package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.PresentacionTrazable;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.repository.TransformacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;

/**
 * Domain Service que orquesta la reposición de stock de una Presentación.
 * Una Presentación Trazable no puede crecer sin que el café salga de su
 * Origen (regla 13): reponer 10 kg descuenta 10 kg del Lote o de la
 * Transformación de donde vino. El merchandising no tiene Origen y se repone
 * directamente. Sin estado y sin anotaciones de Spring.
 */
public class ServicioDeReposicion {

    private final DescuentoDeOrigen descuentoDeOrigen;

    public ServicioDeReposicion(LoteRepositorio loteRepositorio,
                                TransformacionRepositorio transformacionRepositorio) {
        this.descuentoDeOrigen = new DescuentoDeOrigen(loteRepositorio, transformacionRepositorio);
    }

    /**
     * Primero verifica que la Presentación admite la reposición, luego descuenta
     * del Origen y por último repone. Si algo falla, nada cambia.
     *
     * @throws ReglaDominioException si la Presentación no admite la reposición o el Origen no alcanza
     */
    public void reponer(Presentacion presentacion, Cantidad cantidad) {
        presentacion.verificarQueSePuedeReponer(cantidad);
        if (presentacion instanceof PresentacionTrazable trazable) {
            descuentoDeOrigen.descontar(trazable.getOrigen(), cantidad, trazable.getPerfilDeTueste());
        }
        presentacion.reponerCantidad(cantidad);
    }
}
