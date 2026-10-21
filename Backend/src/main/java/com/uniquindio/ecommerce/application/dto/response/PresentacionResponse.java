package com.uniquindio.ecommerce.application.dto.response;

import java.util.List;

// Respuesta de: PublicarPresentacion, PublicarArticuloMerchandising y las operaciones sobre una Presentación publicada (precio, stock, estado, galería, nota de cata).
// Refleja la jerarquía del dominio: cada variante trae solo los campos que le corresponden,
// así un artículo de merchandising no lleva origen ni datos de tueste (ni siquiera como null).
public sealed interface PresentacionResponse
        permits PresentacionTrazableResponse, ArticuloDeMerchandisingResponse {

    String id();

    String vendedorId();

    String titulo();

    String tipo();

    PrecioResponse precio();

    // Precio al que se vende hoy: el precio con el descuento aplicado si rige, y si no, el precio normal.
    PrecioResponse precioVigente();

    // Descuento programado (vigente o no); null si no hay ninguno.
    DescuentoResponse descuento();

    CantidadResponse cantidadDisponible();

    List<ImagenResponse> galeria();

    String estado();

    boolean eliminada();
}
