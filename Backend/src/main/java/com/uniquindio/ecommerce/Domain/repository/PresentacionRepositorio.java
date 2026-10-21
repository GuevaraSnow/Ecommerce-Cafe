package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;

import java.util.List;
import java.util.Optional;

public interface PresentacionRepositorio {
    Optional<Presentacion> buscarPorId(String id);

    void guardar(Presentacion presentacion);

    /**
     * Presentaciones disponibles para la venta (activas y no dadas de baja) que cumplen el
     * filtro, ordenadas por título. La página 0 es la primera.
     */
    Pagina<Presentacion> buscarDisponibles(FiltroDePresentaciones filtro, int pagina, int tamano);

    /**
     * Presentaciones disponibles para la venta cuyo título contiene el texto, sin distinguir
     * mayúsculas, ordenadas por título y limitadas a {@code limite}. Alimenta la búsqueda predictiva.
     */
    List<Presentacion> sugerirDisponiblesPorTitulo(String texto, int limite);
}
