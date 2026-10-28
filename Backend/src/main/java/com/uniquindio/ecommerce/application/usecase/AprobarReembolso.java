package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: AprobarReembolso.
 * (Actor: Vendedor) — aprueba el reembolso de una de sus Presentaciones. No tiene lógica:
 * {@link ServicioDeReembolso} exige que sea el dueño y pasa la Compra a REEMBOLSADA solo cuando
 * todas sus Presentaciones quedan aprobadas.
 */
@Service
@RequiredArgsConstructor
public class AprobarReembolso {

    private final SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;
    private final CompraRepositorio compraRepositorio;
    private final ServicioDeReembolso servicioDeReembolso;

    @Transactional
    public SolicitudReembolso ejecutar(String solicitudId, String vendedorId) {
        SolicitudReembolso solicitud = solicitudReembolsoRepositorio.buscarPorId(solicitudId).orElseThrow();
        Compra compra = compraRepositorio.buscarPorId(solicitud.getCompraId()).orElseThrow();
        servicioDeReembolso.aprobar(solicitud, compra, vendedorId, LocalDateTime.now());
        solicitudReembolsoRepositorio.guardar(solicitud);
        compraRepositorio.guardar(compra);
        return solicitud;
    }
}
