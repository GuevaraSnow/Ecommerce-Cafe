package com.uniquindio.ecommerce.infrastructure.rest;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import com.uniquindio.ecommerce.application.dto.request.CambiarPrecioRequest;
import com.uniquindio.ecommerce.application.dto.request.ProgramarDescuentoRequest;
import com.uniquindio.ecommerce.application.dto.request.PublicarArticuloMerchandisingRequest;
import com.uniquindio.ecommerce.application.dto.request.PublicarPresentacionRequest;
import com.uniquindio.ecommerce.application.dto.request.RegistrarNotaDeCataRequest;
import com.uniquindio.ecommerce.application.dto.request.ReemplazarGaleriaRequest;
import com.uniquindio.ecommerce.application.dto.request.ReponerStockRequest;
import com.uniquindio.ecommerce.application.dto.request.RetirarStockRequest;
import com.uniquindio.ecommerce.application.dto.response.PresentacionResponse;
import com.uniquindio.ecommerce.application.usecase.CambiarPrecioDePresentacion;
import com.uniquindio.ecommerce.application.usecase.EliminarPresentacion;
import com.uniquindio.ecommerce.application.usecase.PausarPresentacion;
import com.uniquindio.ecommerce.application.usecase.ProgramarDescuentoDePresentacion;
import com.uniquindio.ecommerce.application.usecase.PublicarArticuloMerchandising;
import com.uniquindio.ecommerce.application.usecase.PublicarPresentacion;
import com.uniquindio.ecommerce.application.usecase.QuitarDescuentoDePresentacion;
import com.uniquindio.ecommerce.application.usecase.ReactivarPresentacion;
import com.uniquindio.ecommerce.application.usecase.RegistrarNotaDeCata;
import com.uniquindio.ecommerce.application.usecase.ReemplazarGaleriaDePresentacion;
import com.uniquindio.ecommerce.application.usecase.ReponerStockDePresentacion;
import com.uniquindio.ecommerce.application.usecase.RetirarStockDePresentacion;
import com.uniquindio.ecommerce.infrastructure.rest.mapper.PresentacionMapper;
import com.uniquindio.ecommerce.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

/**
 * Operaciones que modifican una Presentación: un endpoint por operación del dominio. El controlador
 * solo recibe, valida la forma, delega en el caso de uso, mapea y responde; no captura excepciones
 * (las traduce {@code GlobalExceptionHandler}).
 *
 * <p>El Vendedor y su Rol salen del token JWT de la petición, nunca del cuerpo. Los roles y la propiedad se
 * declaran con {@code @PreAuthorize}: para publicar hay que ser Caficultor, Tostador o Vendedor de
 * Derivados; para cambiar o dar de baja una Presentación hay que ser quien la publicó.
 */
@RestController
@RequestMapping("/api/presentaciones")
@RequiredArgsConstructor
@Tag(name = "Presentaciones", description = "Publicación y gestión de Presentaciones del Vendedor")
@SecurityRequirement(name = "bearerAuth")
public class PresentacionController {

    private static final String PUBLICAR = "hasAnyAuthority('CAFICULTOR', 'TOSTADOR', 'VENDEDOR_DERIVADOS')";
    private static final String ES_DUENO = "@autorizacionDePresentacion.esDuenoDe(#id, authentication)";

    private final PublicarPresentacion publicarPresentacion;
    private final PublicarArticuloMerchandising publicarArticulo;
    private final CambiarPrecioDePresentacion cambiarPrecio;
    private final ReponerStockDePresentacion reponerStock;
    private final RetirarStockDePresentacion retirarStock;
    private final PausarPresentacion pausar;
    private final ReactivarPresentacion reactivar;
    private final ReemplazarGaleriaDePresentacion reemplazarGaleria;
    private final RegistrarNotaDeCata registrarNotaDeCata;
    private final ProgramarDescuentoDePresentacion programarDescuento;
    private final QuitarDescuentoDePresentacion quitarDescuento;
    private final EliminarPresentacion eliminar;
    private final PresentacionMapper mapper;

    @PostMapping
    @PreAuthorize(PUBLICAR)
    @Operation(summary = "Publicar una Presentación trazable",
            description = "Café verde, tostado o derivado consumible, siempre con Origen (Lote o Transformación).")
    @ApiResponse(responseCode = "201", description = "Presentación publicada; el Location apunta a su detalle")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Regla de negocio violada")
    public ResponseEntity<PresentacionResponse> publicar(
            @AuthenticationPrincipal Jwt token,
            @Valid @RequestBody PublicarPresentacionRequest request) {
        Presentacion presentacion = publicarPresentacion.ejecutar(UUID.randomUUID().toString(), token.getSubject(),
                request.loteId(), request.transformacionId(), request.titulo(), request.tipo(),
                mapper.toPrecio(request.precio()), mapper.toCantidad(request.cantidad()),
                mapper.toGaleria(request.galeria()), request.perfilTueste(),
                mapper.toFechaDeTueste(request.fechaTueste()));
        return creada(presentacion);
    }

