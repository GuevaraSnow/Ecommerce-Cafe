package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: BuscarPresentaciones.
 * (Actor: cualquier visitante) — lista, por páginas, las Presentaciones disponibles para la venta que
 * cumplen los filtros; no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class BuscarPresentaciones {

    private final PresentacionRepositorio repositorio;

    @Transactional(readOnly = true)
    public Pagina<Presentacion> ejecutar(FiltroDePresentaciones filtro, int pagina, int tamano) {
        return repositorio.buscarDisponibles(filtro, pagina, tamano);
    }
}
