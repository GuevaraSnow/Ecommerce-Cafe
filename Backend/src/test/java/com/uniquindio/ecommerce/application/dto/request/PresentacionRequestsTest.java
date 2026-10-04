package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDePresentacion;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que las anotaciones de los Requests de Presentación se disparan; las reglas de negocio las valida el dominio. */
class PresentacionRequestsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private List<ImagenRequest> galeriaValida() {
        return List.of(new ImagenRequest("http://img/1.png", true));
    }

    @Test
    void publicarPresentacionCompletoNoTieneViolacionesYLosCamposOpcionalesPuedenFaltar() {
        PublicarPresentacionRequest conOpcionalesVacios = new PublicarPresentacionRequest("Café Verde",
                TipoDePresentacion.CAFE_VERDE, new PrecioRequest(28000.0, "COP"), new CantidadRequest(200.0, "kg"),
                galeriaValida(), "lote-1", null, null, null);
        PublicarPresentacionRequest conTodo = new PublicarPresentacionRequest("Café Tostado",
                TipoDePresentacion.CAFE_TOSTADO, new PrecioRequest(32000.0, "COP"), new CantidadRequest(20.0, "kg"),
                galeriaValida(), null, "transformacion-1", PerfilTueste.MEDIO, LocalDate.now());

        assertTrue(camposInvalidos(conOpcionalesVacios).isEmpty());
        assertTrue(camposInvalidos(conTodo).isEmpty());
    }

    @Test
    void publicarPresentacionReportaLosCamposObligatoriosVacios() {
        PublicarPresentacionRequest vacio = new PublicarPresentacionRequest(" ", null, null, null, null,
                null, null, null, null);

        assertEquals(Set.of("titulo", "tipo", "precio", "cantidad", "galeria"), camposInvalidos(vacio));
    }

    @Test
    void publicarPresentacionValidaLosCamposAnidados() {
        PublicarPresentacionRequest anidadosInvalidos = new PublicarPresentacionRequest("Café Verde",
                TipoDePresentacion.CAFE_VERDE, new PrecioRequest(null, " "), new CantidadRequest(null, " "),
                List.of(new ImagenRequest(" ", true)), "lote-1", null, null, null);

        assertEquals(Set.of("precio.monto", "precio.moneda", "cantidad.valor", "cantidad.unidad", "galeria[0].url"),
                camposInvalidos(anidadosInvalidos));
    }

    @Test
    void publicarArticuloDeMerchandisingCompletoNoTieneViolaciones() {
        PublicarArticuloMerchandisingRequest valido = new PublicarArticuloMerchandisingRequest("Manilla",
                "Manilla artesanal", "semilla de café", new PrecioRequest(15000.0, "COP"),
                new CantidadRequest(20.0, "unidad"), galeriaValida());

        assertTrue(camposInvalidos(valido).isEmpty());
    }

    @Test
    void publicarArticuloDeMerchandisingReportaLosCamposObligatoriosVacios() {
        PublicarArticuloMerchandisingRequest vacio =
                new PublicarArticuloMerchandisingRequest(" ", " ", " ", null, null, null);

        assertEquals(Set.of("titulo", "descripcion", "material", "precio", "cantidad", "galeria"),
                camposInvalidos(vacio));
    }

    @Test
    void actualizarPresentacionSinNingunCampoEsValidoPorqueTodosSonOpcionales() {
        assertTrue(camposInvalidos(new ActualizarPresentacionRequest(null, null, null, null, null)).isEmpty());
    }

    @Test
    void actualizarPresentacionValidaLosCamposAnidadosQueVengan() {
        ActualizarPresentacionRequest anidadosInvalidos = new ActualizarPresentacionRequest(
                new PrecioRequest(null, " "), new CantidadRequest(null, " "), true,
                List.of(new ImagenRequest(" ", true)), List.of("Chocolate"));

        assertEquals(Set.of("nuevoPrecio.monto", "nuevoPrecio.moneda", "nuevaCantidadTotal.valor",
                "nuevaCantidadTotal.unidad", "nuevaGaleria[0].url"), camposInvalidos(anidadosInvalidos));
    }
}
