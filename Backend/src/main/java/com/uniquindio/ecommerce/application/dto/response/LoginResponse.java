package com.uniquindio.ecommerce.application.dto.response;

public record LoginResponse(
        String token,
        UsuarioResponse usuario
) {
}