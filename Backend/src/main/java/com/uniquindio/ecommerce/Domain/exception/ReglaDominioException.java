package com.uniquindio.ecommerce.Domain.exception;

/**
 * Señala la violación de una regla de negocio del dominio para diferenciarla de errores
 * técnicos o de infraestructura.
 */
public class ReglaDominioException extends RuntimeException{
    public ReglaDominioException(String mensaje) {
        super(mensaje);
    }
}
