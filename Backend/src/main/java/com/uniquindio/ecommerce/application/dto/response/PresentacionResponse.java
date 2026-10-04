package com.uniquindio.ecommerce.application.dto.response;

import java.util.List;

// Respuesta de: PublicarPresentacion, PublicarArticuloMerchandising y ActualizarPresentacion.
// Refleja la jerarquía del dominio: cada variante trae solo los campos que le corresponden,
// así un artículo de merchandising no lleva origen ni datos de tueste (ni siquiera como null).
public sealed interface PresentacionResponse
        permits PresentacionTrazableResponse, ArticuloDeMerchandisingResponse {

    String id();

    String vendedorId();

    String titulo();

    String tipo();

    PrecioResponse precio();

    CantidadResponse cantidadDisponible();

    List<ImagenResponse> galeria();

    String estado();

    boolean eliminada();
}
