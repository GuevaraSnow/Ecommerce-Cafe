package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: RechazarReembolso.
 * (Actor: Vendedor) — rechaza el reembolso de una de sus Presentaciones. No tiene lógica:
 * SolicitudReembolso.rechazar() exige que sea el dueño y que no se haya resuelto antes. Solo toca
 * la Solicitud, así que no necesita un Domain Service; la Compra no cambia.
 */
@Service
@RequiredArgsConstructor
public class RechazarReembolso {

    private final SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;

    @Transactional
    public SolicitudReembolso ejecutar(String solicitudId, String vendedorId) {
        SolicitudReembolso solicitud = solicitudReembolsoRepositorio.buscarPorId(solicitudId).orElseThrow();
        solicitud.rechazar(vendedorId, LocalDateTime.now());
        solicitudReembolsoRepositorio.guardar(solicitud);
        return solicitud;
    }
}
