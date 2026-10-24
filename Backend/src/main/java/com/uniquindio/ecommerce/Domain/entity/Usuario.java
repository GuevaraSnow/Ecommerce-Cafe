package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.EncriptadorContrasenaRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.CodigoDeRecuperacion;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;

import java.util.Objects;

public class Usuario {

    private final String id;
    private String nombre;
    private final Email email;
    private Contrasena contrasena;
    private Telefono telefono;
    private final FechaDeNacimiento fechaDeNacimiento;
    private final Rol rol;
    private CodigoDeRecuperacion codigoRecuperacion;
    private Usuario(String id, String nombre, Email email, Contrasena contrasena,
                    Telefono telefono, FechaDeNacimiento fechaDeNacimiento,
                    Rol rol, CodigoDeRecuperacion codigoRecuperacion) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.fechaDeNacimiento = fechaDeNacimiento;
        this.rol = rol;
        this.codigoRecuperacion = codigoRecuperacion;
    }

    public static Usuario registrar(String id, String nombre, Email email, Contrasena contrasena,
                                    Telefono telefono, FechaDeNacimiento fechaDeNacimiento, Rol rol) {
        validarId(id);
        validarNombre(nombre);
        if (email == null) {
            throw new ReglaDominioException("El email no puede ser nulo.");
        }
        if (contrasena == null) {
            throw new ReglaDominioException("La contraseña no puede ser nula.");
        }
        if (rol == null) {
            throw new ReglaDominioException("El rol no puede ser nulo.");
        }

        return new Usuario(id, nombre.trim(), email, contrasena, telefono, fechaDeNacimiento, rol, null);
    }

    public static Usuario reconstruir(String id, String nombre, Email email, Contrasena contrasena,
                                      Telefono telefono, FechaDeNacimiento fechaDeNacimiento,
                                      Rol rol, CodigoDeRecuperacion codigoRecuperacion) {
        return new Usuario(id, nombre, email, contrasena, telefono, fechaDeNacimiento, rol, codigoRecuperacion);
    }

    private static void validarId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ReglaDominioException("El id no puede ser nulo ni estar vacío.");
        }
    }

    private static void validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaDominioException("El nombre no puede ser nulo ni estar vacío.");
        }
    }

    public void asignarCodigoRecuperacion(CodigoDeRecuperacion codigo) {
        if (codigo == null) {
            throw new ReglaDominioException("El código de recuperación no puede ser nulo.");
        }
        this.codigoRecuperacion = codigo;
    }

    public void cambiarContrasena(CodigoDeRecuperacion codigoEnviado, Contrasena nuevaContrasena) {
        if (this.codigoRecuperacion == null || !this.codigoRecuperacion.estaVigente()) {
            throw new ReglaDominioException("El código de recuperación ha expirado o no ha sido solicitado.");
        }
        if (!this.codigoRecuperacion.equals(codigoEnviado)) {
            throw new ReglaDominioException("El código de recuperación proporcionado no es válido.");
        }
        if (nuevaContrasena == null) {
            throw new ReglaDominioException("La nueva contraseña no puede ser nula.");
        }
        this.contrasena = nuevaContrasena;
        this.codigoRecuperacion = null;
    }


    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Email getEmail() { return email; }
    public Contrasena getContrasena() { return contrasena; }
    public Telefono getTelefono() { return telefono; }
    public FechaDeNacimiento getFechaDeNacimiento() { return fechaDeNacimiento; }
    public Rol getRol() { return rol; }
    public CodigoDeRecuperacion getCodigoRecuperacion() { return codigoRecuperacion; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public void editarPerfil(String nuevoNombre, Telefono nuevoTelefono) {
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
            throw new ReglaDominioException("El nombre no puede ser nulo ni estar vacío.");
        }
        this.nombre = nuevoNombre.trim();
        this.telefono = nuevoTelefono;
    }


    public void cambiarContrasenaConVerificacion(Contrasena contrasenaActualFormulario,
                                                 Contrasena nuevaContrasena,
                                                 EncriptadorContrasenaRepositorio encriptador) {
        if (contrasenaActualFormulario == null || nuevaContrasena == null) {
            throw new ReglaDominioException("La contraseña actual y la nueva contraseña son obligatorias.");
        }
        if (encriptador == null) {
            throw new ReglaDominioException("El encriptador no puede ser nulo.");
        }

        if (!encriptador.coinciden(contrasenaActualFormulario.contrasena(), this.contrasena.contrasena())) {
            throw new ReglaDominioException("La contraseña actual ingresada no es correcta.");
        }

        String nuevaEncriptada = encriptador.encriptar(nuevaContrasena.contrasena());
        this.contrasena = new Contrasena(nuevaEncriptada);
    }
}