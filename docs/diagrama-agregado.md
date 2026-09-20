# Diagrama de Agregado — Presentación

**Café Trazado · E-commerce de café de origen**
Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

Diagrama: [`docs/DiagramaAgregado.drawio.png`](./diagrama-agregado.png)

**Raíz de agregado:** `Presentación` — la unidad final empacada que el comprador adquiere.
Es donde viven las reglas propias del nicho: frescura del tueste, perfil de tueste según el
rol del vendedor, trazabilidad hacia el origen y control de cantidad disponible.

---

## Invariantes

1. **La cantidad disponible nunca puede ser negativa.** `descontarCantidad()` rechaza la
   operación si el stock no alcanza.

2. **Una Presentación de café tostado nunca puede estar ACTIVA si han pasado más de 30 días
   desde su Fecha de Tueste** (regla D). `estaFresca()` es la condición que se evalúa en
   `publicar()` y en el momento de la compra.

3. **Una Presentación publicada por un Caficultor siempre debe tener Perfil de Tueste
   TRADICIONAL** (regla A). Los perfiles CLARO, MEDIO y OSCURO son exclusivos del Tostador.

4. **Toda Presentación de café o derivado consumible siempre debe apuntar a un Lote o a una
   Transformación de origen** (regla B). Solo los derivados no alimenticios quedan exentos.

5. **Una Presentación con compras activas nunca puede borrarse físicamente.** `darDeBaja()`
   marca `eliminadaLogicamente`, y el registro se conserva para la trazabilidad y el historial
   del comprador.

6. **La Galería siempre debe tener entre 1 y 10 imágenes, con exactamente una principal, y el
   Precio siempre debe ser mayor que cero.** Se validan en el constructor de cada record.

---

## Dentro del límite (value objects, composición)

Nacen, cambian y mueren con la Presentación, y se guardan en la misma transacción que la raíz.

| Value Object | Tipo |
| --- | --- |
| `Precio` | record — monto, moneda |
| `Cantidad` | record — valor, unidad |
| `TipoDeProducto` | enum — CAFE_VERDE, CAFE_TOSTADO, DERIVADO |
| `PerfilDeTueste` | enum — TRADICIONAL, CLARO, MEDIO, OSCURO |
| `EstadoDePublicacion` | enum — ACTIVA, AGOTADA, VENCIDA |
| `FechaDeTueste` | record — fecha |
| `NotaDeCata` | record — descriptores |
| `Galeria` → `ImagenDeProducto` | records — imágenes de la Presentación |

## Fuera del agregado (referencia por id)

Cada uno tiene ciclo de vida propio y es raíz de su propio agregado.

| Entidad | Referencia |
| --- | --- |
| `Vendedor` | `vendedorId` |
| `Lote` | `loteId` |
| `Transformación` | `transformacionId` |
| `Compra` | la Compra guarda `presentacionId` |
| `Reseña` | llega vía `compraId` |
| `FichaDeOrigen` | se arma por lectura, no se persiste |

---

## Métodos de negocio (sin setters)

```
+publicar(id, vendedorId, origenId, tipo, precio, cantidad, perfil, rol) : Presentacion
+descontarCantidad(cantidad) : void
+reponerCantidad(cantidad) : void
+cambiarPrecio(nuevoPrecio) : void
+registrarNotaCata(nota) : void
+reemplazarGaleria(galeria) : void
+estaFresca() : boolean
+marcarAgotada() : void
+darDeBaja() : void
```

Cada método se guarda en una única transacción y deja el agregado en estado válido. Si alguna
invariante falla, se lanza `ReglaDominioException` y no se modifica nada.
