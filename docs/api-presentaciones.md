# API de Presentaciones — Café Trazado

Base: `/api/presentaciones`. Swagger UI en `/swagger-ui.html`; el contrato en `/api-docs`.
Cada endpoint corresponde a **una operación del dominio** (no hay un PATCH genérico) y a un caso de uso.
Los controladores solo reciben, validan la forma, delegan, mapean y responden; las reglas de negocio
las aplica el dominio y las excepciones las traduce `GlobalExceptionHandler`.

## Comandos (`PresentacionController`)

| Método y ruta | Caso de uso | Cuerpo | Respuesta |
| --- | --- | --- | --- |
| `POST /` | `PublicarPresentacion` | `PublicarPresentacionRequest` | 201 + `Location` |
| `POST /merchandising` | `PublicarArticuloMerchandising` | `PublicarArticuloMerchandisingRequest` | 201 + `Location` |
| `PUT /{id}/precio` | `CambiarPrecioDePresentacion` | `CambiarPrecioRequest` | 200 |
| `POST /{id}/reposiciones` | `ReponerStockDePresentacion` | `ReponerStockRequest` | 200 |
| `POST /{id}/retiros` | `RetirarStockDePresentacion` | `RetirarStockRequest` | 200 |
| `POST /{id}/pausa` | `PausarPresentacion` | — | 200 |
| `POST /{id}/reactivacion` | `ReactivarPresentacion` | — | 200 |
| `PUT /{id}/galeria` | `ReemplazarGaleriaDePresentacion` | `ReemplazarGaleriaRequest` | 200 |
| `PUT /{id}/nota-de-cata` | `RegistrarNotaDeCata` | `RegistrarNotaDeCataRequest` | 200 |
| `PUT /{id}/descuento` | `ProgramarDescuentoDePresentacion` | `ProgramarDescuentoRequest` | 200 |
| `DELETE /{id}/descuento` | `QuitarDescuentoDePresentacion` | — | 200 |
| `DELETE /{id}` | `EliminarPresentacion` | — | 204 |

- El **id** de una Presentación nuevo lo genera el servidor (UUID) en el controlador; el cliente nunca lo envía.
- El Vendedor y su Rol llegan por ahora en los encabezados `X-Vendedor-Id` y `X-Rol-Vendedor`;
  cuando exista autenticación saldrán del usuario autenticado.

## Consultas (`PresentacionConsultaController`)

| Método y ruta | Caso de uso | Parámetros | Respuesta |
| --- | --- | --- | --- |
| `GET /{id}` | `ConsultarPresentacion` | — | 200 `PresentacionResponse` |
| `GET /` | `BuscarPresentaciones` | `titulo`, `tipo`, `vendedorId`, `precioMinimo`, `precioMaximo`, `pagina` (0 por defecto, mínimo 0), `tamano` (10 por defecto, entre 1 y 50) | 200 `PaginaResponse` |
| `GET /sugerencias` | `SugerirPresentaciones` | `texto` (obligatorio), `limite` (5 por defecto, entre 1 y 10) | 200 lista de `SugerenciaResponse` |

- Las listas solo incluyen Presentaciones **disponibles para la venta** (activas y no dadas de baja),
  ordenadas por título. El rango de precio se aplica al precio normal.
- El detalle devuelve la galería, el precio vigente y el descuento. Las reseñas se agregan con su propia épica.

## Errores

Todo error trae `{ timestamp, status, mensaje }`.

| Situación | Estado |
| --- | --- |
| Regla de negocio violada (`ReglaDominioException`) | 400 con el mensaje de la regla |
| Cuerpo con campos inválidos o que no es JSON | 400 con el campo y su mensaje |
| Parámetro o encabezado faltante, de tipo inválido o fuera de rango | 400 |
| La Presentación no existe (`NoSuchElementException`) | 404 |
