# Clasificación Entidad / Value Object — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Las 3 pruebas: **P1 identidad** (¿tiene un id propio que la distingue, independiente de sus
atributos?), **P2 ciclo de vida** (¿cambia y esa historia importa?), **P3 reemplazo** (si dos tienen
**todos** los datos iguales, ¿son intercambiables?).
Entidad = P1 y P2 sí, P3 no. Value Object = P1 y P2 no, P3 sí.

En la P3 de una entidad no basta con cambiar un dato ("otro vendedor", "otra fecha"): eso ya sería
otro valor. Una entidad sigue siendo distinta **aun con todos sus datos idénticos**, porque lo único
que la separa es su id.

## Entidades

| Concepto | P1 identidad | P2 ciclo de vida | P3 reemplazo (con todos los datos iguales…) |
| --- | --- | --- | --- |
| **Presentación** (raíz abstracta) | `id` propio; sigue siendo la misma aunque cambie stock o precio | Publicar, descontar y reponer cantidad, agotarse, pausarse, dar de baja | Dos publicaciones idénticas siguen siendo dos: si una se agota o se da de baja, la otra sigue a la venta |
| **PresentaciónTrazable** (subclase) | Hereda el `id` + Origen obligatorio | Además controla frescura (puede quedar Vencida) y recibe Nota de Cata | Dos bolsas idénticas del mismo Origen son dos publicaciones con stock independiente |
| **ArticuloDeMerchandising** (subclase) | Hereda el `id`; sin Origen | Ciclo común (stock, pausa, baja) | Dos manillas idénticas del mismo vendedor son dos artículos con stock independiente |
| **Lote** | `CodigoDeLote` (`LOT-2026-045`), independiente de su cantidad | Su cantidad se descuenta con cada Transformación; eliminación lógica | Dos Lotes con la misma Finca, Cosecha, Proceso y cantidad siguen siendo dos: tostar uno no descuenta del otro |
| **Transformación** | `id` propio | Su cantidad resultante disponible baja con cada Presentación publicada desde ella | Dos tuestes idénticos (mismos Lotes, cantidad, merma, Tostador y fecha) son dos eventos, cada uno con su propio saldo |
| **Finca** | `id` propio (el nombre puede repetirse); pertenece a un Caficultor | Se registra y puede eliminarse lógicamente sin perder la trazabilidad de sus Lotes | Dos Fincas "La Esperanza" del mismo dueño, municipio y altura siguen siendo dos terrenos |
| **Vendedor** | `id` propio, generado al registrarse | Se registra y puede darse de baja; lo que publicó sigue atribuido a él | Dos Vendedores con el mismo nombre y rol son dos cuentas distintas |
| **Comprador** | `id` propio (su cuenta), no sus datos de contacto | Cambia contraseña, recibe códigos de recuperación, eliminación lógica | Dos Compradores con el mismo nombre y teléfono son dos personas distintas |
| **Compra** (raíz abstracta) | `id` propio para trazabilidad y disputas | Cambia de estado (pendiente → confirmada…) con transiciones válidas | Dos Compras idénticas (mismo comprador, productos y fecha) son dos pedidos: cancelar uno no cancela el otro |
| **CompraFisica** (subclase) | Hereda el `id`; agrega dirección de envío y detalles | Además tiene tramo logístico (enviada, entregada) | Dos pedidos idénticos se envían y entregan por separado |
| **CompraDigital** (subclase) | Hereda el `id`; agrega el Curso y el precio congelado | Se confirma al comprar el Curso y deja una Inscripción; sin envío | Dos compras idénticas del mismo Curso son dos transacciones distintas |
| **Reseña** *(diseño; aún no implementada en el código)* | `id` propio, pertenece a una Compra | Se crea con la Compra entregada; puede eliminarse lógicamente | Dos Reseñas con el mismo texto y calificación son dos opiniones distintas |
| **Solicitud de Reembolso** | `id` propio, asociada a una Compra | Solicitada → Aprobada / Rechazada | Dos solicitudes idénticas son dos trámites: aprobar una no resuelve la otra |
| **Notificación** | `id` propio y destinatario | No leída → leída | Dos avisos idénticos son dos: leer uno no marca el otro |
| **Curso** | `id` propio; sigue siendo el mismo aunque cambie precio o contenido | Publicación, nuevas Lecciones, eliminación lógica | Dos Cursos idénticos (mismo Formador, título y precio) son dos cursos con inscritos distintos |
| **Lección** (interna de Curso) | Identidad **local**: su número de orden dentro del Curso; fuera del Curso no existe | Su Archivo Digital puede reemplazarse sin dejar de ser la misma Lección | Dos Lecciones con el mismo título y archivo en posiciones distintas son dos lecciones |
| **Inscripción** | `id` propio; une Comprador, Curso y Compra | Registra primer acceso y revocación | Dos Inscripciones idénticas son dos accesos: revocar una no revoca la otra |

## Value Objects

| Concepto | Tipo | Por qué es VO (sin ID · sin historia · intercambiable) |
| --- | --- | --- |
| **Precio** | record | Par monto + moneda; si cambia es otro valor, no "el mismo precio editado" |
| **Cantidad** | record | Valor + unidad (kg, g, ml, unidad); igual por valor y unidad (no convierte: `0,5 kg` y `500 g` son valores distintos); restar devuelve una nueva |
| **DetalleDeCompra** | record | Línea de una Compra física (compraId, presentacionId, precio congelado, cantidad); sin identidad propia, se compara por valor |
| **PorcentajeDeMerma** | record | Porcentaje de peso perdido al tostar (12 %–22 %); dos Transformaciones con 18 % comparten el valor |
| **CodigoDeLote** | record | Formato `LOT-AAAA-NNN`; el código en sí no tiene historia |
| **VariedadDeCafe** | record | Un nombre (Castillo, Caturra, Geisha…) validado en el constructor; no es un conjunto cerrado, así que no es enum; dos Lotes "Caturra" comparten el valor |
| **Cosecha** | record | Año + temporada (`2026-Traviesa`); solo etiqueta un Lote |
| **OrigenDePresentacion** | record | Referencia a un Lote **o** una Transformación (exactamente uno); fijo al publicar |
| **FechaDeTueste** | record | Solo una fecha (nunca futura) |
| **NotaDeCata** | record | Descriptores de sabor; dos notas iguales son intercambiables |
| **Galeria / ImagenDePresentacion** | record | Entre 1 y 10 imágenes, una principal; agregar una imagen produce otra Galería |
| **Altitud / Ubicación / Email / Teléfono / Contraseña / FechaDeNacimiento** | record | Valores de atributo sin identidad; si cambian, se reemplazan |
| **DireccionDeEnvio** | record | Ciudad + dirección + destinatario; queda fija en la Compra aunque el comprador cambie de dirección después |
| **CodigoDeRecuperacion** | record | Código + fecha de expiración (15 min); si vence se genera otro, no se edita |
| **FichaDeOrigen** | record de lectura | Reconstrucción de la cadena; no se persiste ni se edita |
| **EstadoDeCompra, TipoDePresentacion, EstadoDePublicacion, PerfilTueste, ProcesoDeBeneficio, EstadoDelCafe, TemporadaDeCosecha, RolVendedor, TipoDeComprador, EstadoDeReembolso, TipoDeNotificacion** | enum | Conjunto cerrado de valores, sin identidad ni historia propia; quien cambia de estado es la entidad |
| **ArchivoDigital, TamanoDeArchivo, TipoDeArchivo, Duracion, NivelDelCurso, TematicaDelCurso** | record / enum | Atributos de Curso y Lección, reemplazables por valor |
