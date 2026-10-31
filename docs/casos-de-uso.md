# Casos de Uso — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Cada caso de uso solo **coordina**: carga con el Repository (y responde no encontrado si no existe),
invoca comportamiento del dominio y guarda. Las reglas de negocio viven en las entidades y value
objects; cuando una regla cruza agregados la resuelve un Domain Service. El Repository se crea una sola
vez por entidad y lo reutilizan todos los casos que lo necesitan.

## Producción y trazabilidad (café físico)

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `RegistrarFinca` | Caficultor | Registra una Finca a nombre del Caficultor que la crea (el dueño sale del token) | `FincaRepositorio` |
| `ConsultarFinca` | Usuario autenticado | Devuelve una Finca por su id (consulta) | `FincaRepositorio` |
| `RegistrarLote` | Caficultor | Registra un Lote de una Cosecha con Variedad y Proceso; `ServicioDeLote` le genera el código `LOT-AAAA-NNN` (siguiente número del año de registro) y exige que la Cosecha sea del año de registro o del anterior | `LoteRepositorio` |
| `ConsultarLote` | Usuario autenticado | Devuelve un Lote por su código; un Lote dado de baja no se muestra (consulta) | `LoteRepositorio` |
| `EliminarLote` | Caficultor | Da de baja lógica un Lote (solo el dueño de su Finca); el Lote se conserva para la trazabilidad | `LoteRepositorio` |
| `RegistrarTransformacion` | Tostador | Carga el Lote; `ServicioDeTransformacion` aplica la merma, calcula la cantidad resultante y descuenta del Lote la cantidad de entrada; guarda ambos | `LoteRepositorio`, `TransformacionRepositorio` |
| `ConsultarFichaOrigen` | Usuario autenticado | Arma la Ficha de Origen de una Transformación con su Lote y su Finca reales (consulta) | `TransformacionRepositorio`, `LoteRepositorio`, `FincaRepositorio` |

## Comercialización (Presentaciones)

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `PublicarPresentacion` | Caficultor / Tostador / Vendedor de Derivados | Publica una Presentación Trazable; `ServicioDePublicacion` toma el Rol real del Usuario, verifica que su Origen exista y le descuenta la cantidad publicada (al Lote si es café sin tostar, a la Transformación si es tostado o derivado) | `PresentacionRepositorio`, `UsuarioRepositorio`, `LoteRepositorio`, `TransformacionRepositorio` |
| `PublicarArticuloMerchandising` | Vendedor de Derivados | Publica un Artículo de Merchandising (sin Origen) | `PresentacionRepositorio` |
| `CambiarPrecioDePresentacion` | Vendedor | Cambia el precio de una Presentación publicada | `PresentacionRepositorio` |
| `ReponerStockDePresentacion` | Vendedor | Repone stock; si es Trazable, el café sale de su Origen (`ServicioDeReposicion`) | `PresentacionRepositorio`, `LoteRepositorio`, `TransformacionRepositorio` |
| `RetirarStockDePresentacion` | Vendedor | Retira stock (merma, daño); no regresa café al Origen | `PresentacionRepositorio` |
| `PausarPresentacion` | Vendedor | Pausa la venta de una Presentación activa | `PresentacionRepositorio` |
| `ReactivarPresentacion` | Vendedor | Reactiva una Presentación pausada que tenga stock | `PresentacionRepositorio` |
| `ReemplazarGaleriaDePresentacion` | Vendedor | Reemplaza la Galería de imágenes | `PresentacionRepositorio` |
| `RegistrarNotaDeCata` | Vendedor | Registra los descriptores de sabor de una Presentación Trazable | `PresentacionRepositorio` |
| `ProgramarDescuentoDePresentacion` | Vendedor | Programa un Descuento por temporada (1–50 %, entre dos fechas); reemplaza al anterior | `PresentacionRepositorio` |
| `QuitarDescuentoDePresentacion` | Vendedor | Quita el Descuento programado | `PresentacionRepositorio` |
| `EliminarPresentacion` | Vendedor | Baja lógica; `ServicioDeBajaDePresentacion` la bloquea con compras activas | `PresentacionRepositorio`, `CompraRepositorio` |
| `ConsultarPresentacion` | Cualquier visitante | Detalle de una Presentación con su galería, precio vigente y descuento; una dada de baja no se muestra (consulta) | `PresentacionRepositorio` |
| `BuscarPresentaciones` | Cualquier visitante | Lista por páginas las Presentaciones disponibles para la venta, con filtros por título, tipo, vendedor, rango de precio y calificación mínima (promedio de reseñas, calculado en la consulta) | `PresentacionRepositorio` |
| `SugerirPresentaciones` | Cualquier visitante | Búsqueda predictiva por nombre: sugiere Presentaciones disponibles cuyo título contiene el texto (consulta) | `PresentacionRepositorio` |
| `ConsultarMasVendidas` | Cualquier visitante | Ranking de las Presentaciones más vendidas por número de Compras e ingresos; la consulta une Compras y Presentaciones y agrupa en la base (consulta) | `PresentacionRepositorio` |

