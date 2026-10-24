package com.uniquindio.ecommerce.application.dto.response;

public record UsuarioResponse(
        String id,
        String nombre,
        String email,
        String telefono,
        String rol
) {}