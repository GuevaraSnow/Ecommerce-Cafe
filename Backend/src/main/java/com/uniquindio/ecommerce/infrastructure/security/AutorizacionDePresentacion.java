package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Regla de propiedad para los {@code @PreAuthorize} de PresentacionController: una Presentación solo
 * la modifica o la da de baja el Vendedor que la publicó. Se invoca desde la expresión
 * {@code @autorizacionDePresentacion.esDuenoDe(#id, authentication)}.
 */
@Component("autorizacionDePresentacion")
@RequiredArgsConstructor
public class AutorizacionDePresentacion {

    private final PresentacionRepositorio repositorio;

    /**
     * Si la Presentación no existe se deja pasar: no hay dueño del que proteger nada y es el caso de uso
     * quien responde 404; negar aquí revelaría con un 403 qué ids existen y cuáles no.
     */
    public boolean esDuenoDe(String presentacionId, Authentication usuario) {
        return repositorio.buscarPorId(presentacionId)
                .map(presentacion -> presentacion.getVendedorId().equals(usuario.getName()))
                .orElse(true);
    }
}
