package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record CodigoDeLote(String valor) {
    public CodigoDeLote {
        if (valor == null || !valor.matches("LOT-\\d{4}-\\d{3}")) {
            throw new ReglaDominioException("Código de Lote inválido: " + valor);
        }
    }

    public int anio() {
        String codigo = valor;
        String[] split = codigo.split("-");
        return Integer.parseInt(split[1]);
    }

    public int numero() {
        String codigo = valor;
        String[] split = codigo.split("-");
        return Integer.parseInt(split[2]);
    }
}
