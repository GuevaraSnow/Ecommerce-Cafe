package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.EstadoDelCafe;
import com.uniquindio.ecommerce.Domain.valueobject.ProcesoDeBeneficio;
import com.uniquindio.ecommerce.Domain.valueobject.TemporadaDeCosecha;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest.CantidadRequest;
import com.uniquindio.ecommerce.application.dto.request.RegistrarLoteRequest.CosechaRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que las anotaciones del Request se disparan; las reglas de negocio las valida el dominio. */
class RegistrarLoteRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(RegistrarLoteRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private RegistrarLoteRequest requestValido() {
        return new RegistrarLoteRequest("LOT-2026-045", new CantidadRequest(200.0, "kg"), "finca-1",
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new CosechaRequest(2026, TemporadaDeCosecha.TRAVIESA), "Caturra");
    }

    @Test
    void unRequestCompletoNoTieneViolaciones() {
        assertTrue(camposInvalidos(requestValido()).isEmpty());
    }

    @Test
    void losCamposObligatoriosVaciosSeReportan() {
        RegistrarLoteRequest vacio = new RegistrarLoteRequest(" ", null, " ", null, null, null, " ");

        assertEquals(Set.of("codigo", "cantidad", "fincaId", "procesoDeBeneficio", "estado", "cosecha", "variedad"),
                camposInvalidos(vacio));
    }

    @Test
    void laValidacionBajaHastaLosCamposAnidados() {
        RegistrarLoteRequest anidadosVacios = new RegistrarLoteRequest("LOT-2026-045",
                new CantidadRequest(null, " "), "finca-1", ProcesoDeBeneficio.Lavado, EstadoDelCafe.VERDE,
                new CosechaRequest(null, null), "Caturra");

        assertEquals(Set.of("cantidad.valor", "cantidad.unidad", "cosecha.anio", "cosecha.temporada"),
                camposInvalidos(anidadosVacios));
    }
}