    @PostMapping("/merchandising")
    @PreAuthorize(PUBLICAR)
    @Operation(summary = "Publicar un Artículo de Merchandising",
            description = "Solo el Vendedor de Derivados; sin Origen ni datos de tueste.")
    @ApiResponse(responseCode = "201", description = "Artículo publicado; el Location apunta a su detalle")
    @ApiResponse(responseCode = "400", description = "Datos mal formados")
    @ApiResponse(responseCode = "422", description = "Regla de negocio violada")
    public ResponseEntity<PresentacionResponse> publicarMerchandising(
            @AuthenticationPrincipal Jwt token,
            @Valid @RequestBody PublicarArticuloMerchandisingRequest request) {
        Presentacion articulo = publicarArticulo.ejecutar(UUID.randomUUID().toString(), token.getSubject(),
                request.titulo(), mapper.toPrecio(request.precio()), mapper.toCantidad(request.cantidad()),
                mapper.toGaleria(request.galeria()), request.material(), request.descripcion(), rolDe(token));
        return creada(articulo);
    }

    @PutMapping("/{id}/precio")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Cambiar el precio de una Presentación")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse cambiarPrecio(@PathVariable String id,
                                              @Valid @RequestBody CambiarPrecioRequest request) {
        return mapper.toResponse(cambiarPrecio.ejecutar(id, mapper.toPrecio(request.precio())));
    }

    @PostMapping("/{id}/reposiciones")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Reponer stock",
            description = "Suma cantidad a la Presentación y la descuenta de su Origen.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reponerStock(@PathVariable String id,
                                             @Valid @RequestBody ReponerStockRequest request) {
        return mapper.toResponse(reponerStock.ejecutar(id, mapper.toCantidad(request.cantidad())));
    }

    @PostMapping("/{id}/retiros")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Retirar stock",
            description = "Resta cantidad de la Presentación; si llega a cero queda Agotada.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse retirarStock(@PathVariable String id,
                                             @Valid @RequestBody RetirarStockRequest request) {
        return mapper.toResponse(retirarStock.ejecutar(id, mapper.toCantidad(request.cantidad())));
    }

    @PostMapping("/{id}/pausa")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Pausar la venta de una Presentación activa")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse pausar(@PathVariable String id) {
        return mapper.toResponse(pausar.ejecutar(id));
    }

    @PostMapping("/{id}/reactivacion")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Reactivar una Presentación pausada")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reactivar(@PathVariable String id) {
        return mapper.toResponse(reactivar.ejecutar(id));
    }

    @PutMapping("/{id}/galeria")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Reemplazar la galería",
            description = "Entre 1 y 10 imágenes, con exactamente una principal.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reemplazarGaleria(@PathVariable String id,
                                                  @Valid @RequestBody ReemplazarGaleriaRequest request) {
        return mapper.toResponse(reemplazarGaleria.ejecutar(id, mapper.toGaleria(request.galeria())));
    }

    @PutMapping("/{id}/nota-de-cata")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Registrar la Nota de Cata", description = "Solo aplica a una Presentación trazable.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse registrarNotaDeCata(@PathVariable String id,
                                                    @Valid @RequestBody RegistrarNotaDeCataRequest request) {
        return mapper.toResponse(registrarNotaDeCata.ejecutar(id, mapper.toNotaDeCata(request.descriptores())));
    }

    @PutMapping("/{id}/descuento")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Programar un Descuento por temporada",
            description = "Entre 1 % y 50 %, entre dos fechas incluidas; reemplaza al anterior si lo había.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse programarDescuento(@PathVariable String id,
                                                   @Valid @RequestBody ProgramarDescuentoRequest request) {
        return mapper.toResponse(programarDescuento.ejecutar(id, mapper.toDescuento(request)));
    }

    @DeleteMapping("/{id}/descuento")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Quitar el Descuento programado")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse quitarDescuento(@PathVariable String id) {
        return mapper.toResponse(quitarDescuento.ejecutar(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(ES_DUENO)
    @Operation(summary = "Dar de baja una Presentación",
            description = "Baja lógica; no se permite si tiene compras activas.")
    @ApiResponse(responseCode = "204", description = "Presentación dada de baja")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        eliminar.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    /** El Rol que dejó el servidor en el token al iniciar sesión. */
    private Rol rolDe(Jwt token) {
        return Rol.valueOf(token.getClaimAsString(JwtService.CLAIM_ROL));
    }

    private ResponseEntity<PresentacionResponse> creada(Presentacion presentacion) {
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/presentaciones/{id}").buildAndExpand(presentacion.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(presentacion));
    }
}
