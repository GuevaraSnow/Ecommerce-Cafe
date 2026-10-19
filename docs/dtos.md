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
| `codigo` | Identidad del Lote (formato `LOT-AAAA-NNN`) |
| `cantidad` (valor, unidad) | Cantidad inicial disponible; mayor a cero |
| `fincaId` | Referencia a la Finca de origen (otro agregado, por id) |
| `procesoDeBeneficio` | Lavado / Honey / Natural; define el perfil de sabor |
| `estado` (`EstadoDelCafe`) | Verde o Pergamino; el dominio rechaza Tostado |
| `cosecha` (año, temporada) | Parte de lo que identifica el origen del café |
| `variedad` | Castillo, Caturra, Geisha…; dato de trazabilidad |

### Operaciones sobre una Presentación publicada: un Request por operación

No hay un Request genérico de "actualizar": cada operación del dominio tiene su caso de uso y su
endpoint con nombre propio. El id de la Presentación viaja en la ruta, no en el cuerpo.

| Operación (caso de uso) | Cuerpo | Por qué |
| --- | --- | --- |
| `CambiarPrecioDePresentacion` → `cambiarPrecio()` | `PrecioRequest` (monto, moneda) | Reutiliza el mismo DTO de la publicación |
| `ReponerStockDePresentacion` → `ServicioDeReposicion` | `CantidadRequest` (valor, unidad) | Es lo que se **suma**, no el total: así el endpoint no es ambiguo y el café sale del Origen por esa cantidad |
| `RetirarStockDePresentacion` → `descontarCantidad()` | `CantidadRequest` | Lo que se **resta** del stock (merma, daño) |
| `PausarPresentacion` → `desactivar()` / `ReactivarPresentacion` → `activar()` | sin cuerpo | La operación es el endpoint (`/pausar`, `/reactivar`) |
| `ReemplazarGaleriaDePresentacion` → `reemplazarGaleria()` | `ReemplazarGaleriaRequest` (lista de `ImagenRequest`) | Valida la forma; 1 a 10 imágenes con una principal lo valida el dominio |
| `RegistrarNotaDeCata` → `registrarNotaCata()` | `RegistrarNotaDeCataRequest` (descriptores) | Solo aplica a una Presentación Trazable |

## Response

### `PresentacionResponse` (interfaz sellada) — respuesta de `PublicarPresentacion`, `PublicarArticuloMerchandising` y de las operaciones sobre una Presentación publicada

Refleja la herencia del dominio: cada variante trae **solo** los campos que le corresponden.

| Campo común | Por qué se devuelve |
| --- | --- |
| `id`, `vendedorId` | Identificar la Presentación y a quién pertenece |
| `titulo`, `tipo` | Mostrarla y saber qué variante es |
| `precio`, `cantidadDisponible` | Datos comerciales vigentes |
| `galeria` | Imágenes para el catálogo |
| `estado`, `eliminada` | Distinguir `ACTIVA/AGOTADA/VENCIDA/INACTIVA` de la baja definitiva |

- `PresentacionTrazableResponse` agrega `origen`, `perfilTueste`, `fechaTueste`, `notaCata`
  (los dos últimos solo si es café tostado / ya se registró).
- `ArticuloDeMerchandisingResponse` agrega `material` y `descripcion`, y **no** lleva origen ni datos
  de tueste, ni siquiera como `null`.

### `LoteResponse` — respuesta de `RegistrarLote`

| Campo | Por qué se devuelve |
| --- | --- |
| `codigo`, `fincaId` | Identidad del Lote y su Finca |
| `cantidadDisponible` | Saldo actual (cambia con cada Transformación) |
| `estado`, `procesoDeBeneficio`, `variedad`, `cosecha` | Datos de origen para la Ficha de Origen |
| `eliminadoLogicamente` | Indica si el Lote ya no puede usarse |
