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
| `CrearCarrito` | Comprador | Abre un carrito vacío (Compra pendiente, sin Dirección de Envío) | `CompraRepositorio` |
| `AgregarAlCarrito` | Comprador | Agrega una Presentación al carrito: `ServicioDeCompra` verifica que esté a la venta y que el stock alcance para el total del carrito, y congela el precio vigente; no descuenta stock | `CompraRepositorio`, `PresentacionRepositorio` |
| `ConfirmarCompra` | Comprador | Confirma el carrito con la Dirección de Envío; `ServicioDeCompra` vuelve a verificar el stock de todos los ítems y lo descuenta (todo o nada) | `CompraRepositorio`, `PresentacionRepositorio` |
| `CancelarCompra` | Comprador | Cancela una Compra antes del envío; `ServicioDeCompra` repone el stock si ya estaba confirmada | `CompraRepositorio`, `PresentacionRepositorio` |
| `MarcarEnviada` | Vendedor | Marca como enviada una Compra confirmada | `CompraRepositorio` |
| `MarcarEntregada` | Vendedor | Marca como entregada una Compra enviada y registra la fecha y hora de entrega | `CompraRepositorio` |
| `DejarResena` | Comprador | Deja una Reseña (Calificación de 1 a 5 y texto) de una Presentación que adquirió; `VerificadorDeCompra` exige la Compra entregada y que no la haya reseñado antes | `PresentacionRepositorio`, `ResenaRepositorio`, `CompraRepositorio` |
| `ResponderResena` | Vendedor | Responde una Reseña de una de sus Presentaciones (solo el dueño, una sola vez) | `ResenaRepositorio` |

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
| `CompraRepositorio` | `buscarPorId`, `guardar`, `existeCompraActivaConPresentacion`, `existeCompraEntregada` | `CompraRepositorioMemoria` |
| `CompradorRepositorio` | `existePorEmail`, `buscarPorEmail`, `guardar` | `CompradorRepositorioMemoria` |
| `CursoRepositorio` | `buscarPorId`, `guardar` | `CursoRepositorioMemoria` |
| `InscripcionRepositorio` | `buscarPorId`, `buscarActivaPorCompradorYCurso`, `guardar` | `InscripcionRepositorioMemoria` |
| `SolicitudReembolsoRepositorio` | `buscarPorId`, `guardar` | `SolicitudReembolsoRepositorioMemoria` |
| `ResenaRepositorio` | `buscarPorId`, `guardar`, `existeResenaDe` | `ResenaRepositorioMemoria` |

## Notas

- `RegistrarFinca` existe en el código pero no usa ningún Repository: crea la Finca y no la guarda.
- Diseñados en el modelado pero aún no implementados:
  `SolicitarReembolso` (café físico), `GestionarReembolso`, `EnviarNotificacion`,
  `MarcarNotificacionLeida`.
- Los casos de uso solo coordinan. Cuando una regla cruza agregados la resuelve un Domain Service
  (`Domain/service`): `ServicioDePublicacion`, `ServicioDeReposicion`, `ServicioDeBajaDePresentacion`, `ServicioDeCompra` y `VerificadorDeCompra`.
  Hoy `RestablecerContrasena` (valida el código) todavía contiene una decisión que debería vivir
  en el dominio.
- Un caso de uso por operación: no hay un "actualizar" genérico con campos opcionales.
