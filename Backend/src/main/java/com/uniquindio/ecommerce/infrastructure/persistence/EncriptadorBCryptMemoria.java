package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class EncriptadorBCryptMemoria implements EncriptadorContrasenaRepositorio {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Override
    public String encriptar(String contrasenaPlana) {
        if (contrasenaPlana == null || contrasenaPlana.isBlank()) {
            return contrasenaPlana;
        }
        return encoder.encode(contrasenaPlana);
    }

    @Override
    public boolean coinciden(String contrasenaPlana, String contrasenaEncriptada) {
        if (contrasenaPlana == null || contrasenaEncriptada == null) {
            return false;
        }
        return encoder.matches(contrasenaPlana, contrasenaEncriptada);
    }
}