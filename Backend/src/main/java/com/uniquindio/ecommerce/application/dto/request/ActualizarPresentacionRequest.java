package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.Valid;

import java.util.List;

// Mapea a: ActualizarPresentacion.ejecutar(...)
// Todos los campos son opcionales: solo se aplica el cambio de los que vengan.
//  - nuevaCantidadTotal es la cantidad final que debe quedar (no un delta).
//  - activar: true reactiva una Presentación pausada, false la pausa, null no toca el estado.
//  - nuevaNotaCata solo aplica a una Presentación Trazable (los descriptores sensoriales).
public record ActualizarPresentacionRequest(
        @Valid
        PrecioRequest nuevoPrecio,

        @Valid
        CantidadRequest nuevaCantidadTotal,

        Boolean activar,

        List<@Valid ImagenRequest> nuevaGaleria,

        List<String> nuevaNotaCata
) {}
