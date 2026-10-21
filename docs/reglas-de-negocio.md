# Reglas de Negocio Innegociables — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Las reglas se protegen en el dominio (lanzan `ReglaDominioException`); los casos de uso solo
coordinan. La columna "Dónde vive" indica quién la hace cumplir.

## Reglas base

| # | Regla | Dónde vive |
| --- | --- | --- |
| 1 | Una Presentación no puede tener cantidad disponible negativa. | `Cantidad.restar()` / `Presentacion.descontarCantidad()` |
| 2 | Un Comprador no puede dejar una Reseña (calificación y comentario) de una Presentación sin haberla comprado (compra completada). La Nota de Cata no aplica aquí: la escribe el vendedor. | **Pendiente:** requiere el caso de uso de reseñas, que aún no está implementado |
| 3 | Un vendedor no puede eliminar una Presentación con compras activas; solo eliminación lógica (se conserva para trazabilidad e historial). | `Presentacion.darDeBaja()` + `EliminarPresentacion` |
| 4 | Un Vendedor no puede eliminar un Lote ya usado en una Transformación; solo eliminación lógica (borrarlo rompería la trazabilidad de las Presentaciones derivadas). | `Lote.eliminarLogicamente()` |
| 5 | Un Comprador B2C no puede adquirir un Lote completo; solo Presentaciones. El Lote se transa en B2B entre Caficultor, Tostador y Vendedor de Derivados. | Modelo: el Lote no es comprable |

## Reglas propias del nicho

| # | Regla | Dónde vive |
| --- | --- | --- |
| 6 (A) | Un Caficultor solo puede publicar café tostado bajo Perfil de Tueste **Tradicional**; Claro, Medio y Oscuro son exclusivos del Tostador. | `PresentacionTrazable.publicar(...)` según `rolVendedor` |
| 7 (B) | Toda Presentación de café o derivado consumible debe tener Origen (un Lote o una Transformación). El merchandising no alimenticio queda exento **por tipo**: es otra subclase sin Origen. | `PresentacionTrazable` (Origen obligatorio) / `ArticuloDeMerchandising` (sin Origen) |
| 8 (C) | Una Transformación no puede usar más café del que tiene el Lote de origen (ej. no se tuestan 80 kg si quedan 50). | `Lote.descontar()` |
| 9 (D) | Un café tostado no puede publicarse ni venderse pasados 30 días desde su fecha de tueste (frescura). Pasado el plazo queda VENCIDA por sí sola: el estado se calcula. | `PresentacionTrazable.estaFresca()` / `getEstado()` |
| 10 (E) | Toda Transformación de tueste registra una **merma por humedad entre 12 % y 22 %**; la cantidad resultante es `entrada × (1 − merma)`. Al Lote se le descuenta la cantidad de entrada, no la resultante. | `PorcentajeDeMerma` (rango) / `Cantidad.aplicarMerma()` / `Transformacion.registrar()` |
| 11 (F) | El café verde solo puede publicarse desde un **Lote**; el café tostado y los derivados consumibles, solo desde una **Transformación**. | `PresentacionTrazable.publicar(...)` según `TipoDePresentacion` |
| 12 (G) | Solo el Vendedor de Derivados puede publicar Artículos de Merchandising, y el Formador no puede publicar Presentaciones (solo cursos). | `Presentacion.publicar(...)` según `rolVendedor` |
| 13 (H) | No se puede publicar más café del que tiene el Origen: la suma de lo publicado desde una Transformación no supera su cantidad resultante, y el café sin tostar publicado desde un Lote se descuenta de su cantidad disponible. Lo mismo al reponer stock. | `Transformacion.asignarCantidad()` / `Lote.descontar()`, coordinados por `ServicioDePublicacion` y `ServicioDeReposicion` |
| 14 (I) | El Vendedor puede programar un **Descuento por temporada** sobre una Presentación: un porcentaje **entre 1 % y 50 %** que rige entre dos fechas (ambas incluidas; la de fin no puede ser anterior a la de inicio). Fuera de ese rango de fechas se vende al Precio normal; el Precio original nunca se modifica. | `Descuento` (rango y fechas) / `Precio.aplicarDescuento()` / `Presentacion.precioVigente(hoy)` |

## Reglas de compra y cursos

Implementadas en el código:

- La Compra pendiente es el carrito: solo mientras está PENDIENTE se agregan, cambian o quitan
  Presentaciones (`Compra.agregarPresentacion()`, `cambiarCantidad()`, `quitarPresentacion()`).
- Cada Presentación ocupa una sola línea del carrito: si se agrega de nuevo, se suma la cantidad (en la
  misma unidad) y se conserva el precio congelado la primera vez (`Compra.agregarPresentacion()`).
- El precio que se congela es el vigente: con el Descuento por temporada si rige ese día
  (`Presentacion.precioVigente(hoy)` en `RealizarCompra`).
- El stock se verifica sobre la cantidad ya sumada de cada Presentación, y si algo no alcanza no se
  descuenta nada (`RealizarCompra`, todo o nada).
- Una misma Compra no puede confirmarse dos veces, y para confirmarla se necesitan al menos un
  detalle y la Dirección de Envío (`Compra.confirmar(direccion)`); recomprar la misma Presentación en
  otro pedido sí.
- Al entregar una Compra se registra la fecha y hora de entrega (`Compra.entregar(fechaHora)`).
- Una Compra se cancela sin costo mientras no se haya enviado (`Compra.cancelar()`); las transiciones
  de estado siguen un flujo válido (`EstadoDeCompra.puedeTransicionarA()`).
- Un Comprador no puede comprar un Curso que ya tiene activo (`ComprarCurso`).
- Un Curso solo se reembolsa dentro de 48 h y antes de acceder al contenido (`Inscripcion`); el
  contenido solo es accesible con una Inscripción vigente (`AccederLeccion`).
- Un Lote solo puede estar en estado Verde o Pergamino (`Lote.registrar()`).
- Un Comprador debe ser mayor de edad al registrarse (`Comprador`, 18 años).

Pendientes (diseñadas, aún no implementadas): máximo una calificación por Compra entregada, reembolso
de café físico dentro de 48 h tras la entrega, y la restricción de derivados alcohólicos a mayores de edad
al comprar.
