package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Altitud;
import com.uniquindio.ecommerce.Domain.valueobject.Ubicacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** La consulta solo delega en el repositorio, lanza no encontrado si no existe y nunca guarda. */
@ExtendWith(MockitoExtension.class)
class ConsultarFincaTest {

    @Mock
    private FincaRepositorio fincaRepositorio;

    @InjectMocks
    private ConsultarFinca consultarFinca;

    @Test
    void devuelveLaFinca() {
        Finca finca = Finca.registrar("finca-1", "caficultor-1", "Finca El Recuerdo",
                new Ubicacion("Quindío", "Armenia"), new Altitud(1550));
        when(fincaRepositorio.buscarPorId("finca-1")).thenReturn(Optional.of(finca));

        assertSame(finca, consultarFinca.ejecutar("finca-1"));
        verify(fincaRepositorio, never()).guardar(any());
    }

    @Test
    void unaFincaInexistenteLanzaNoEncontrado() {
        when(fincaRepositorio.buscarPorId("no-existe")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> consultarFinca.ejecutar("no-existe"));
    }
}
