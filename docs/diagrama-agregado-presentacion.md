# Diagrama de Agregado — Presentación

**E-commerce de café de origen — Café Trazado**
Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

Diagrama: [`diagrama-agregado-presentacionV2.png`](./diagrama-agregado-presentacionV2.png)

---

**Raíz:** `Presentacion` — **abstracta**. Concentra el ciclo de vida comercial común y delega en
dos subclases según el tipo de producto:

- `PresentacionTrazable` — café verde, tostado y derivados consumibles. **Siempre** tiene Origen.
- `ArticuloDeMerchandising` — producto no alimenticio, con `material` y `descripcion`. **Nunca** tiene
  Origen, Perfil ni Fecha de Tueste, así que por construcción no puede violar la regla B.

La única forma de crear una Presentación es el factory `Presentacion.publicar(...)`: valida lo común
(identidad, vendedor, título, precio, cantidad, galería y rol) y delega en el `publicar(...)` de la
subclase que corresponde según `TipoDePresentacion`, que valida sus propias reglas.

**Sobre `rolVendedor`.** Los `publicar(...)` reciben `rolVendedor : RolVendedor` (CAFICULTOR, TOSTADOR,
VENDEDOR_DERIVADOS, FORMADOR). No es un atributo de la Presentación: el rol pertenece al Vendedor, que está
**fuera del agregado**, así que solo entra como dato de validación al publicar (reglas A, FORMADOR no
publica y solo VENDEDOR_DERIVADOS publica Merchandising) y no se guarda.

## Invariantes

1. **La cantidad disponible nunca puede ser negativa.** `descontarCantidad()` delega en
   `Cantidad.restar()`, que lanza `ReglaDominioException` si no alcanza; al llegar a cero la
   Presentación queda `AGOTADA`. La cantidad inicial siempre debe ser mayor que cero.

2. **Una Presentación de `MERCHANDISING` nunca puede tener Origen, Perfil ni Fecha de Tueste, y
   cualquier otro tipo siempre debe tener exactamente uno de `loteId` o `transformacionId`**
   (regla B). Si una Presentación de `MERCHANDISING` llega con `loteId` o `transformacionId`, se
   rechaza. El record `OrigenDePresentacion` rechaza traer los dos o ninguno. El origen además
   debe corresponder al tipo: `CAFE_VERDE` viene de un Lote; `CAFE_TOSTADO` y
   `DERIVADO_CONSUMIBLE`, de una Transformación.

3. **Un café tostado nunca puede publicarse si han pasado más de 30 días desde su Fecha de Tueste**
   (regla D). Después de publicado, `verificarVencimiento()` usa `estaFresca()` y lo pasa a
   `VENCIDA` cuando se cumple el plazo. Además, `estaDisponibleParaVenta()`
   exige `estaFresca()` en la Presentación Trazable: un café pasado de fecha nunca puede venderse, aunque
   su estado aún no se haya actualizado.

4. **Los perfiles de tueste especializados (`CLARO`, `MEDIO`, `OSCURO`) solo puede publicarlos un
   `TOSTADOR`; cualquier otro rol que publique café tostado siempre lo hace en `TRADICIONAL`**
   (regla A). El café sin tostar no lleva perfil. Un `FORMADOR` nunca puede publicar
   Presentaciones, y solo un `VENDEDOR_DERIVADOS` puede publicar un Artículo de Merchandising.

5. **Un Artículo de Merchandising siempre se cuenta en `unidad` y siempre debe tener material y
   descripción; una Presentación Trazable nunca se cuenta en `unidad`**, siempre en peso o volumen
   (kg, g, ml).

6. **Una Presentación dada de baja nunca puede volver a operarse y nunca se borra físicamente.**
   `darDeBaja()` marca `eliminada` y `validarNoEliminada()` bloquea los métodos posteriores. Pausar
   la venta es otra cosa: `desactivar()` pausa una Presentación `ACTIVA` y `activar()` la devuelve a
   `ACTIVA`. **Pausar y reactivar nunca puede devolver a la venta una Presentación `AGOTADA` o
   `VENCIDA`:** solo se pausa lo que está activo, `AGOTADA` se marca sola al quedar sin stock y una
   Presentación sin cantidad disponible nunca queda `ACTIVA`.

7. **La Galería siempre debe tener entre 1 y 10 imágenes con exactamente una principal, y el Precio
   siempre debe ser mayor que cero.** Se validan en el constructor de cada record.

## Aclaración: `INACTIVA` frente a `eliminada`

No son lo mismo, y por eso conviven:

| | `INACTIVA` (estado) | `eliminada` (boolean) |
| --- | --- | --- |
| Significado | Pausa de la venta | Baja definitiva |
| Se activa con | `desactivar()` (solo desde `ACTIVA`) | `darDeBaja()` |
| ¿Reversible? | Sí, con `activar()` (solo desde `INACTIVA` y con cantidad disponible) | No |
| ¿La Presentación sigue existiendo? | Sí, el Vendedor puede reactivarla | Ya no se puede operar; nunca se borra físicamente |

Una Presentación `INACTIVA` sigue existiendo y puede volver a `ACTIVA`; una `eliminada` ya no existe
para el negocio, y cualquier método posterior falla en `validarNoEliminada()`.

## Dentro del límite

| Value Object | Pertenece a | Contenido |
| --- | --- | --- |
| `Precio` | raíz | monto, moneda |
| `Cantidad` | raíz | valor, unidad (kg, g, ml, unidad) |
| `TipoDePresentacion` | raíz | CAFE_VERDE, CAFE_TOSTADO, DERIVADO_CONSUMIBLE, MERCHANDISING |
| `EstadoDePublicacion` | raíz | ACTIVA, AGOTADA, VENCIDA, INACTIVA |
| `Galeria` → `ImagenDePresentacion` | raíz | 1..10 imágenes, una principal |
| `OrigenDePresentacion` | `PresentacionTrazable` | loteId **o** transformacionId |
| `PerfilTueste` | `PresentacionTrazable` | TRADICIONAL, CLARO, MEDIO, OSCURO |
| `FechaDeTueste` | `PresentacionTrazable` | fecha, nunca futura |
| `NotaDeCata` | `PresentacionTrazable` | lista de descriptores, no vacía |

## Fuera del agregado

| Entidad | Referencia |
| --- | --- |
| `Vendedor` | `vendedorId` |
| `Lote` | `origen.loteId` |
| `Transformación` | `origen.transformacionId` |
| `Compra` | la Compra guarda `presentacionId` en su `DetalleDeCompra` |
| `Reseña` | llega vía `compraId` |
| `FichaDeOrigen` | se arma por lectura, no se persiste |

---

## Regla de transacción

Cada método de negocio de la raíz se guarda en **una única transacción** y deja el agregado en estado válido. Si alguna invariante falla, se lanza `ReglaDominioException` y no se modifica nada.

Cuando una operación toca otro agregado, la coordinación vive en el caso de uso, no en la entidad. Al
publicar una Presentación Trazable, `PublicarPresentacion` busca su Origen y le descuenta la cantidad
publicada: si es café sin tostar, verifica que el **Lote** exista y llama a `Lote.descontar()`; si es
tostado o derivado, verifica que la **Transformación** exista y llama a
`Transformacion.asignarCantidad()`. Así nunca se publica más café del que realmente tiene el origen, y
si el origen no existe o no alcanza, la Presentación no se crea.
