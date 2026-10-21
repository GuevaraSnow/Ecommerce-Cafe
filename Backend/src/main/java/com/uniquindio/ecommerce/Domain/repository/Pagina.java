package com.uniquindio.ecommerce.Domain.repository;

import java.util.List;
import java.util.function.Function;

/**
 * Una página de resultados de una consulta: el contenido de esa página y los
 * totales necesarios para pedir las siguientes. La primera página es la 0.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos) {

    public int totalPaginas() {
        return (int) Math.ceil((double) totalElementos / tamano);
    }

    /** Convierte el contenido conservando la información de paginación. */
    public <R> Pagina<R> mapear(Function<T, R> conversion) {
        return new Pagina<>(contenido.stream().map(conversion).toList(), pagina, tamano, totalElementos);
    }
}
