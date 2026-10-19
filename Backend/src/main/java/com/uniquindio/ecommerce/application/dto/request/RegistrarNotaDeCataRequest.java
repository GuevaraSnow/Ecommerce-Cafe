package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

// Mapea a: RegistrarNotaDeCata.ejecutar(...)
// Solo aplica a una Presentación Trazable (los descriptores sensoriales que escribe el vendedor).
// Que haya al menos un descriptor y ninguno vacío lo aplica el dominio.
public record RegistrarNotaDeCataRequest(
        @NotNull(message = "Los descriptores son obligatorios")
        List<String> descriptores
) {}
