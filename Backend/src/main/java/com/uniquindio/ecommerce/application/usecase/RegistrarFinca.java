package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegistrarFinca {

    private final FincaRepositorio fincaRepositorio;

    @Transactional
    public Finca ejecutar(String id, String propietarioId, String nombre, Ubicacion ubicacion, Altitud altitud) {
        Finca finca = Finca.registrar(id, propietarioId, nombre, ubicacion, altitud);
        fincaRepositorio.guardar(finca);
        return finca;
    }
}