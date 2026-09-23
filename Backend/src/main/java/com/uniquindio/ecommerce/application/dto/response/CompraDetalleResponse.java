package com.uniquindio.ecommerce.application.dto.response;

import java.time.LocalDate;

public record CompraDetalleResponse(
        String id,
        String presentacionId,
        String compradorId,
        double montoPrecio,
        String estado,
        LocalDate fechaCompra

) {}
