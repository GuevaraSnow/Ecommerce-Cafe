package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Resena;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.TextoDeResena;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Caso de uso: ResponderResena.
 * (Actor: Vendedor) — responde una Reseña de una de sus Presentaciones. No tiene lógica:
 * Resena.responder() exige que sea el vendedor dueño y que no la haya respondido antes.
 */
@Service
@RequiredArgsConstructor
public class ResponderResena {

    private final ResenaRepositorio resenaRepositorio;

    @Transactional
    public Resena ejecutar(String resenaId, String vendedorId, TextoDeResena texto) {
        Resena resena = resenaRepositorio.buscarPorId(resenaId).orElseThrow();
        resena.responder(vendedorId, texto, LocalDateTime.now());
        resenaRepositorio.guardar(resena);
        return resena;
    }
}
