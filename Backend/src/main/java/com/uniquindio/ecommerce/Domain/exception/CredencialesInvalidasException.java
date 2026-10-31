package com.uniquindio.ecommerce.Domain.exception;

/**
 * El inicio de sesión falló. Se lanza con el mismo mensaje sea cual sea la causa (el correo no existe, la
 * contraseña no coincide o el usuario está dado de baja) para no revelar si un correo está registrado.
 */
public class CredencialesInvalidasException extends ReglaDominioException {

    public static final String MENSAJE = "Credenciales inválidas.";

    public CredencialesInvalidasException() {
        super(MENSAJE);
    }
}
