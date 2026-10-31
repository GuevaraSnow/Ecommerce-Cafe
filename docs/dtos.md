# DTOs (Request / Response) — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Los DTOs viven en `application/dto/` y **solo validan la forma** de la entrada (campos presentes,
no vacíos). Las reglas de negocio (origen exclusivo, frescura, perfil por rol, cantidad positiva…)
las aplica siempre el dominio. Ningún DTO expone entidades ni setters.

## Request

### `PublicarPresentacionRequest` → `PublicarPresentacion.ejecutar(...)`

| Campo | Por qué es necesario aquí |
| --- | --- |
| `titulo` | Nombre comercial de la Presentación; obligatorio para publicar |
| `tipo` (`TipoDePresentacion`) | Decide la subclase (café verde/tostado/derivado → `PresentacionTrazable`) |
| `precio` (`PrecioRequest`) | Monto y moneda; el dominio exige que sea mayor a cero |
| `cantidad` (`CantidadRequest`) | Stock inicial y su unidad; mayor a cero |
| `galeria` (lista de `ImagenRequest`) | 1 a 10 imágenes, una principal (regla de `Galeria`) |
| `loteId` / `transformacionId` | Origen: exactamente uno de los dos (regla B, la valida `OrigenDePresentacion`) |
| `perfilTueste`, `fechaTueste` | Solo para café tostado; sirven a las reglas A (perfil por rol) y D (frescura) |

**Campos que NO incluye, a propósito:** el `id` (lo genera el servidor) y el vendedor con su rol
(salen del usuario autenticado, para que el cliente no pueda publicar a nombre de otro ni
saltarse la regla A declarando un rol falso).

### `RegistrarLoteRequest` → `RegistrarLote.ejecutar(...)`

| Campo | Por qué es necesario aquí |
| --- | --- |
| `cantidad` (`CantidadRequest`: valor, unidad) | Cantidad inicial disponible; mayor a cero |
| `fincaId` | Referencia a la Finca de origen (otro agregado, por id) |
| `procesoDeBeneficio` | Lavado / Honey / Natural; define el perfil de sabor |
| `estado` (`EstadoDelCafe`) | Verde o Pergamino; el dominio rechaza Tostado |
| `cosecha` (año, temporada) | Parte de lo que identifica el origen del café; solo se acepta la del año de registro o la anterior |
| `variedad` | Castillo, Caturra, Geisha…; dato de trazabilidad |

**Campos que NO incluye, a propósito:** el `codigo` del Lote (`LOT-AAAA-NNN`): lo genera el servidor con el
siguiente número del año de registro, para que el cliente no pueda repetir ni saltarse un código.

### Operaciones sobre una Presentación publicada: un Request por operación

No hay un Request genérico de "actualizar": cada operación del dominio tiene su caso de uso y su
endpoint con nombre propio. El id de la Presentación viaja en la ruta, no en el cuerpo.

| Operación (caso de uso) | Cuerpo | Por qué |
| --- | --- | --- |
| `CambiarPrecioDePresentacion` → `cambiarPrecio()` | `CambiarPrecioRequest` (`PrecioRequest`: monto, moneda) | Un request por operación; reutiliza el `PrecioRequest` de la publicación |
| `ReponerStockDePresentacion` → `ServicioDeReposicion` | `ReponerStockRequest` (`CantidadRequest`: valor, unidad) | Es lo que se **suma**, no el total: así el endpoint no es ambiguo y el café sale del Origen por esa cantidad |
| `RetirarStockDePresentacion` → `descontarCantidad()` | `RetirarStockRequest` (`CantidadRequest`) | Lo que se **resta** del stock (merma, daño) |
| `PausarPresentacion` → `desactivar()` / `ReactivarPresentacion` → `activar()` | sin cuerpo | La operación es el endpoint (`/pausa`, `/reactivacion`) |
| `ReemplazarGaleriaDePresentacion` → `reemplazarGaleria()` | `ReemplazarGaleriaRequest` (lista de `ImagenRequest`) | Valida la forma; 1 a 10 imágenes con una principal lo valida el dominio |
| `RegistrarNotaDeCata` → `registrarNotaCata()` | `RegistrarNotaDeCataRequest` (descriptores) | Solo aplica a una Presentación Trazable |
| `ProgramarDescuentoDePresentacion` → `programarDescuento()` | `ProgramarDescuentoRequest` (porcentaje, fechaInicio, fechaFin) | Valida la forma; porcentaje 1–50 y fechas no invertidas lo valida el dominio (`Descuento`) |
| `QuitarDescuentoDePresentacion` → `quitarDescuento()` | sin cuerpo | La operación es el endpoint (`DELETE /descuento`) |
| `EliminarPresentacion` → `ServicioDeBajaDePresentacion` | sin cuerpo | La operación es el endpoint (`DELETE /{id}`) |

Validación de forma en los requests: `@NotBlank` en textos, `@NotNull` en campos obligatorios y
`@Positive` en el monto del precio y el valor de la cantidad. Todo lo demás lo decide el dominio.

## Response

### `PresentacionResponse` (interfaz sellada) — respuesta de `PublicarPresentacion`, `PublicarArticuloMerchandising` y de las operaciones sobre una Presentación publicada

Refleja la herencia del dominio: cada variante trae **solo** los campos que le corresponden.

| Campo común | Por qué se devuelve |
| --- | --- |
| `id`, `vendedorId` | Identificar la Presentación y a quién pertenece |
| `titulo`, `tipo` | Mostrarla y saber qué variante es |
| `precio`, `cantidadDisponible` | Datos comerciales vigentes |
| `precioVigente`, `descuento` | Precio al que se vende **hoy** (con el descuento si rige; si no, igual a `precio`) y el descuento programado, vigente o no (`null` si no hay) |
| `galeria` | Imágenes para el catálogo |
| `estado`, `eliminada` | Distinguir `ACTIVA/AGOTADA/VENCIDA/INACTIVA` de la baja definitiva |

- `PresentacionTrazableResponse` agrega `origen`, `perfilTueste`, `fechaTueste`, `notaCata`
  (los dos últimos solo si es café tostado / ya se registró).
- `ArticuloDeMerchandisingResponse` agrega `material` y `descripcion`, y **no** lleva origen ni datos
  de tueste, ni siquiera como `null`.

### Respuestas de las consultas

| Respuesta | Qué trae |
| --- | --- |
| `PaginaResponse<T>` | `contenido`, `pagina` (la primera es la 0), `tamano`, `totalElementos`, `totalPaginas`; la usa la búsqueda con filtros |
| `SugerenciaResponse` | Solo `id` y `titulo`: lo justo para mostrar la opción de la búsqueda predictiva y navegar a su detalle |
| `DescuentoResponse` | `porcentaje`, `fechaInicio`, `fechaFin` |

### `LoteResponse` — respuesta de `RegistrarLote`

| Campo | Por qué se devuelve |
| --- | --- |
| `codigo`, `fincaId` | Identidad del Lote y su Finca |
| `cantidadDisponible` | Saldo actual (cambia con cada Transformación) |
| `estado`, `procesoDeBeneficio`, `variedad`, `cosecha` | Datos de origen para la Ficha de Origen |
| `eliminadoLogicamente` | Indica si el Lote ya no puede usarse |
