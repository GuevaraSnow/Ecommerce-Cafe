package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Cantidad;
import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransformacionTest {

    @Test
    @DisplayName("Regla Protegida - Debería lanzar ReglaDominioException cuando la lista de Lotes de origen está vacía")
    void registrar_deberiaLanzarExcepcion_cuandoListaDeLotesEstaVacia() {

        String idTransformacion = "TRANS-100";
        List<String> lotesVacios = Collections.emptyList(); // Lista de lotes vacía
        Cantidad cantidadUsada = new Cantidad(5.0, "kg");
        String tostadorId = "TOST-001";
        LocalDate fecha = LocalDate.now();


        PerfilTueste perfilTueste = PerfilTueste.MEDIO;

        ReglaDominioException excepcion = assertThrows(
                ReglaDominioException.class,
                () -> Transformacion.registrar(
                        idTransformacion,
                        lotesVacios,
                        cantidadUsada,
                        tostadorId,
                        fecha,
                        perfilTueste
                )
        );


        assertEquals("La Transformación debe usar al menos un Lote de origen", excepcion.getMessage());
    }
}