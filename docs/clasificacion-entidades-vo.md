# Clasificación Entidad / Value Object — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2

Las 3 pruebas: **P1 identidad** (¿tiene id propio que la distingue?), **P2 ciclo de vida** (¿cambia
y esa historia importa?), **P3 reemplazo** (¿dos con los mismos datos son intercambiables?).
Entidad = P1 y P2 sí, P3 no. Value Object = P1 y P2 no, P3 sí.

## Entidades

| Concepto | P1 identidad | P2 ciclo de vida | P3 reemplazo (no intercambiables porque…) |
| --- | --- | --- | --- |
| **Presentación** (raíz abstracta) | ID propio; sigue siendo la misma aunque cambie stock o precio | Publicar, descontar cantidad, dar de baja | Dos con los mismos atributos son distintas si las publican vendedores distintos o vienen de orígenes distintos |
| **PresentaciónTrazable** (subclase) | Hereda identidad + Origen obligatorio | Además controla frescura y descuenta de la Transformación | Dos bolsas de 500 g Tueste Medio de Transformaciones distintas tienen Ficha de Origen distinta |
| **ArticuloDeMerchandising** (subclase) | Hereda identidad; sin Origen | Ciclo común (publicación, stock, baja) | Dos manillas idénticas de vendedores distintos son artículos distintos |
| **Lote** | Código único ("Lote #045"), independiente de su cantidad | Su cantidad se descuenta con cada Transformación | Dos Lotes de la misma Finca, Cosecha y Proceso no son el mismo |
| **Transformación** | Cada ejecución es un evento distinguible (Lotes, entrada, merma, Tostador, fecha) | Queda como hecho histórico y alimenta la Ficha de Origen | Mismos Lotes y cantidad pero otro momento o Tostador = otra Transformación |
| **Finca** | Nombre, ubicación y altura propios; pertenece a un Caficultor | Acumula Cosechas y Lotes en el tiempo | Mismo nombre con distinto dueño = otra Finca |
| **Vendedor** | Identidad y rol propios que no cambian | Acumula Lotes, Transformaciones y Presentaciones | Dos caficultores con fincas similares no son el mismo vendedor |
| **Comprador** | Se identifica por su cuenta, no por contacto | Su historial de compras importa | Dos con el mismo nombre no son el mismo |
| **Compra** (raíz abstracta) | ID único para trazabilidad y disputas | Cambia de estado (pendiente → confirmada…) con transiciones válidas | Mismo comprador y monto en otra fecha = otra Compra |
| **CompraFisica** (subclase) | Hereda identidad; agrega dirección de envío y detalles | Además tiene tramo logístico (enviada, entregada) | Dos compras con las mismas Presentaciones en pedidos distintos son eventos distintos |
| **CompraDigital** (subclase) | Hereda identidad; agrega el Curso y el precio congelado | Nace confirmada y deja una Inscripción; sin envío | Dos compras del mismo Curso por compradores distintos son independientes |
| **Reseña** *(diseño; aún no implementada en el código)* | ID propio, pertenece a una Compra | Se crea con la Compra entregada; puede eliminarse lógicamente | Mismo texto de Compras distintas = reseñas distintas |
| **Solicitud de Reembolso** | ID propio, asociada a una Compra | Solicitada → Aprobada / Rechazada | Mismo motivo en momentos distintos = solicitudes distintas |
| **Notificación** | ID y destinatario | No leída → leída | Mismo mensaje en momentos distintos = avisos distintos |
| **Curso** | ID propio; sigue siendo el mismo aunque cambie precio o contenido | Publicación, compras, eliminación lógica | Mismo título y precio de Formadores distintos = cursos distintos |
| **Lección** (interna de Curso) | Número de orden dentro del Curso | Se edita, reordena o reemplaza | Mismo título en cursos distintos = lecciones distintas |
| **Inscripción** | ID propio; une Comprador, Curso y Compra | Registra primer acceso y revocación | Dos inscripciones al mismo curso de compradores distintos son independientes |

## Value Objects

| Concepto | Tipo | Por qué es VO (sin ID · sin historia · intercambiable) |
| --- | --- | --- |
| **Precio** | record | Par monto + moneda; si cambia es otro valor, no "el mismo precio editado" |
| **Cantidad** | record | Valor + unidad (kg, g, ml, unidad); igual por valor y unidad (no convierte: `0,5 kg` y `500 g` son valores distintos); restar devuelve una nueva |
| **DetalleDeCompra** | record | Línea de una Compra física (compraId, presentacionId, precio congelado, cantidad); sin identidad propia, se compara por valor |
| **PorcentajeDeMerma** | record | Porcentaje de peso perdido al tostar (12 %–22 %); dos Transformaciones con 18 % comparten el valor |
| **CodigoDeLote** | record | Formato `LOT-AAAA-NNN`; el código en sí no tiene historia |
| **Cosecha** | record | Año + temporada (`2024-Traviesa`); solo etiqueta un Lote |
| **OrigenDePresentacion** | record | Referencia a un Lote **o** una Transformación (exactamente uno); fijo al publicar |
| **FechaDeTueste** | record | Solo una fecha (nunca futura) |
| **NotaDeCata** | record | Descriptores de sabor; dos notas iguales son intercambiables |
| **Galeria / ImagenDePresentacion** | record | Entre 1 y 10 imágenes, una principal; agregar una imagen produce otra Galería |
| **Altitud / Ubicación / Email / Teléfono / Contraseña / FechaDeNacimiento** | record | Valores de atributo sin identidad; si cambian, se reemplazan |
| **FichaDeOrigen** | record de lectura | Reconstrucción de la cadena; no se persiste ni se edita |
| **TipoDePresentacion, EstadoDePublicacion, PerfilTueste, ProcesoDeBeneficio, VariedadDeCafe, EstadoDelCafe, TemporadaDeCosecha, RolVendedor, TipoDeComprador, EstadoDeReembolso, TipoDeNotificacion** | enum | Conjunto cerrado de valores, sin identidad ni historia propia; quien cambia de estado es la entidad |
| **ArchivoDigital, TamanoDeArchivo, TipoDeArchivo, Duracion, NivelDelCurso, TematicaDelCurso** | record / enum | Atributos de Curso y Lección, reemplazables por valor |