## Compras, reembolsos y reseñas

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `CrearCarrito` | Comprador | Abre un carrito vacío (Compra pendiente, sin Dirección de Envío) | `CompraRepositorio` |
| `AgregarAlCarrito` | Comprador | Agrega una Presentación al carrito: `ServicioDeCompra` verifica que esté a la venta y que el stock alcance para el total del carrito, y congela el precio vigente; no descuenta stock | `CompraRepositorio`, `PresentacionRepositorio` |
| `ConfirmarCompra` | Comprador | Confirma el carrito con la Dirección de Envío; `ServicioDeCompra` vuelve a verificar el stock de todos los ítems y lo descuenta (todo o nada); avisa por correo al Comprador | `CompraRepositorio`, `PresentacionRepositorio`, `UsuarioRepositorio` |
| `CancelarCompra` | Comprador | Cancela una Compra antes del envío; `ServicioDeCompra` repone el stock si ya estaba confirmada | `CompraRepositorio`, `PresentacionRepositorio` |
| `MarcarEnviada` | Vendedor | Marca como enviada una Compra confirmada | `CompraRepositorio` |
| `MarcarEntregada` | Vendedor | Marca como entregada una Compra enviada y registra la fecha y hora de entrega | `CompraRepositorio` |
| `ConsultarCompra` | Comprador dueño o vendedor de la Compra | Detalle de una Compra (consulta) | `CompraRepositorio` |
| `ConsultarHistorialDeCompras` | Comprador | Lista sus Compras, de la más reciente a la más antigua (consulta) | `CompraRepositorio` |
| `SolicitarReembolso` | Comprador | Pide el reembolso de una Presentación de una Compra entregada; `ServicioDeReembolso` exige que sea quien compró, dentro de las 48 h, que la Presentación esté en la Compra y que no haya otra solicitud vigente de ella | `CompraRepositorio`, `PresentacionRepositorio`, `SolicitudReembolsoRepositorio` |
| `AprobarReembolso` | Vendedor | Aprueba el reembolso de una de sus Presentaciones; `ServicioDeReembolso` pasa la Compra a REEMBOLSADA solo cuando todas sus Presentaciones quedan aprobadas | `SolicitudReembolsoRepositorio`, `CompraRepositorio` |
| `RechazarReembolso` | Vendedor | Rechaza el reembolso de una de sus Presentaciones (solo el dueño, una sola vez); la Compra no cambia | `SolicitudReembolsoRepositorio` |
| `DejarResena` | Comprador | Deja una Reseña (Calificación de 1 a 5 y texto) de una Presentación que adquirió; `VerificadorDeCompra` exige la Compra entregada y que no la haya reseñado antes | `PresentacionRepositorio`, `ResenaRepositorio`, `CompraRepositorio` |
| `ResponderResena` | Vendedor | Responde una Reseña de una de sus Presentaciones (solo el dueño, una sola vez) | `ResenaRepositorio` |
| `ConsultarResenasDePresentacion` | Cualquier visitante | Lista las Reseñas de una Presentación, de la más reciente a la más antigua (consulta) | `ResenaRepositorio` |

## Usuarios

