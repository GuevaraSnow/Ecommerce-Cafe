package com.uniquindio.ecommerce.Domain.valueobject;

/**
 * Rol del Vendedor que publica una Presentación. Se usa como dato de
 * validación al publicar (regla A); el Vendedor en sí es un agregado
 * aparte, referenciado solo por vendedorId.
 */
public enum RolVendedor {
    CAFICULTOR,
    TOSTADOR
}
