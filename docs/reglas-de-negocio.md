# Reglas de Negocio Innegociables — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Las reglas se protegen en el dominio (lanzan `ReglaDominioException`, que la API traduce a 422); los
casos de uso solo coordinan. La columna "Dónde vive" indica quién la hace cumplir.

## Reglas base

| # | Regla | Dónde vive |
| --- | --- | --- |
| 1 | Una Presentación no puede tener cantidad disponible negativa. | `Cantidad.restar()` / `Presentacion.descontarCantidad()` |
| 2 | Un Comprador no puede dejar una Reseña (calificación y comentario) de una Presentación sin haberla adquirido (Compra **entregada**), y solo puede reseñar cada Presentación una vez. Solo el vendedor dueño de la Presentación responde la Reseña, y una sola vez. La Nota de Cata no aplica aquí: la escribe el vendedor. | `VerificadorDeCompra` (compra entregada y reseña única) + `DejarResena` / `Resena.responder()` (dueño y respuesta única) |
| 3 | Un vendedor no puede eliminar una Presentación con compras activas; solo eliminación lógica (se conserva para trazabilidad e historial). | `ServicioDeBajaDePresentacion` (consulta las compras activas) + `Presentacion.darDeBaja()` |
| 4 | Un Lote nunca se borra físicamente, solo se da de baja de forma lógica (borrarlo rompería la trazabilidad de las Transformaciones y Presentaciones derivadas); un Lote dado de baja ya no se consulta ni se puede usar. Solo lo da de baja el Caficultor dueño de su Finca. | `Lote.eliminarLogicamente()` / `Lote.descontar()` + `EliminarLote` + `AutorizacionDeLote` |
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
| 12 (G) | Solo el Vendedor de Derivados puede publicar Artículos de Merchandising, y un Comprador no puede publicar Presentaciones. | `Presentacion.publicar(...)` según `rolVendedor` |
| 13 (H) | No se puede publicar más café del que tiene el Origen: la suma de lo publicado desde una Transformación no supera su cantidad resultante, y el café sin tostar publicado desde un Lote se descuenta de su cantidad disponible. Lo mismo al reponer stock. | `Transformacion.asignarCantidad()` / `Lote.descontar()`, coordinados por `ServicioDePublicacion` y `ServicioDeReposicion` |
| 14 (I) | El Vendedor puede programar un **Descuento por temporada** sobre una Presentación: un porcentaje **entre 1 % y 50 %** que rige entre dos fechas (ambas incluidas; la de fin no puede ser anterior a la de inicio). Fuera de ese rango de fechas se vende al Precio normal; el Precio original nunca se modifica. | `Descuento` (rango y fechas) / `Precio.aplicarDescuento()` / `Presentacion.precioVigente(hoy)` |
| 15 (J) | El código de un Lote lo genera el servidor, con el formato `LOT-AAAA-NNN`: el año de registro y un número consecutivo de ese año (001 a 999; el siguiente es el más alto del año más uno). El cliente nunca lo escribe. | `ServicioDeLote` + `CodigoDeLote.crear()` + `LoteRepositorio.obtenerNumeroMasAlto()` |
| 16 (K) | Un Lote solo se registra con una Cosecha del año de registro o del anterior; un Lote ya guardado conserva su Cosecha aunque pasen los años. | `ServicioDeLote` + `Cosecha.esRegistrable()` |
| 17 (L) | El Rol de quien publica sale del Usuario real, no de lo que envíe el cliente; un Comprador no publica. | `ServicioDePublicacion` + `Presentacion.publicar(...)` |

## Reglas de compra

Implementadas en el código:

- La Compra pendiente es el carrito: solo mientras está PENDIENTE se agregan, cambian o quitan
  Presentaciones (`Compra.agregarPresentacion()`, `cambiarCantidad()`, `quitarPresentacion()`).
- Cada Presentación ocupa una sola línea del carrito: si se agrega de nuevo, se suma la cantidad (en la
  misma unidad) y se conserva el precio congelado la primera vez (`Compra.agregarPresentacion()`).
