package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.ContrasenaPlana;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Traduce entre los DTO de Usuario y el dominio. No aplica reglas de negocio: los constructores de los Value
 * Objects son quienes validan y lanzan ReglaDominioException, que aquí nunca se captura.
 */
@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail().email(),
                usuario.getTelefono() != null ? usuario.getTelefono().telefono() : null,
                usuario.getRol().name()
        );
    }

    public Email toEmail(String email) {
        return new Email(email);
    }

    /** La contraseña tal como llega al iniciar sesión: sin reglas de seguridad, solo se compara. */
    public Contrasena toContrasena(String contrasena) {
        return new Contrasena(contrasena);
    }

    /** La contraseña nueva en texto plano: el Value Object valida que sea segura. */
    public ContrasenaPlana toContrasenaPlana(String contrasena) {
        return new ContrasenaPlana(contrasena);
    }

    /** Un teléfono ausente o en blanco es "sin teléfono": devuelve {@code null}. */
    public Telefono toTelefono(String telefono) {
        return telefono != null && !telefono.isBlank() ? new Telefono(telefono) : null;
    }

    public FechaDeNacimiento toFechaDeNacimiento(LocalDate fecha) {
        return new FechaDeNacimiento(fecha);
    }
}
