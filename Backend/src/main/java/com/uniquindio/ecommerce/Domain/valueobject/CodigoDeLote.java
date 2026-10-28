package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record CodigoDeLote(String valor) {
    public CodigoDeLote {
        if (valor == null || !valor.matches("LOT-\\d{4}-\\d{3}")) {
            throw new ReglaDominioException("Código de Lote inválido: " + valor);
        }
    }

    public static CodigoDeLote crear(int anio, int numero){
        if(anio < 1000 || anio > 9999){
            throw new ReglaDominioException("El año debe tener 4 cifras");
        }
        if(numero <= 0){
            throw new ReglaDominioException("El numero debe ser positivo y mayor que 0");
        }
        if(numero>999){
            throw new ReglaDominioException("Se alcanzo el maximo numero de lotes para este año");
        }
        String valor = "LOT-"+anio+"-"+String.format("%03d", numero);

        return new CodigoDeLote(valor);
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
