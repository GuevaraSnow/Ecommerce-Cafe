package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceMapper {

    public UsuarioEntity toEntity(Usuario domain) {
        if (domain == null) return null;

        String codigo = domain.getCodigoRecuperacion() != null ? domain.getCodigoRecuperacion().codigo() : null;
        var expiracion = domain.getCodigoRecuperacion() != null ? domain.getCodigoRecuperacion().fechaExpiracion() : null;

        return new UsuarioEntity(
                domain.getId(),
                domain.getNombre(),
                domain.getEmail().email(),
                domain.getContrasena().contrasena(),
                domain.getTelefono() != null ? domain.getTelefono().telefono() : null,
                domain.getFechaDeNacimiento().valor(),
                domain.getRol(),
                codigo,
                expiracion,
                domain.isEliminado()
        );
    }

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) return null;

        CodigoDeRecuperacion codigoRecuperacion = null;
        if (entity.getCodigoRecuperacion() != null && entity.getCodigoExpiracion() != null) {
            codigoRecuperacion = new CodigoDeRecuperacion(entity.getCodigoRecuperacion(), entity.getCodigoExpiracion());
        }

        Telefono telefono = entity.getTelefono() != null ? new Telefono(entity.getTelefono()) : null;

        return Usuario.reconstruir(
                entity.getId(),
                entity.getNombre(),
                new Email(entity.getEmail()),
                new Contrasena(entity.getContrasena()),
                telefono,
                new FechaDeNacimiento(entity.getFechaNacimiento()),
                entity.getRol(),
                codigoRecuperacion,
                entity.isEliminado()
        );
    }
}