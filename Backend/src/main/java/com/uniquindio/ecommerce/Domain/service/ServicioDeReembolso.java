package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;

import java.time.LocalDateTime;

/**
 * Domain Service del reembolso de café físico, que cruza Compra, Presentación y Solicitud de
 * Reembolso. El reembolso es por Presentación: pedir el de todo el carrito crea una solicitud
 * por cada una, y cada vendedor resuelve solo las suyas. Sin estado y sin anotaciones de Spring.
 */
public class ServicioDeReembolso {

    private final SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    public ServicioDeReembolso(SolicitudReembolsoRepositorio solicitudReembolsoRepositorio) {
        this.solicitudReembolsoRepositorio = solicitudReembolsoRepositorio;
    }

    /**
     * Crea la Solicitud de Reembolso de una Presentación de la Compra. La Compra verifica que la
     * pida quien compró, que esté entregada, que sea dentro de las 48 h y que incluya la Presentación;
     * no puede haber otra solicitud pendiente o aprobada de esa Presentación en esa Compra (si se
     * la rechazaron, puede volver a pedirla). La solicitud guarda al vendedor dueño de la
     * Presentación para que solo él la resuelva.
     *
     * @throws ReglaDominioException si no se puede pedir el reembolso; en ese caso no se crea nada
     */
    public SolicitudReembolso solicitar(String id, Compra compra, String compradorId, Presentacion presentacion,
                                        String motivo, LocalDateTime momento) {
        compra.verificarQueSePuedeReembolsar(compradorId, presentacion.getId(), momento);
        if (solicitudReembolsoRepositorio.existeSolicitudVigente(compra.getId(), presentacion.getId())) {
            throw new ReglaDominioException("Ya hay una solicitud de reembolso pendiente o aprobada de esta Presentacion");
        }
        return SolicitudReembolso.solicitar(id, compra.getId(), presentacion.getId(), presentacion.getVendedorId(),
                motivo, momento);
    }

    /**
     * El vendedor dueño de la Presentación aprueba la solicitud. Si con esta aprobación todas las
     * Presentaciones de la Compra quedan reembolsadas, la Compra pasa a REEMBOLSADA; con un
     * reembolso parcial (por ejemplo 3 de 4) la Compra sigue ENTREGADA. El stock no se devuelve.
     *
     * @throws ReglaDominioException si no es el dueño, la solicitud ya se resolvió o no es de esa Compra;
     *                               en ese caso nada cambia
     */
    public void aprobar(SolicitudReembolso solicitud, Compra compra, String vendedorId, LocalDateTime momento) {
        if (!solicitud.getCompraId().equals(compra.getId())) {
            throw new ReglaDominioException("La solicitud de reembolso no pertenece a esta compra");
        }
        solicitud.aprobar(vendedorId, momento);
        boolean todasReembolsadas = compra.getDetalles().stream()
                .allMatch(detalle -> detalle.presentacionId().equals(solicitud.getPresentacionId())
                        || solicitudReembolsoRepositorio.existeSolicitudAprobada(compra.getId(), detalle.presentacionId()));
        if (todasReembolsadas) {
            compra.reembolsar();
        }
    }
}
