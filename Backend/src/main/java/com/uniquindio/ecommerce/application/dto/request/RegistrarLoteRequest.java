package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDelCafe;
import com.uniquindio.ecommerce.Domain.valueobject.ProcesoDeBeneficio;
import com.uniquindio.ecommerce.Domain.valueobject.TemporadaDeCosecha;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Mapea a: RegistrarLote.ejecutar(...)
// Solo valida la forma de la entrada; las reglas de negocio (cantidad positiva,
// estado distinto de TOSTADO) las aplica el dominio.
public record RegistrarLoteRequest(
        @NotNull(message = "La cantidad es obligatoria")
        @Valid
        CantidadRequest cantidad,

        @NotBlank(message = "La finca es obligatoria")
        String fincaId,

        @NotNull(message = "El proceso de beneficio es obligatorio")
        ProcesoDeBeneficio procesoDeBeneficio,

        @NotNull(message = "El estado del café es obligatorio")
        EstadoDelCafe estado,

        @NotNull(message = "La cosecha es obligatoria")
        @Valid
        CosechaRequest cosecha,

        @NotBlank(message = "La variedad es obligatoria")
        String variedad
) {

    public record CosechaRequest(
            @NotNull(message = "El año de la cosecha es obligatorio")
            Integer anio,

            @NotNull(message = "La temporada de la cosecha es obligatoria")
            TemporadaDeCosecha temporada
    ) {}
}
