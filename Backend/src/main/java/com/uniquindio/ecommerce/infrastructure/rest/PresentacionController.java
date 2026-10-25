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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
 * <p>Mientras no exista autenticación, el Vendedor y su Rol llegan en los encabezados
 * {@code X-Vendedor-Id} y {@code X-Rol-Vendedor}; con JWT saldrán del usuario autenticado.
 */
@RestController
@RequestMapping("/api/presentaciones")
@RequiredArgsConstructor
@Tag(name = "Presentaciones", description = "Publicación y gestión de Presentaciones del Vendedor")
public class PresentacionController {

    private static final String VENDEDOR = "X-Vendedor-Id";
    private static final String ROL = "X-Rol";

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
    @Operation(summary = "Publicar una Presentación trazable",
            description = "Café verde, tostado o derivado consumible, siempre con Origen (Lote o Transformación).")
    @ApiResponse(responseCode = "201", description = "Presentación publicada; el Location apunta a su detalle")
    @ApiResponse(responseCode = "400", description = "Datos mal formados o regla de negocio violada")
    public ResponseEntity<PresentacionResponse> publicar(
            @RequestHeader(VENDEDOR) String vendedorId,
            @RequestHeader(ROL) Rol rol,
            @Valid @RequestBody PublicarPresentacionRequest request) {
        Presentacion presentacion = publicarPresentacion.ejecutar(UUID.randomUUID().toString(), vendedorId,
                request.loteId(), request.transformacionId(), request.titulo(), request.tipo(),
                mapper.toPrecio(request.precio()), mapper.toCantidad(request.cantidad()),
                mapper.toGaleria(request.galeria()), request.perfilTueste(),
                mapper.toFechaDeTueste(request.fechaTueste()));
        return creada(presentacion);
    }

    @PostMapping("/merchandising")
    @Operation(summary = "Publicar un Artículo de Merchandising",
            description = "Solo el Vendedor de Derivados; sin Origen ni datos de tueste.")
    @ApiResponse(responseCode = "201", description = "Artículo publicado; el Location apunta a su detalle")
    @ApiResponse(responseCode = "400", description = "Datos mal formados o regla de negocio violada")
    public ResponseEntity<PresentacionResponse> publicarMerchandising(
            @RequestHeader(VENDEDOR) String vendedorId,
            @RequestHeader(ROL) Rol rol,
            @Valid @RequestBody PublicarArticuloMerchandisingRequest request) {
        Presentacion articulo = publicarArticulo.ejecutar(UUID.randomUUID().toString(), vendedorId,
                request.titulo(), mapper.toPrecio(request.precio()), mapper.toCantidad(request.cantidad()),
                mapper.toGaleria(request.galeria()), request.material(), request.descripcion(), rol);
        return creada(articulo);
    }

    @PutMapping("/{id}/precio")
    @Operation(summary = "Cambiar el precio de una Presentación")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse cambiarPrecio(@PathVariable String id,
                                              @Valid @RequestBody CambiarPrecioRequest request) {
        return mapper.toResponse(cambiarPrecio.ejecutar(id, mapper.toPrecio(request.precio())));
    }

    @PostMapping("/{id}/reposiciones")
    @Operation(summary = "Reponer stock",
            description = "Suma cantidad a la Presentación y la descuenta de su Origen.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reponerStock(@PathVariable String id,
                                             @Valid @RequestBody ReponerStockRequest request) {
        return mapper.toResponse(reponerStock.ejecutar(id, mapper.toCantidad(request.cantidad())));
    }

    @PostMapping("/{id}/retiros")
    @Operation(summary = "Retirar stock",
            description = "Resta cantidad de la Presentación; si llega a cero queda Agotada.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse retirarStock(@PathVariable String id,
                                             @Valid @RequestBody RetirarStockRequest request) {
        return mapper.toResponse(retirarStock.ejecutar(id, mapper.toCantidad(request.cantidad())));
    }

    @PostMapping("/{id}/pausa")
    @Operation(summary = "Pausar la venta de una Presentación activa")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse pausar(@PathVariable String id) {
        return mapper.toResponse(pausar.ejecutar(id));
    }

    @PostMapping("/{id}/reactivacion")
    @Operation(summary = "Reactivar una Presentación pausada")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reactivar(@PathVariable String id) {
        return mapper.toResponse(reactivar.ejecutar(id));
    }

    @PutMapping("/{id}/galeria")
    @Operation(summary = "Reemplazar la galería",
            description = "Entre 1 y 10 imágenes, con exactamente una principal.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse reemplazarGaleria(@PathVariable String id,
                                                  @Valid @RequestBody ReemplazarGaleriaRequest request) {
        return mapper.toResponse(reemplazarGaleria.ejecutar(id, mapper.toGaleria(request.galeria())));
    }

    @PutMapping("/{id}/nota-de-cata")
    @Operation(summary = "Registrar la Nota de Cata", description = "Solo aplica a una Presentación trazable.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse registrarNotaDeCata(@PathVariable String id,
                                                    @Valid @RequestBody RegistrarNotaDeCataRequest request) {
        return mapper.toResponse(registrarNotaDeCata.ejecutar(id, mapper.toNotaDeCata(request.descriptores())));
    }

    @PutMapping("/{id}/descuento")
    @Operation(summary = "Programar un Descuento por temporada",
            description = "Entre 1 % y 50 %, entre dos fechas incluidas; reemplaza al anterior si lo había.")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse programarDescuento(@PathVariable String id,
                                                   @Valid @RequestBody ProgramarDescuentoRequest request) {
        return mapper.toResponse(programarDescuento.ejecutar(id, mapper.toDescuento(request)));
    }

    @DeleteMapping("/{id}/descuento")
    @Operation(summary = "Quitar el Descuento programado")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public PresentacionResponse quitarDescuento(@PathVariable String id) {
        return mapper.toResponse(quitarDescuento.ejecutar(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Dar de baja una Presentación",
            description = "Baja lógica; no se permite si tiene compras activas.")
    @ApiResponse(responseCode = "204", description = "Presentación dada de baja")
    @ApiResponse(responseCode = "404", description = "No existe la Presentación")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        eliminar.ejecutar(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<PresentacionResponse> creada(Presentacion presentacion) {
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/presentaciones/{id}").buildAndExpand(presentacion.getId()).toUri();
        return ResponseEntity.created(ubicacion).body(mapper.toResponse(presentacion));
    }
}
