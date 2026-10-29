package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeLote;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

@Service
@RequiredArgsConstructor
public class RegistrarLote {

    private final LoteRepositorio repositorio;
    private final ServicioDeLote servicioDeLote;

    @Transactional
    public Lote ejecutar(Cantidad cantidadDisponible, String fincaId,
                          ProcesoDeBeneficio procesoDeBeneficio, EstadoDelCafe estado,
                          Cosecha cosecha, VariedadDeCafe variedad) {
        Lote lote = servicioDeLote.registrar(Year.now().getValue(),
                cantidadDisponible, fincaId, procesoDeBeneficio, estado, cosecha, variedad
        );
        repositorio.guardar(lote);
        return lote;
    }
}
