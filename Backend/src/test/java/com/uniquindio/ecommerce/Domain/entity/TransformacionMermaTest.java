package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransformacionMermaTest {

    @Test
    @DisplayName("Caso Café del Valle: 50kg de entrada con 18% de merma debe resultar en 41kg")
    void registrar_deberiaCalcular41kgResultantes_paraCasoCafeDelValle() {
        // ARRANGE
        String idTransformacion = "TRANS-VALLE-001";
        List lotesOrigen = List.of("LOT-2024-001");
        Cantidad cantidadEntrada = new Cantidad(50.0, "kg");
        PorcentajeDeMerma merma18PorCiento = new PorcentajeDeMerma(0.18);
        String tostadorId = "TOST-001";
        LocalDate fecha = LocalDate.now();
        PerfilTueste perfil = PerfilTueste.MEDIO;

        // ACT
        Transformacion transformacion = Transformacion.registrar(
                idTransformacion,
                lotesOrigen,
                cantidadEntrada,
                merma18PorCiento,
                tostadorId,
                fecha,
                perfil
        );

        // ASSERT: 50 * (1 - 0.18) = 50 * 0.82 = 41.0 kg
        assertEquals(41.0, transformacion.getCantidadResultante().valor(), 0.0001);
        assertEquals("kg", transformacion.getCantidadResultante().unidad());
        assertEquals(41.0, transformacion.getCantidadResultanteDisponible().valor(), 0.0001);
    }
}