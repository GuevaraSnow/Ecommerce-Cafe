package com.uniquindio.ecommerce.application.dto.response;

import java.util.List;

public record ArticuloDeMerchandisingResponse(
        String id,
        String vendedorId,
        String titulo,
        String tipo,
        PrecioResponse precio,
        CantidadResponse cantidadDisponible,
        List<ImagenResponse> galeria,
        String estado,
        boolean eliminada,
        String descripcion,
        String material
) implements PresentacionResponse {}
