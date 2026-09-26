package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;

public record OrigenDePresentacion(String loteId, String transformacionId){
    public OrigenDePresentacion {
        boolean tieneLote = loteId != null && !loteId.isBlank();
        boolean tieneTransformacion = transformacionId != null && !transformacionId.isBlank();
        if (tieneLote == tieneTransformacion) {
            throw new ReglaDominioException(
                    "El Origen de Presentación debe provenir de un Lote o de una Transformación, no de ambos ni de ninguno");
        }
    }

    public static OrigenDePresentacion desde(String loteId, String transformacionId) {
        return new OrigenDePresentacion(loteId, transformacionId);
    }
}