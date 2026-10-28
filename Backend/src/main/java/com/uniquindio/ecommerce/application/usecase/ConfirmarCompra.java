package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Compra;
import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.repository.CompraRepositorio;
import com.uniquindio.ecommerce.Domain.repository.NotificadorDeCorreo;
import com.uniquindio.ecommerce.Domain.repository.UsuarioRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDeCompra;
import com.uniquindio.ecommerce.Domain.valueobject.DireccionDeEnvio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: ConfirmarCompra.
 * (Actor: Comprador) — confirma su carrito con la Dirección de Envío y le llega el correo de
 * confirmación. No tiene lógica: {@link ServicioDeCompra} vuelve a verificar el stock de todos
 * los ítems (por si alguien compró antes) y lo descuenta todo o nada. El Comprador se carga
 * antes de confirmar, así que si no existe nada cambia.
 */
@Service
@RequiredArgsConstructor
public class ConfirmarCompra {

    private final CompraRepositorio compraRepositorio;
    private final ServicioDeCompra servicioDeCompra;
    private final UsuarioRepositorio usuarioRepositorio;
    private final NotificadorDeCorreo notificadorDeCorreo;

    @Transactional
    public Compra ejecutar(String compraId, DireccionDeEnvio direccionEnvio) {
        Compra compra = compraRepositorio.buscarPorId(compraId).orElseThrow();
        Usuario comprador = usuarioRepositorio.buscarPorId(compra.getCompradorId()).orElseThrow();
        servicioDeCompra.confirmar(compra, direccionEnvio);
        compraRepositorio.guardar(compra);
        notificadorDeCorreo.enviarConfirmacionDeCompra(comprador.getEmail(), compra);
        return compra;
    }
}
