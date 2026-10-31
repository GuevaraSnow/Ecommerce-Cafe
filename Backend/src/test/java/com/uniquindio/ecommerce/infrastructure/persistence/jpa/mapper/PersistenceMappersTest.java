package com.uniquindio.ecommerce.infrastructure.persistence.jpa.mapper;

import com.uniquindio.ecommerce.Domain.entity.Finca;
import com.uniquindio.ecommerce.Domain.entity.Transformacion;
import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.*;

import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.FincaEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.TransformacionEntity;
import com.uniquindio.ecommerce.infrastructure.persistence.jpa.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceMappersTest {

    private final UsuarioPersistenceMapper usuarioMapper = new UsuarioPersistenceMapper();
    private final FincaPersistenceMapper fincaMapper = new FincaPersistenceMapper();
    private final TransformacionPersistenceMapper transformacionMapper = new TransformacionPersistenceMapper();

    @Test
    void usuarioMapperConservaTodosLosCampos() {
        Usuario usuario = Usuario.registrar(
                "usr-1",
                "Juan Perez",
                new Email("juan@test.com"),
                new Contrasena("pass123"),
                new Telefono("3001234567"),
                new FechaDeNacimiento(LocalDate.of(1995, 5, 10)),
                Rol.CAFICULTOR
        );

        UsuarioEntity entity = usuarioMapper.toEntity(usuario);
        Usuario mapeado = usuarioMapper.toDomain(entity);

        assertEquals(usuario.getId(), mapeado.getId());
        assertEquals(usuario.getNombre(), mapeado.getNombre());
        assertEquals(usuario.getEmail(), mapeado.getEmail());
        assertEquals(usuario.getContrasena(), mapeado.getContrasena());
        assertEquals(usuario.getTelefono(), mapeado.getTelefono());
        assertEquals(usuario.getFechaDeNacimiento(), mapeado.getFechaDeNacimiento());
        assertEquals(usuario.getRol(), mapeado.getRol());
        assertEquals(usuario.isEliminado(), mapeado.isEliminado());
    }

    @Test
    void fincaMapperConservaTodosLosCampos() {
        Finca finca = Finca.registrar(
                "finca-1",
                "usr-1",
                "Finca El Edén",
                new Ubicacion("Quindío", "Montenegro"),
                new Altitud(1500)
        );

        FincaEntity entity = fincaMapper.toEntity(finca);
        Finca mapeada = fincaMapper.toDomain(entity);

        assertEquals(finca.getId(), mapeada.getId());
        assertEquals(finca.getPropietarioId(), mapeada.getPropietarioId());
        assertEquals(finca.getNombre(), mapeada.getNombre());
        assertEquals(finca.getUbicacion(), mapeada.getUbicacion());
        assertEquals(finca.getAltitud(), mapeada.getAltitud());
        assertEquals(finca.isEliminada(), mapeada.isEliminada());
    }

    @Test
    void transformacionMapperConservaTodosLosCampos() {
        Transformacion trans = Transformacion.registrar(
                "trans-1",
                "LOT-001",
                new Cantidad(100.0, "kg"),
                new PorcentajeDeMerma(0.15),
                "tostador-1",
                LocalDate.now(),
                PerfilTueste.MEDIO
        );

        TransformacionEntity entity = transformacionMapper.toEntity(trans);
        Transformacion mapeada = transformacionMapper.toDomain(entity);

        assertEquals(trans.getId(), mapeada.getId());
        assertEquals(trans.getLoteCodigo(), mapeada.getLoteCodigo());
        assertEquals(trans.getCantidadEntrada(), mapeada.getCantidadEntrada());
        assertEquals(trans.getMerma(), mapeada.getMerma());
        assertEquals(trans.getCantidadResultante(), mapeada.getCantidadResultante());
        assertEquals(trans.getTostadorId(), mapeada.getTostadorId());
        assertEquals(trans.getFecha(), mapeada.getFecha());
        assertEquals(trans.getPerfilTuesteResultante(), mapeada.getPerfilTuesteResultante());
    }
}