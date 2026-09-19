package com.uniquindio.ecommerce.Domain.valueobject;

/**
 * Método usado para retirar la pulpa y secar el grano después de la
 * cosecha. Es un atributo propio del Lote: algunas Fincas se especializan
 * en un solo proceso porque requiere equipos o conocimiento particular,
 * y define en buena parte el perfil de sabor final del café.
 */
public enum ProcesoDeBeneficio {
    /** Toda la pulpa se retira antes del secado; da un sabor más limpio y ácido. */
    Lavado,
    /** Se retira parte de la pulpa, dejando algo de mucílago durante el secado; sabor más dulce y con cuerpo. */
    Honey,
    /** El grano se seca con toda la pulpa intacta (café en cereza); sabor más afrutado e intenso. */
    Natural
}