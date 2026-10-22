package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.SolicitudReembolso;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.SolicitudReembolsoRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeReembolso;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: SolicitarReembolso.
 * (Actor: Comprador) — pide el reembolso de una Presentación de una Compra entregada. No tiene
 * lógica: {@link ServicioDeReembolso} exige que la pida quien compró, dentro de las 48 h, que la
 * Presentación esté en la Compra y que no haya otra solicitud vigente de ella.
 */
@Service
@RequiredArgsConstructor
public class SolicitarReembolso {

    private final CompraRepositorio compraRepositorio;
    private final PresentacionRepositorio presentacionRepositorio;
    private final SolicitudReembolsoRepositorio solicitudReembolsoRepositorio;
    private final ServicioDeReembolso servicioDeReembolso;

    @Transactional
    public SolicitudReembolso ejecutar(String id, String compraId, String compradorId, String presentacionId,
                                       String motivo) {
        Compra compra = compraRepositorio.buscarPorId(compraId).orElseThrow();
        Presentacion presentacion = presentacionRepositorio.buscarPorId(presentacionId).orElseThrow();
        SolicitudReembolso solicitud = servicioDeReembolso.solicitar(id, compra, compradorId, presentacion, motivo,
                LocalDateTime.now());
        solicitudReembolsoRepositorio.guardar(solicitud);
        return solicitud;
    }
}