- El precio que se congela es el vigente: con el Descuento por temporada si rige ese día
  (`ServicioDeCompra.agregarAlCarrito()` usa `Presentacion.precioVigente(hoy)`).
- Al agregar al carrito se verifica, sin descontar nada, que la Presentación esté a la venta y que su
  stock alcance para el total que quedaría en el carrito (`ServicioDeCompra.agregarAlCarrito()`).
  Agregar al carrito no aparta stock: gana quien confirma primero.
- El stock se descuenta al confirmar, no al agregar al carrito. Al confirmar se vuelve a verificar el
  stock de todos los ítems (por si alguien compró antes) y, si alguno no alcanza, no se descuenta nada
  (`ServicioDeCompra.confirmar()`, todo o nada).
- Al cancelar una Compra confirmada se le devuelve el stock a cada Presentación (salvo a las dadas de
  baja); un carrito pendiente nunca descontó stock, así que solo se cancela (`ServicioDeCompra.cancelar()`).
- Una misma Compra no puede confirmarse dos veces, y para confirmarla se necesitan al menos un
  detalle y la Dirección de Envío (`Compra.confirmar(direccion)`); recomprar la misma Presentación en
  otro pedido sí.
- Al entregar una Compra se registra la fecha y hora de entrega (`Compra.entregar(fechaHora)`).
- Una Compra se cancela sin costo mientras no se haya enviado (`Compra.cancelar()`); las transiciones
  de estado siguen un flujo válido (`EstadoDeCompra.puedeTransicionarA()`).
- El reembolso de café físico es **por Presentación**: pedir el de todo el carrito crea una solicitud
  por cada una. Solo lo pide quien hizo la Compra, sobre una Compra ENTREGADA, dentro de las 48 horas
  siguientes a la entrega (las 48 h exactas todavía cuentan) y para una Presentación que está en la
  Compra (`Compra.verificarQueSePuedeReembolsar()` + `ServicioDeReembolso.solicitar()`).
- No puede haber otra solicitud pendiente o aprobada de la misma Presentación en la misma Compra; si se
  la rechazaron, se puede volver a pedir dentro de las 48 h (`SolicitudReembolsoRepositorio.existeSolicitudVigente()`).
- Solo el vendedor dueño de la Presentación aprueba o rechaza la solicitud, y una sola vez
  (`SolicitudReembolso.aprobar()` / `rechazar()`).
- La Compra pasa a REEMBOLSADA solo cuando todas sus Presentaciones tienen el reembolso aprobado; con un
  reembolso parcial (por ejemplo 3 de 4) sigue ENTREGADA. El stock no se devuelve: el café ya se
  entregó (`ServicioDeReembolso.aprobar()` + `Compra.reembolsar()`).
- Solo cuentan como venta las Compras confirmadas, enviadas y entregadas (`EstadoDeCompra.VENTAS`); el
  ranking de las Presentaciones más vendidas no incluye las pendientes, canceladas ni reembolsadas.
- Un Lote solo puede estar en estado Verde o Pergamino (`Lote.registrar()`).

## Reglas de usuarios y acceso

- Un usuario debe ser mayor de edad al registrarse (`Usuario.registrar()`, 18 años) y su correo es único
  (`VerificadorDeEmail`).
- La contraseña debe tener al menos 8 caracteres, una mayúscula y un número (`ContrasenaPlana`); se guarda
  siempre encriptada, también al cambiarla y al restablecerla con el código de recuperación
  (`ServicioDeContrasena`).
- El código de recuperación vence a los 15 minutos y se descarta al usarse (`CodigoDeRecuperacion`,
  `Usuario.cambiarContrasena()`).
- El inicio de sesión no revela por qué falla: un correo inexistente, una contraseña errada y un usuario
  dado de baja responden igual (`ServicioDeAutenticacion`, 401).
- Quién es el usuario y su Rol salen del token JWT; cada operación declara qué Rol la puede ejecutar y, si
  corresponde, que el usuario sea el dueño (`@PreAuthorize`, `AutorizacionDePresentacion`,
  `AutorizacionDeCompra`, `AutorizacionDeLote`).

Pendientes (diseñadas, aún no implementadas): la restricción de derivados alcohólicos a mayores de edad
al comprar.
