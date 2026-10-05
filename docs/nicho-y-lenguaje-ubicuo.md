# Nicho y Lenguaje Ubicuo — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

## Nicho

Marketplace de **café de origen**. No hay revendedores de productos genéricos: los vendedores son
quienes cultivan y transforman el café, y su diferenciador es la **transparencia total** sobre
tierra, proceso y preparación (región y altura en msnm, proceso de beneficio, variedad y estado
del grano).

**Quién compra y por qué**

- **Comprador Final (B2C):** compra café tostado para consumo personal; busca calidad, explorar
  procesos de beneficio y apoyar al caficultor con un precio justo, sin intermediarios.
- **Comprador Comercial (B2B):** tostadurías, cafeterías de especialidad y comercios; compra café
  verde, pergamino o grandes volúmenes tostados, y necesita trazabilidad exacta (finca, cosecha,
  proceso) para controlar sus curvas de tueste.

**Roles de vendedor**

- **Caficultor:** dueño de la Finca. Vende café crudo (Lote) o su propio café tostado tradicional.
- **Tostador:** compra café crudo y lo transforma (tueste) para vender tostado.
- **Vendedor de Derivados:** usa café como materia prima para miel, licor, cosméticos, merchandising.
- **Formador:** publica cursos virtuales (contenido digital). No publica Presentaciones físicas.

## Cadena de trazabilidad

```
Finca (dueña: Caficultor)
 └── produce → Lote (de una Cosecha, con un Proceso de Beneficio)
      ├── se vende directo como → Presentación (verde / pergamino)
      └── pasa por → Transformación (Tostador / Vendedor de Derivados)
           └── genera → Presentación (tostada / derivada, con Perfil de Tueste y Notas de Cata)
```

## Glosario (9 términos propios del nicho)

| Término | Definición | Ejemplo |
| --- | --- | --- |
| **Finca** | Terreno de cultivo con nombre, ubicación y altura (msnm), propiedad de un Caficultor. Punto de partida de la trazabilidad. | "Finca La Esperanza", Pitalito, Huila, 1.750 msnm |
| **Cosecha** | Año y temporada (Principal / Traviesa) en que se recolectó el café. | "Cosecha 2024 - Traviesa" |
| **Lote** | Cantidad de café recolectada en una misma Finca, Cosecha y Proceso de Beneficio. Código único; no se vende directo al comprador final. | "Lote #045": 200 kg de pergamino, proceso Lavado |
| **Proceso de Beneficio** | Método para retirar la pulpa y secar el grano (Lavado, Honey, Natural). Define buena parte del sabor. | El Lote #045 usó Lavado: sabor limpio y ácido |
| **Perfil de Tueste** | Nivel de tueste de una Presentación: Tradicional (Caficultor) o Claro / Medio / Oscuro (Tostador). | Jorge solo ofrece Tueste Tradicional |
| **Transformación** | Acción de tomar uno o varios Lotes como insumo para generar un producto nuevo; registra la merma por humedad. | "Café del Valle" usó 50 kg del Lote #045 y obtuvo 41 kg tostados |
| **Presentación** | Unidad final empacada que el Comprador adquiere; proviene de un Lote o de una Transformación. | "Café del Valle, Tueste Medio, 500 g, $28.000" |
| **Nota de Cata** | Palabras que describen sabor y aroma de una Presentación. | "chocolate, caramelo, ligera acidez cítrica" |
| **Ficha de Origen** | Reconstrucción de la historia completa de una Presentación recorriendo la cadena hacia atrás. | Finca → Cosecha → Lote → Transformación → Notas |

## Anti-patrones (términos a evitar)

| No usar | Usar |
| --- | --- |
| "Producto" (genérico) | "Presentación" |
| "Vendedor" a secas | "Caficultor", "Tostador" o "Vendedor de Derivados" |
| "Borrar Lote / Presentación" | "Eliminación lógica" |
| "Reseña" (de la Presentación) | "Nota de Cata" |
| "Trazabilidad" (como objeto) | "Ficha de Origen" |
| `presentacion.setLote(lote)` | La Presentación *proviene de* un Lote: se expresa con `OrigenDePresentacion` |
