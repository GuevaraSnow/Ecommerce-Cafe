# Diagrama de Agregado — Lote

**E-commerce de café de origen — Café Trazado**
Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

Diagrama: [`diagrama-agregado-lote.png`](./diagrama-agregado-lote.png)

---

**Raíz:** `Lote` — la cantidad de café recolectada en una misma Finca, Cosecha y Proceso de
Beneficio. Es el punto de partida de toda la trazabilidad y el guardián de la regla C: nadie
transforma ni publica más café del que el Lote tiene disponible.

## Invariantes

1. **Un Lote nunca puede registrarse en estado `TOSTADO`** — solo `VERDE` o `PERGAMINO`. El tueste
   es resultado de una Transformación, no un estado de origen.

2. **La cantidad inicial de un Lote siempre debe ser mayor que cero.**

3. **La cantidad disponible nunca puede quedar negativa** (regla C). `descontar()` delega en
   `Cantidad.restar()`, que rechaza la operación si se intenta usar más café del disponible, ya sea
   para una Transformación o para publicar café sin tostar como Presentación.

4. **Un Lote dado de baja nunca puede transformarse ni publicarse.** `descontar()` lo verifica antes
   de operar.

5. **Un Lote nunca se borra físicamente:** solo `eliminarLogicamente()` (la baja), para no romper la
   trazabilidad de las Transformaciones y Presentaciones derivadas. Un Lote dado de baja deja de mostrarse en
   la consulta, pero se sigue pudiendo cargar para la Ficha de Origen y para rechazar usos nuevos.

6. **El Código de Lote es la identidad, es inmutable y siempre sigue el formato `LOT-AAAA-NNN`.**
   `equals()` y `hashCode()` se calculan solo sobre él. Lo genera el servidor (`ServicioDeLote`): el año de
   registro más el siguiente número de ese año, de 001 a 999.

7. **Una Cosecha solo se registra si es del año de registro o del anterior** (regla del registro, en
   `ServicioDeLote`, no del valor `Cosecha`): un Lote ya guardado conserva su Cosecha aunque pasen los años,
   y por eso `Lote.reconstruir(...)` no repite las validaciones de creación.

## Dentro del límite

| Value Object | Contenido |
| --- | --- |
| `CodigoDeLote` | valor con formato `LOT-AAAA-NNN` — identidad del Lote |
| `Cantidad` | valor, unidad (kg, g, ml, unidad) |
| `Cosecha` → `TemporadaDeCosecha` | año + PRINCIPAL / TRAVIESA |
| `ProcesoDeBeneficio` | Lavado, Honey, Natural |
| `VariedadDeCafe` | nombre (Castillo, Caturra, Geisha…) |
| `EstadoDelCafe` | VERDE, PERGAMINO, TOSTADO (TOSTADO describe el café, pero un Lote nunca puede estar en él: invariante 1) |

## Fuera del agregado

| Entidad | Referencia |
| --- | --- |
| `Finca` | `fincaID` |
| `Transformación` | la Transformación guarda el `loteCodigo` del que sale |
| `PresentacionTrazable` | la Presentación guarda `origen.loteId`; al publicarla como café sin tostar se descuenta del Lote |
| `FichaDeOrigen` | se arma por lectura |

---

## Regla de transacción

Cada método de negocio de la raíz se guarda en **una única transacción** y deja el agregado en estado válido. Si alguna invariante falla, se lanza `ReglaDominioException` y no se modifica nada. Cuando una operación toca dos agregados, la coordinación vive en el caso de uso, no en las entidades: `RegistrarTransformacion` (con `ServicioDeTransformacion`) descuenta del Lote la cantidad de entrada del tueste, y `PublicarPresentacion` (con `ServicioDePublicacion`) descuenta del Lote el café sin tostar que se publica.
