package com.uniquindio.ecommerce.Domain.service;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.ContrasenaPlana;

public class ServicioDeContrasena {

    private final EncriptadorContrasenaRepositorio encriptador;

    public ServicioDeContrasena(EncriptadorContrasenaRepositorio encriptador) {
        this.encriptador = encriptador;
    }

    public void cambiarContrasena(Usuario usuario, String contrasenaActualTexto, ContrasenaPlana nuevaContrasenaPlana) {
        if (usuario == null) {
            throw new ReglaDominioException("El usuario es obligatorio.");
        }
        if (contrasenaActualTexto == null || contrasenaActualTexto.isBlank() || nuevaContrasenaPlana == null) {
            throw new ReglaDominioException("La contraseña actual y la nueva contraseña son obligatorias.");
        }

        if (!encriptador.coinciden(contrasenaActualTexto, usuario.getContrasena().contrasena())) {
            throw new ReglaDominioException("La contraseña actual ingresada no es correcta.");
        }

        String nuevaEncriptada = encriptador.encriptar(nuevaContrasenaPlana.valor());
        usuario.cambiarContrasenaDirecta(new Contrasena(nuevaEncriptada));
    }

    /**
     * Restablece la contraseña con el código de recuperación que se le envió al usuario. La contraseña nueva
     * se encripta antes de asignarla, igual que al registrarse o al cambiarla, para que después pueda iniciar
     * sesión con ella. Si el código no es válido o ya expiró, el usuario no cambia.
     */
    public void restablecerContrasena(Usuario usuario, String codigoTexto, ContrasenaPlana nuevaContrasenaPlana) {
        if (usuario == null) {
            throw new ReglaDominioException("El usuario es obligatorio.");
        }
        if (nuevaContrasenaPlana == null) {
            throw new ReglaDominioException("La nueva contraseña es obligatoria.");
        }
        String nuevaEncriptada = encriptador.encriptar(nuevaContrasenaPlana.valor());
        usuario.cambiarContrasena(codigoTexto, new Contrasena(nuevaEncriptada));
    }
}