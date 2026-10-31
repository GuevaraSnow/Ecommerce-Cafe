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
- El Vendedor y su Rol salen **del token JWT**, nunca del cuerpo ni de encabezados propios: el sujeto del
  token es el id del usuario y el claim `rol` su Rol.

## Seguridad

Las consultas (`GET`) son públicas. Todo lo demás exige `Authorization: Bearer <token>`:

| Operación | Quién puede | Si no cumple |
| --- | --- | --- |
| Publicar (`POST /` y `POST /merchandising`) | Caficultor, Tostador o Vendedor de Derivados (que pueda publicar ese tipo lo decide el dominio) | sin token, 401; con otro rol, 403 |
| Cambiar precio, stock, pausa, galería, nota, descuento o dar de baja | El Vendedor que **publicó** esa Presentación | sin token, 401; otro usuario, 403 |

- Si la Presentación no existe se responde 404 aunque el usuario no sea su dueño, para no revelar qué ids existen.
- El token se firma con HS256 y la clave `app.jwt.secret` (variable de entorno `JWT_SECRET`), y dura
  `app.jwt.duracion-minutos`. Un token vencido, mal firmado o que no es un JWT responde 401.
- Los roles y la propiedad se declaran con `@PreAuthorize` en el controlador; las reglas generales viven en
  `SecurityConfig`. El token lo emite `JwtService.generarToken(usuarioId, rol)`, que debe llamar el inicio de
  sesión.

## Consultas (`PresentacionConsultaController`)

| Método y ruta | Caso de uso | Parámetros | Respuesta |
| --- | --- | --- | --- |
| `GET /{id}` | `ConsultarPresentacion` | — | 200 `PresentacionResponse` |
| `GET /` | `BuscarPresentaciones` | `titulo`, `tipo`, `vendedorId`, `precioMinimo`, `precioMaximo`, `calificacionMinima` (promedio de reseñas, de 1 a 5; sin reseñas no aparece), `pagina` (0 por defecto, mínimo 0), `tamano` (10 por defecto, entre 1 y 50) | 200 `PaginaResponse` |
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

## Persistencia

Las Presentaciones se guardan en H2 con JPA (`PresentacionRepositorioJpa`). Las dos variantes comparten
la tabla `presentaciones` (herencia `SINGLE_TABLE`, columna `clase`: `TRAZABLE` o `MERCHANDISING`); la
galería y la nota de cata van en `presentacion_galeria` y `presentacion_nota_cata`, con su posición para
conservar el orden. Los datos de ejemplo están en `data-presentacion.sql`.

- El catálogo y las sugerencias solo devuelven lo **disponible para la venta**: activo, sin baja y, si es
  café tostado, con menos de 30 días desde el tueste (el estado `VENCIDA` se calcula, por eso la consulta
  también revisa la fecha y no solo la columna `estado`).
- La versión en memoria (`PresentacionRepositorioMemoria`) ya no es un bean; se conserva para las pruebas.
