package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.PorcentajeDeMerma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransformacionTest {

    @Test
    @DisplayName("Debería registrar una Transformación exitosamente cuando todos los datos son válidos")
    void registrar_deberiaCrearTransformacion_cuandoDatosSonValidos() {
        String id = "TRANS-001";
        List lotes = List.of("LOT-2024-001");
        Cantidad cantidadEntrada = new Cantidad(50.0, "kg");
        PorcentajeDeMerma merma = new PorcentajeDeMerma(0.18);
        String tostadorId = "TOST-001";
        LocalDate fecha = LocalDate.now();
        PerfilTueste perfil = PerfilTueste.MEDIO;

        Transformacion transformacion = Transformacion.registrar(
                id,
                lotes,
                cantidadEntrada,
                merma,
                tostadorId,
                fecha,
                perfil
        );

        assertNotNull(transformacion);
        assertEquals(id, transformacion.getId());
        assertEquals(41.0, transformacion.getCantidadResultante().valor(), 0.0001);
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException si la lista de lotes está vacía")
    void registrar_deberiaLanzarExcepcion_cuandoListaLotesEstaVacia() {
        List lotesVacios = Collections.emptyList();
        Cantidad cantidadEntrada = new Cantidad(50.0, "kg");
        PorcentajeDeMerma merma = new PorcentajeDeMerma(0.18);

        assertThrows(ReglaDominioException.class, () ->
                Transformacion.registrar(
                        "TRANS-002",
                        lotesVacios,
                        cantidadEntrada,
                        merma,
                        "TOST-001",
                        LocalDate.now(),
                        PerfilTueste.MEDIO
                )
        );
    }

    @Test
    @DisplayName("Debería lanzar ReglaDominioException si la fecha es futura")
    void registrar_deberiaLanzarExcepcion_cuandoFechaEsFutura() {
        List lotes = List.of("LOT-2024-001");
        Cantidad cantidadEntrada = new Cantidad(50.0, "kg");
        PorcentajeDeMerma merma = new PorcentajeDeMerma(0.18);
        LocalDate fechaFutura = LocalDate.now().plusDays(1);

        assertThrows(ReglaDominioException.class, () ->
                Transformacion.registrar(
                        "TRANS-003",
                        lotes,
                        cantidadEntrada,
                        merma,
                        "TOST-001",
                        fechaFutura,
                        PerfilTueste.MEDIO
                )
        );
    }
}