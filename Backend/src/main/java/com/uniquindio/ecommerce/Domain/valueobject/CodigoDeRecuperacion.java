package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Value Object para la recuperación de contraseña
 * Genera un codigo numerico que se expira en 15 minutos
 */
public record CodigoDeRecuperacion(String codigo, LocalDateTime fechaExpiracion) {

    public CodigoDeRecuperacion {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaDominioException("El código de recuperación no puede estar vacío.");
        }
        if (fechaExpiracion == null) {
            throw new ReglaDominioException("La fecha de expiración es obligatoria.");
        }
    }


    public static CodigoDeRecuperacion generar() {
        String codigoGenerado = String.format("%06d", new Random().nextInt(900000) + 100000);
        return new CodigoDeRecuperacion(codigoGenerado, LocalDateTime.now().plusMinutes(15));
    }


    public boolean estaVigente() {
        return LocalDateTime.now().isBefore(fechaExpiracion);
    }
}