package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Usuario;
import com.uniquindio.ecommerce.application.dto.request.*;
import com.uniquindio.ecommerce.application.dto.response.LoginResponse;
import com.uniquindio.ecommerce.application.dto.response.UsuarioResponse;
import com.uniquindio.ecommerce.application.usecase.*;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.UsuarioMapper;
import com.uniquindio.ecommerce.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Autenticación, perfil y gestión de usuarios")
public class UsuarioController {

    private final RegistrarUsuario registrarUsuario;
    private final IniciarSesion iniciarSesion;
    private final RecuperarContrasena recuperarContrasena;
    private final RestablecerContrasena restablecerContrasena;
    private final EditarPerfil editarPerfil;
    private final CambiarContrasena cambiarContrasena;
    private final UsuarioMapper mapper;
    private final JwtService jwtService;

    @PostMapping("/registro")
    @Operation(summary = "Registrar un usuario")
    @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Datos que incumplen las reglas del usuario o email ya registrado")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        Usuario usuario = registrarUsuario.ejecutar(
                request.nombre(),
                mapper.toEmail(request.email()),
                mapper.toContrasenaPlana(request.contrasena()),
                mapper.toTelefono(request.telefono()),
                mapper.toFechaDeNacimiento(request.fechaNacimiento()),
                request.rol()
        );

        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();

        return ResponseEntity.created(ubicacion).body(mapper.toResponse(usuario));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    @ApiResponse(responseCode = "200", description = "Login exitoso con token")
    @ApiResponse(responseCode = "401",
            description = "Credenciales inválidas: el correo no existe, la contraseña no coincide o el usuario está dado de baja (mismo mensaje en los tres casos)")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = iniciarSesion.ejecutar(
                mapper.toEmail(request.email()),
                mapper.toContrasena(request.contrasena())
        );

        String token = jwtService.generarToken(usuario.getId(), usuario.getEmail().email(), usuario.getRol());

        return ResponseEntity.ok(new LoginResponse(token, mapper.toResponse(usuario)));
    }

    @PostMapping("/recuperar-contrasena")
    @Operation(summary = "Solicitar código de recuperación de contraseña")
    @ApiResponse(responseCode = "200", description = "Código de recuperación generado")
    @ApiResponse(responseCode = "404", description = "Email no registrado")
    public ResponseEntity<Void> solicitarRecuperacion(@Valid @RequestBody RecuperarContrasenaRequest request) {
        recuperarContrasena.ejecutar(mapper.toEmail(request.email()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Restablecer contraseña usando código")
    @ApiResponse(responseCode = "200", description = "Contraseña restablecida correctamente")
    @ApiResponse(responseCode = "422", description = "Código inválido o expirado, o la contraseña no es segura")
    public ResponseEntity<Void> restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest request) {
        restablecerContrasena.ejecutar(
                mapper.toEmail(request.email()),
                request.codigo(),
                mapper.toContrasenaPlana(request.nuevaContrasena())
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/perfil")
    @Operation(summary = "Editar perfil del usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Perfil actualizado")
    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    public ResponseEntity<UsuarioResponse> editarPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EditarPerfilRequest request) {
        Usuario usuario = editarPerfil.ejecutar(jwt.getSubject(), request.nombre(), mapper.toTelefono(request.telefono()));
        return ResponseEntity.ok(mapper.toResponse(usuario));
    }

    @PutMapping("/perfil/contrasena")
    @Operation(summary = "Cambiar contraseña del usuario autenticado", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Contraseña cambiada exitosamente")
    @ApiResponse(responseCode = "401", description = "No autenticado o token inválido")
    public ResponseEntity<Void> cambiarContrasena(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CambiarContrasenaRequest request) {
        cambiarContrasena.ejecutar(
                jwt.getSubject(),
                request.contrasenaActual(),
                mapper.toContrasenaPlana(request.nuevaContrasena())
        );
        return ResponseEntity.ok().build();
    }
}