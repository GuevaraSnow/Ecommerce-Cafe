package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

/**
 * Código único de un Lote, con el formato LOT-AAAA-NNN: el año de registro y un número consecutivo de ese
 * año, de 001 a 999. El servidor lo genera con {@link #crear(int, int)}; el constructor valida el formato
 * al reconstruirlo desde un texto.
 */
public record CodigoDeLote(String valor) {
    public CodigoDeLote {
        if (valor == null || !valor.matches("LOT-\\d{4}-\\d{3}")) {
            throw new ReglaDominioException("Código de Lote inválido: " + valor);
        }
    }

    /** Arma el código del número {@code numero} del año {@code anio}; el número va rellenado con ceros. */
    public static CodigoDeLote crear(int anio, int numero){
        if(anio < 1000 || anio > 9999){
            throw new ReglaDominioException("El año debe tener 4 cifras");
        }
        if(numero <= 0){
            throw new ReglaDominioException("El número debe ser positivo y mayor que 0");
        }
        if(numero>999){
            throw new ReglaDominioException("Se alcanzó el máximo número de Lotes para este año");
        }
        String valor = "LOT-"+anio+"-"+String.format("%03d", numero);

        return new CodigoDeLote(valor);
    }

    /** El año de registro, la segunda parte del código. */
    public int anio() {
        return Integer.parseInt(valor.split("-")[1]);
    }

    /** El número consecutivo dentro del año, la tercera parte del código. */
    public int numero() {
        return Integer.parseInt(valor.split("-")[2]);
    }
}
