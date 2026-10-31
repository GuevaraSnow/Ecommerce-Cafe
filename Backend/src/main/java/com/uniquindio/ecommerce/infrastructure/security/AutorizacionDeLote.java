package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.FincaRepositorio;
import com.uniquindio.ecommerce.Domain.repository.LoteRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeLote;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Regla de propiedad para los {@code @PreAuthorize} de LoteController: un Lote solo lo da de baja el
 * Caficultor dueño de su Finca (el Lote guarda el id de la Finca y la Finca, su propietario). Se invoca
 * desde la expresión {@code @autorizacionDeLote.esDuenoDe(#codigoDeLote, authentication)}.
 */
@Component("autorizacionDeLote")
@RequiredArgsConstructor
public class AutorizacionDeLote {

    private final LoteRepositorio loteRepositorio;
    private final FincaRepositorio fincaRepositorio;

    /**
     * Si el código no tiene el formato válido o el Lote no existe se deja pasar: no hay dueño del que
     * proteger nada y son el mapper (400) y el caso de uso (404) quienes responden; negar aquí revelaría
     * con un 403 qué códigos existen y cuáles no. Si el Lote existe pero su Finca no, nadie es dueño.
     */
    public boolean esDuenoDe(String codigoDeLote, Authentication usuario) {
        try {
            return loteRepositorio.buscarPorCodigo(new CodigoDeLote(codigoDeLote))
                    .map(lote -> fincaRepositorio.buscarPorId(lote.getFincaID())
                            .map(finca -> finca.getPropietarioId().equals(usuario.getName()))
                            .orElse(false))
                    .orElse(true);
        } catch (ReglaDominioException formatoInvalido) {
            return true;
        }
    }
}
