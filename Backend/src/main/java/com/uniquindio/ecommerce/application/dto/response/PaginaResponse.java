package com.uniquindio.ecommerce.application.dto.response;

import java.util.List;

// Una página de resultados; la primera página es la 0.
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {}
