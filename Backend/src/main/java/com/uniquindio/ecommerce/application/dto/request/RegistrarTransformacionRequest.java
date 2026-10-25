package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RegistrarTransformacionRequest(
        @NotBlank(message = "El código de lote es obligatorio")
        String loteCodigo,

        @NotNull(message = "La cantidad de entrada es obligatoria")
        CantidadRequest cantidadEntrada,

        @NotNull(message = "El porcentaje de merma es obligatorio")
        Double merma,

        @NotBlank(message = "El identificador del tostador es obligatorio")
        String tostadorId,

        @NotNull(message = "La fecha de transformación es obligatoria")
        LocalDate fecha,

        @NotNull(message = "El perfil de tueste resultante es obligatorio")
        PerfilTueste perfilTuesteResultante
) {
}