package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import com.uniquindio.ecommerce.application.dto.request.*;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.UsuarioMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Autenticación, perfil y gestión de usuarios")
public class UsuarioController {

    private static final String USUARIO_HEADER = "X-Usuario-Id";

    private final RegistrarUsuario registrarUsuario;
    private final IniciarSesion iniciarSesion;
    private final RecuperarContrasena recuperarContrasena;
    private final RestablecerContrasena restablecerContrasena;
    private final EditarPerfil editarPerfil;
    private final CambiarContrasena cambiarContrasena;
    private final UsuarioMapper mapper;

    @PostMapping("/registro")
    @Operation(summary = "Registrar un usuario")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos inválidos o email ya registrado")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        Telefono telefono = request.telefono() != null && !request.telefono().isBlank()
                ? new Telefono(request.telefono()) : null;

        Usuario usuario = registrarUsuario.ejecutar(
                request.nombre(),
                new Email(request.email()),
                new ContrasenaPlana(request.contrasena()),
                telefono,
                new FechaDeNacimiento(request.fechaNacimiento()),
                request.rol()
        );

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(mapper.toResponse(usuario));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    @ApiResponse(responseCode = "200", description = "Login exitoso")
    @ApiResponse(responseCode = "400", description = "Credenciales inválidas")
    public ResponseEntity<UsuarioResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = iniciarSesion.ejecutar(
                new Email(request.email()),
                new Contrasena(request.contrasena())
        );

        return ResponseEntity.ok(mapper.toResponse(usuario));
    }

    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Solicitar código de recuperación de contraseña")
    @ApiResponse(responseCode = "200", description = "Código de recuperación generado")
    @ApiResponse(responseCode = "404", description = "Email no registrado")
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody RecuperarContrasenaRequest request) {
        recuperarContrasena.ejecutar(new Email(request.email()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Restablecer contraseña usando código")
    @ApiResponse(responseCode = "200", description = "Contraseña restablecida correctamente")
    @ApiResponse(responseCode = "400", description = "Código inválido o expirado")
    public ResponseEntity<Void> restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest request) {
        restablecerContrasena.ejecutar(
                new Email(request.email()),
                request.codigo(),
                new Contrasena(request.nuevaContrasena())
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/perfil")
    @Operation(summary = "Editar perfil del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "400", description = "Encabezado X-Usuario-Id faltante o datos inválidos")
    public ResponseEntity<UsuarioResponse> editarPerfil(
            @RequestHeader(USUARIO_HEADER) String usuarioId,
            @Valid @RequestBody EditarPerfilRequest request) {
        Usuario usuario = editarPerfil.ejecutar(usuarioId, request);
        return ResponseEntity.ok(mapper.toResponse(usuario));
    }

    @PutMapping("/perfil/contrasena")
    @Operation(summary = "Cambiar contraseña del usuario autenticado")
    @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente")
    @ApiResponse(responseCode = "400", description = "Contraseña actual incorrecta o nueva inválida")
    public ResponseEntity<Void> cambiarContrasena(
            @RequestHeader(USUARIO_HEADER) String usuarioId,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        cambiarContrasena.ejecutar(
                usuarioId,
                request.contrasenaActual(),
                new ContrasenaPlana(request.nuevaContrasena())
        );
        return ResponseEntity.ok().build();
    }
}