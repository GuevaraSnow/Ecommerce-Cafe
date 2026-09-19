package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;


public class RegistrarFinca {

    public Finca ejecutar(String id, String propietarioId, String nombre, Ubicacion ubicacion, Altitud altitud){
        return Finca.registrar(id,propietarioId,nombre,ubicacion,altitud);

    }
}
