package com.uniquindio.ecommerce.infrastructure.security;

import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Reglas de propiedad para los {@code @PreAuthorize} de CompraController:
 * <ul>
 *   <li>el carrito solo lo modifican, confirman o cancelan el Comprador que lo abrió;</li>
 *   <li>una Compra solo la marca enviada o entregada un vendedor que tenga alguna Presentación en ella.</li>
 * </ul>
 * Si la Compra no existe se deja pasar: es el caso de uso quien responde 404; negar aquí revelaría
 * con un 403 qué ids existen y cuáles no.
 */
@Component("autorizacionDeCompra")
@RequiredArgsConstructor
public class AutorizacionDeCompra {

    private final CompraRepositorio compraRepositorio;
    private final PresentacionRepositorio presentacionRepositorio;

    public boolean esDelComprador(String compraId, Authentication usuario) {
        return compraRepositorio.buscarPorId(compraId)
                .map(compra -> compra.getCompradorId().equals(usuario.getName()))
                .orElse(true);
    }

    public boolean esVendedorDe(String compraId, Authentication usuario) {
        return compraRepositorio.buscarPorId(compraId)
                .map(compra -> compra.getDetalles().stream()
                        .anyMatch(detalle -> presentacionRepositorio.buscarPorId(detalle.presentacionId())
                                .map(presentacion -> presentacion.getVendedorId().equals(usuario.getName()))
                                .orElse(false)))
                .orElse(true);
    }
}
