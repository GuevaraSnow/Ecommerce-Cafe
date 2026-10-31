package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.entity.Lote;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Dueño de un Lote = propietario de su Finca. Si el código es inválido o el Lote no existe se deja
 * pasar (responden el mapper y el caso de uso); si el Lote existe pero su Finca no, nadie es dueño.
 */
@ExtendWith(MockitoExtension.class)
class AutorizacionDeLoteTest {

    private static final String CODIGO = "LOT-" + Year.now().getValue() + "-001";

    @Mock
    private LoteRepositorio loteRepositorio;
    @Mock
    private FincaRepositorio fincaRepositorio;

    @InjectMocks
    private AutorizacionDeLote autorizacion;

    private Authentication usuario(String id) {
        Authentication usuario = mock(Authentication.class);
        lenient().when(usuario.getName()).thenReturn(id);
        return usuario;
    }

    private Lote loteDeLaFinca(String fincaId) {
        return Lote.registrar(new CodigoDeLote(CODIGO), new Cantidad(100, "kg"), fincaId,
                ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO,
                new Cosecha(Year.now().getValue(), TemporadaDeCosecha.PRINCIPAL),
                new VariedadDeCafe("Caturra"));
    }

    private Finca fincaDe(String propietarioId) {
        return Finca.reconstruir("finca-1", propietarioId, "Villa Laura",
                new Ubicacion("Quindío", "Calarcá"), new Altitud(1650), false);
    }

    @Test
    void elPropietarioDeLaFincaDelLoteEsElDueno() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote(CODIGO))).thenReturn(Optional.of(loteDeLaFinca("finca-1")));
        when(fincaRepositorio.buscarPorId("finca-1")).thenReturn(Optional.of(fincaDe("caficultor-1")));

        assertTrue(autorizacion.esDuenoDe(CODIGO, usuario("caficultor-1")));
    }

    @Test
    void otroCaficultorNoEsElDueno() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote(CODIGO))).thenReturn(Optional.of(loteDeLaFinca("finca-1")));
        when(fincaRepositorio.buscarPorId("finca-1")).thenReturn(Optional.of(fincaDe("caficultor-1")));

        assertFalse(autorizacion.esDuenoDe(CODIGO, usuario("caficultor-2")));
    }

    @Test
    void siLaFincaDelLoteNoExisteNadieEsDueno() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote(CODIGO))).thenReturn(Optional.of(loteDeLaFinca("finca-1")));
        when(fincaRepositorio.buscarPorId("finca-1")).thenReturn(Optional.empty());

        assertFalse(autorizacion.esDuenoDe(CODIGO, usuario("caficultor-1")));
    }

    @Test
    void siElLoteNoExisteSeDejaPasarParaQueResponda404() {
        when(loteRepositorio.buscarPorCodigo(new CodigoDeLote(CODIGO))).thenReturn(Optional.empty());

        assertTrue(autorizacion.esDuenoDe(CODIGO, mock(Authentication.class)));
    }

    @Test
    void siElCodigoNoTieneElFormatoValidoSeDejaPasarParaQueResponda400() {
        assertTrue(autorizacion.esDuenoDe("ABC", mock(Authentication.class)));
    }
}
