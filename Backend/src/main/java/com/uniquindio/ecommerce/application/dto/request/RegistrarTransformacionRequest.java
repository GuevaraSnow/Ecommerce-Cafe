package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record RegistrarTransformacionRequest(
        @NotBlank(message = "El código de lote es obligatorio")
        String loteCodigo,

        @NotNull(message = "La cantidad de entrada es obligatoria")
        @Valid
        CantidadRequest cantidadEntrada,

        @NotNull(message = "El porcentaje de merma es obligatorio")
        @Positive(message = "El porcentaje de merma debe ser mayor que cero")
        Double merma,

        @NotNull(message = "La fecha de transformación es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El perfil de tueste resultante es obligatorio")
        PerfilTueste perfilTuesteResultante
) {
}