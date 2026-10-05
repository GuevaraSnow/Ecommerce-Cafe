# Casos de Uso — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Cada caso de uso solo **coordina**: carga con el Repository, invoca comportamiento del dominio y
guarda. Las reglas de negocio viven en las entidades y value objects. El Repository se crea una
sola vez por entidad y lo reutilizan todos los casos que lo necesitan.

## Producción y trazabilidad (café físico)

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `RegistrarLote` | Caficultor | Crea un Lote de una Cosecha con Variedad y Proceso | `LoteRepositorio` |
| `RegistrarTransformacion` | Tostador | Descuenta la cantidad de entrada de los Lotes, aplica la merma y calcula la cantidad resultante | `TransformacionRepositorio`, `LoteRepositorio` |
| `ConsultarFichaOrigen` | Comprador / Sistema | Arma la Ficha de Origen a partir del id de una Transformación y sus Lotes (la Finca y la Cosecha aún no se leen de datos reales) | `TransformacionRepositorio`, `LoteRepositorio` |

## Comercialización

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `PublicarPresentacion` | Caficultor / Tostador / Vendedor de Derivados | Publica una Presentación Trazable; asigna cantidad desde la Transformación | `PresentacionRepositorio`, `TransformacionRepositorio` |
| `PublicarArticuloMerchandising` | Vendedor de Derivados | Publica un Artículo de Merchandising (sin Origen) | `PresentacionRepositorio` |
| `ActualizarPresentacion` | Vendedor | Cambia precio, cantidad, estado, galería o nota de cata | `PresentacionRepositorio` |
| `EliminarPresentacion` | Vendedor | Baja lógica; se bloquea con compras activas | `PresentacionRepositorio`, `CompraRepositorio` |
| `RealizarCompra` | Comprador | Crea una Compra física: valida disponibilidad y stock, congela el precio y descuenta la cantidad (todo o nada) | `PresentacionRepositorio`, `CompraRepositorio` |
| `ConfirmarCompra` | Sistema / Comprador | Confirma una Compra pendiente (evita doble confirmación) | `CompraRepositorio` |
| `CancelarCompra` | Comprador | Cancela una Compra física antes del envío y repone el stock (todo o nada) | `CompraRepositorio`, `PresentacionRepositorio` |
| `CambiarEstadoCompra` | Vendedor / Sistema | Mueve una Compra física a ENVIADA o ENTREGADA; confirmar y cancelar tienen su propio caso de uso | `CompraRepositorio` |

## Cursos virtuales

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `PublicarCurso` | Formador | Publica un Curso con al menos una Lección | `CursoRepositorio` |
| `AgregarLeccion` | Formador | Inserta una Lección en una posición y renumera las siguientes | `CursoRepositorio` |
| `ComprarCurso` | Comprador | Compra un Curso; bloquea si ya tiene Inscripción activa | `CursoRepositorio`, `InscripcionRepositorio`, `CompraRepositorio` |
| `AccederLeccion` | Comprador | Accede a una Lección con Inscripción vigente | `InscripcionRepositorio`, `CursoRepositorio` |
| `SolicitarReembolsoCurso` | Comprador | Pide reembolso (48 h, sin haber accedido) | `InscripcionRepositorio`, `SolicitudReembolsoRepositorio` |
| `RevocarInscripcion` | Sistema | Aprueba la Solicitud de Reembolso y revoca la Inscripción asociada | `SolicitudReembolsoRepositorio`, `InscripcionRepositorio` |

## Usuarios

| Caso de uso | Actor | Descripción | Repository(s) |
| --- | --- | --- | --- |
| `RegistrarVendedor` | Visitante | Crea la cuenta de Vendedor con email único | `VendedorRepositorio` |
| `RegistrarComprador` | Visitante | Crea la cuenta de Comprador con email único | `CompradorRepositorio` |
| `IniciarSesion` | Comprador | Autentica con Email y Contraseña (solo Compradores por ahora; recibe `VendedorRepositorio` pero aún no lo usa) | `CompradorRepositorio` |
| `RestablecerContrasena` | Comprador | Genera un código de recuperación y restablece la contraseña con él (válido 15 min) | `CompradorRepositorio` |

## Repositories (uno por entidad principal)

| Repository | Métodos (lenguaje del negocio) | Implementación en memoria |
| --- | --- | --- |
| `LoteRepositorio` | `buscarPorCodigo`, `guardar` | `LoteRepositorioMemoria` |
| `PresentacionRepositorio` | `buscarPorId`, `guardar` | `PresentacionRepositorioMemoria` |
| `TransformacionRepositorio` | `buscarPorId`, `guardar` | `TransformacionRepositorioMemoria` |
| `VendedorRepositorio` | `existePorEmail`, `guardar` | `VendedorRepositorioMemoria` |
| `CompraRepositorio` | `buscarPorId`, `guardar`, `existeCompraActivaConPresentacion` | `CompraRepositorioMemoria` |
| `CompradorRepositorio` | `existePorEmail`, `buscarPorEmail`, `guardar` | `CompradorRepositorioMemoria` |
| `CursoRepositorio` | `buscarPorId`, `guardar` | `CursoRepositorioMemoria` |
| `InscripcionRepositorio` | `buscarPorId`, `buscarActivaPorCompradorYCurso`, `guardar` | `InscripcionRepositorioMemoria` |
| `SolicitudReembolsoRepositorio` | `buscarPorId`, `guardar` | `SolicitudReembolsoRepositorioMemoria` |

## Notas

- `RegistrarFinca` existe en el código pero no usa ningún Repository: crea la Finca y no la guarda.
- Diseñados en el modelado pero aún no implementados: `DejarResena`, `ResponderResena`,
  `SolicitarReembolso` (café físico), `GestionarReembolso`, `EnviarNotificacion`,
  `MarcarNotificacionLeida`.
- Los casos de uso buscan solo coordinar; hoy `RestablecerContrasena` (valida el código) y
  `ActualizarPresentacion` (decide entre reponer y descontar) todavía contienen decisiones que
  deberían vivir en el dominio.
