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
| `PublicarPresentacion` | Caficultor / Tostador / Vendedor de Derivados | Publica una Presentación Trazable; `ServicioDePublicacion` verifica que su Origen exista y le descuenta la cantidad publicada (al Lote si es café sin tostar, a la Transformación si es tostado o derivado) | `PresentacionRepositorio`, `LoteRepositorio`, `TransformacionRepositorio` |
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
| `ConsultarPresentacion` | Cualquier visitante | Detalle de una Presentación con su galería, precio vigente y descuento (consulta, no modifica) | `PresentacionRepositorio` |
| `BuscarPresentaciones` | Cualquier visitante | Lista por páginas las Presentaciones disponibles para la venta, con filtros por título, tipo, vendedor y rango de precio (consulta) | `PresentacionRepositorio` |
| `SugerirPresentaciones` | Cualquier visitante | Búsqueda predictiva por nombre: sugiere Presentaciones disponibles cuyo título contiene el texto (consulta) | `PresentacionRepositorio` |
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
- Los casos de uso solo coordinan. Cuando una regla cruza agregados la resuelve un Domain Service
  (`Domain/service`): `ServicioDePublicacion`, `ServicioDeReposicion` y `ServicioDeBajaDePresentacion`.
  Hoy `RestablecerContrasena` (valida el código) todavía contiene una decisión que debería vivir
  en el dominio.
- Un caso de uso por operación: no hay un "actualizar" genérico con campos opcionales.
