package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record CodigoDeLote(String valor) {
    public CodigoDeLote {
        if (valor == null || !valor.matches("LOT-\\\\d{4}-\\\\d{3}")) {
            throw new ReglaDominioException("Código de Lote inválido: " + valor);
        }
    }
}
