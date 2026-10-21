package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.service.VerificadorDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.Calificacion;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: DejarResena.
 * (Actor: Comprador) — deja una Reseña sobre una Presentación que adquirió. No tiene lógica:
 * {@link VerificadorDeCompra} exige la Compra entregada y que no la haya reseñado antes, y la
 * Reseña guarda al vendedor dueño de la Presentación para que solo él pueda responderla.
 */
@Service
@RequiredArgsConstructor
public class DejarResena {

    private final PresentacionRepositorio presentacionRepositorio;
    private final ResenaRepositorio resenaRepositorio;
    private final VerificadorDeCompra verificadorDeCompra;

    @Transactional
    public Resena ejecutar(String id, String compradorId, String presentacionId,
                           Calificacion calificacion, TextoDeResena texto) {
        Presentacion presentacion = presentacionRepositorio.buscarPorId(presentacionId).orElseThrow();
        verificadorDeCompra.verificarQuePuedeResenar(compradorId, presentacionId);
        Resena resena = Resena.dejar(id, compradorId, presentacionId, presentacion.getVendedorId(),
                calificacion, texto, LocalDateTime.now());
        resenaRepositorio.guardar(resena);
        return resena;
    }
}
