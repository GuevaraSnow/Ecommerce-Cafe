package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record Altitud(double metrosSobreNivelMar) {

    public Altitud {
        if (metrosSobreNivelMar <= 0 ) {
            throw new ReglaDominioException("La altura debe ser mayor que 0");
        }
    }

}
