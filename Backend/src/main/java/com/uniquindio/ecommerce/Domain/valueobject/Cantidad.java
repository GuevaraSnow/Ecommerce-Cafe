package com.uniquindio.ecommerce.Domain.valueobject;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

import java.util.Set;

public record Cantidad(double valor, String unidad) {

    private static final Set<String> UNIDADES_VALIDAS = Set.of("kg", "g", "ml");

    public Cantidad {
        if (valor < 0) {
            throw new ReglaDominioException("La Cantidad no puede ser negativa: " + valor);
        }
        if(unidad == null || !UNIDADES_VALIDAS.contains(unidad)) {
            throw new ReglaDominioException("Unidad de Cantidad inválida: " + unidad);
        }
    }

    /**
     * Resta otra Cantidad de la misma unidad y devuelve una NUEVA Cantidad.
     * No modifica esta instancia.
     * Lanza excepción si el resultado quedaría negativo o si las unidades no coinciden.
     */
    public Cantidad restar (Cantidad otra){
        validarMismaUnidad(otra);
        double resultado = this.valor - otra.valor;
        if(resultado < 0){
            throw new ReglaDominioException( "No hay suficiente cantidad disponible: se intentó restar " + otra.valor
                    + " " + unidad + " de solo " + this.valor + " " + unidad + " disponibles.");
        }
        return new Cantidad(resultado, unidad);
    }

    public Cantidad sumar(Cantidad otra){
        validarMismaUnidad(otra);
        return new Cantidad(this.valor + otra.valor, unidad);
    }

    private void validarMismaUnidad(Cantidad otra){
        if (!this.unidad.equals(otra.unidad)) {
            throw new ReglaDominioException("No se pueden operar Cantidades con unidades distintas: "
                    + this.unidad + " vs " + otra.unidad);
        }
    }
}
