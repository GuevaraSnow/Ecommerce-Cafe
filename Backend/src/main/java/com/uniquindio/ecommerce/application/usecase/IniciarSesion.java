package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.service.ServicioDeAutenticacion;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: IniciarSesion.
 * (Actor: cualquier usuario) — devuelve al usuario si las credenciales son correctas. No tiene lógica: la
 * regla la aplica el Domain Service {@link ServicioDeAutenticacion}.
 */
@Service
@RequiredArgsConstructor
public class IniciarSesion {

    private final ServicioDeAutenticacion servicioDeAutenticacion;

    @Transactional(readOnly = true)
    public Usuario ejecutar(Email email, Contrasena contrasena) {
        return servicioDeAutenticacion.autenticar(email, contrasena);
    }
}
