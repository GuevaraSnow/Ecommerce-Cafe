package com.uniquindio.ecommerce.application.dto.response;

import java.time.LocalDate;
import java.util.List;

// perfilTueste y fechaTueste solo vienen en el café tostado; notaCata solo si ya se registró.
public record PresentacionTrazableResponse(
        String id,
        String vendedorId,
        String titulo,
        String tipo,
        PrecioResponse precio,
        CantidadResponse cantidadDisponible,
        List<ImagenResponse> galeria,
        String estado,
        boolean eliminada,
        OrigenResponse origen,
        String perfilTueste,
        LocalDate fechaTueste,
        List<String> notaCata
) implements PresentacionResponse {}
