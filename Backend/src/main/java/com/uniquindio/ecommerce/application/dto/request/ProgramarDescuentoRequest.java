package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Mapea a: ProgramarDescuentoDePresentacion.ejecutar(...)
// Solo valida la forma; que el porcentaje esté entre 1 y 50 y que las fechas no estén invertidas lo aplica el dominio.
public record ProgramarDescuentoRequest(
        @NotNull(message = "El porcentaje es obligatorio")
        Integer porcentaje,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin
) {}
