package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que las anotaciones de los Requests de Compra se disparan; las reglas de negocio las valida el dominio. */
class CompraRequestsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void agregarAlCarritoCompletoNoTieneViolaciones() {
        assertTrue(camposInvalidos(new AgregarAlCarritoRequest("pres-1", new CantidadRequest(2.0, "kg"))).isEmpty());
    }

    @Test
    void agregarAlCarritoSinPresentacionNiCantidadMarcaAmbosCampos() {
        assertEquals(Set.of("presentacionId", "cantidad"), camposInvalidos(new AgregarAlCarritoRequest(" ", null)));
    }

    @Test
    void agregarAlCarritoValidaLaCantidadPorDentro() {
        assertEquals(Set.of("cantidad.valor", "cantidad.unidad"),
                camposInvalidos(new AgregarAlCarritoRequest("pres-1", new CantidadRequest(-1.0, ""))));
    }

    @Test
    void direccionDeEnvioCompletaNoTieneViolaciones() {
        assertTrue(camposInvalidos(new DireccionDeEnvioRequest("Armenia", "Cra 14 # 10-20", "Ana Gomez")).isEmpty());
    }

    @Test
    void direccionDeEnvioVaciaMarcaLosTresCampos() {
        assertEquals(Set.of("ciudad", "direccion", "destinatario"),
                camposInvalidos(new DireccionDeEnvioRequest("", null, " ")));
    }

    @Test
    void direccionDeEnvioDemasiadoLargaSeRechaza() {
        assertEquals(Set.of("direccion"),
                camposInvalidos(new DireccionDeEnvioRequest("Armenia", "x".repeat(151), "Ana Gomez")));
    }
}
