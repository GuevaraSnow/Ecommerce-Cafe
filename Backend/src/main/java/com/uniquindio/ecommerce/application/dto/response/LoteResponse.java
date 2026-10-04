package com.uniquindio.ecommerce.application.dto.response;

// Respuesta de: RegistrarLote (representación de un Lote)
public record LoteResponse(
        String codigo,
        String fincaId,
        CantidadResponse cantidadDisponible,
        String estado,
        String procesoDeBeneficio,
        String variedad,
        CosechaResponse cosecha,
        boolean eliminadoLogicamente
) {

    public record CantidadResponse(double valor, String unidad) {}

    public record CosechaResponse(int anio, String temporada) {}
}