Un solo tipo de cuenta, `Usuario`, con un `Rol` (Comprador, Caficultor, Tostador o Vendedor de Derivados).

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `RegistrarUsuario` | Visitante | Crea la cuenta con email único (`VerificadorDeEmail`), contraseña segura que se guarda encriptada y mayoría de edad | `UsuarioRepositorio` |
| `IniciarSesion` | Cualquier usuario | Autentica con Email y Contraseña; `ServicioDeAutenticacion` responde igual si el correo no existe, la contraseña no coincide o el usuario está dado de baja | `UsuarioRepositorio` |
| `RecuperarContrasena` | Cualquier usuario | Genera un código de recuperación (válido 15 min) y lo envía por correo | `UsuarioRepositorio` |
| `RestablecerContrasena` | Cualquier usuario | Restablece la contraseña con el código; `ServicioDeContrasena` la encripta antes de guardarla | `UsuarioRepositorio` |
| `CambiarContrasena` | Usuario autenticado | Cambia la contraseña verificando la actual (`ServicioDeContrasena`) | `UsuarioRepositorio` |
| `EditarPerfil` | Usuario autenticado | Cambia el nombre y el teléfono propios | `UsuarioRepositorio` |

## Repositories (uno por entidad principal)

Cada puerto vive en `Domain/repository`; su adaptador activo es `XRepositorioJpa` (H2 con Spring Data). La
versión `XRepositorioMemoria` ya no es un bean: se conserva para las pruebas unitarias.

| Repository | Métodos (lenguaje del negocio) |
| --- | --- |
| `UsuarioRepositorio` | `guardar`, `buscarPorId`, `buscarPorEmail`, `existePorEmail` |
| `FincaRepositorio` | `guardar`, `buscarPorId` |
| `LoteRepositorio` | `buscarPorCodigo`, `buscarNoEliminadoPorCodigo`, `guardar`, `obtenerNumeroMasAlto` |
| `TransformacionRepositorio` | `buscarPorId`, `guardar` |
| `PresentacionRepositorio` | `buscarPorId`, `buscarNoEliminadaPorId`, `guardar`, `buscarDisponibles`, `sugerirDisponiblesPorTitulo`, `masVendidas` |
| `CompraRepositorio` | `buscarPorId`, `guardar`, `buscarPorComprador`, `existeCompraActivaConPresentacion`, `existeCompraEntregada` |
| `SolicitudReembolsoRepositorio` | `buscarPorId`, `guardar`, `existeSolicitudVigente`, `existeSolicitudAprobada` |
| `ResenaRepositorio` | `buscarPorId`, `guardar`, `buscarPorPresentacion`, `existeResenaDe` |

Otros dos puertos, que no guardan entidades: `EncriptadorContrasenaRepositorio` (`encriptar`, `coinciden`;
BCrypt) y `NotificadorDeCorreo` (`enviarCodigoDeRecuperacion`, `enviarConfirmacionDeCompra`).

## Domain Services

Los casos de uso solo coordinan. Cuando una regla cruza agregados la resuelve un Domain Service
(`Domain/service`, sin anotaciones de Spring), que se registra como bean en `infrastructure/config`
(`ConfigPresentacion`, `ConfigCompra`, `ConfigTransformacion` y `DomainServiceConfig`):

- Presentación: `ServicioDePublicacion`, `ServicioDeReposicion` y `ServicioDeBajaDePresentacion`
  (con la pieza interna `DescuentoDeOrigen`).
- Compra, reembolso y reseña: `ServicioDeCompra`, `ServicioDeReembolso` y `VerificadorDeCompra`.
- Producción: `ServicioDeLote` (código del Lote y ventana de la Cosecha) y `ServicioDeTransformacion`
  (merma y descuento del Lote).
- Usuario: `ServicioDeAutenticacion`, `ServicioDeContrasena` y `VerificadorDeEmail`.

## Notas

- Un caso de uso por operación: no hay un "actualizar" genérico con campos opcionales.
- "No encontrado" se resuelve con `orElseThrow()` sin argumento y la API lo traduce a 404; una regla de
  negocio violada (`ReglaDominioException`) se traduce a 422.
- Los controladores toman al usuario autenticado (su id y su Rol) del token JWT y protegen cada
  operación con `@PreAuthorize`; el caso de uso no conoce el token.
- Diseñados en el modelado pero fuera del alcance de esta entrega: las notificaciones dentro de la
  plataforma y los cursos virtuales.
