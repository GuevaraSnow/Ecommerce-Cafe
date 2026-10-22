package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDateTime;

/**
 * Domain Service del reembolso de café físico, que cruza Compra, Presentación y Solicitud de
 * Reembolso. El reembolso es por Presentación: pedir el de todo el carrito crea una solicitud
 * por cada una, y cada vendedor resuelve solo las suyas. Sin estado y sin anotaciones de Spring.
 */
public class ServicioDeReembolso {

    /**
     * Crea la Solicitud de Reembolso de una Presentación de la Compra. La Compra verifica que la
     * pida quien compró, que esté entregada, que sea dentro de las 48 h y que incluya la Presentación;
     * la solicitud guarda al vendedor dueño de la Presentación para que solo él la resuelva.
     *
     * @throws ReglaDominioException si no se puede pedir el reembolso; en ese caso no se crea nada
     */
    public SolicitudReembolso solicitar(String id, Compra compra, String compradorId, Presentacion presentacion,
                                        String motivo, LocalDateTime momento) {
        compra.verificarQueSePuedeReembolsar(compradorId, presentacion.getId(), momento);
        return SolicitudReembolso.solicitar(id, compra.getId(), presentacion.getId(), presentacion.getVendedorId(),
                motivo, momento);
    }
}
