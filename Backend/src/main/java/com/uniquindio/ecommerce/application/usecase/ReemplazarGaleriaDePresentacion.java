package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Galeria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ReemplazarGaleriaDePresentacion.
 * (Actor: Vendedor) — reemplaza la galería de imágenes de una Presentación. No tiene lógica: delega
 * en Presentacion.reemplazarGaleria().
 */
@Service
@RequiredArgsConstructor
public class ReemplazarGaleriaDePresentacion {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public Presentacion ejecutar(String presentacionId, Galeria nuevaGaleria) {
        Presentacion presentacion = repositorio.buscarPorId(presentacionId)
                .orElseThrow(() -> new ReglaDominioException("No existe una Presentación con id " + presentacionId));
        presentacion.reemplazarGaleria(nuevaGaleria);
        repositorio.guardar(presentacion);
        return presentacion;
    }
}
