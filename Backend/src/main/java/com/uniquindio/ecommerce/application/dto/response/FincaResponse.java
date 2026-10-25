package com.uniquindio.ecommerce.application.dto.response;

public record FincaResponse(
        String id,
        String propietarioId,
        String nombre,
        String departamento,
        String municipio,
        Integer altitud,
        boolean eliminada
) {
}