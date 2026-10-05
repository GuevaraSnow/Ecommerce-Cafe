# Nicho y Lenguaje Ubicuo — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

> **Versión 2 del glosario.** Actualiza el glosario entregado al inicio del curso para que coincida con
> el modelo V3 (merma por humedad, Presentación Trazable / Artículo de Merchandising) y con el código
> actual. Cambios: se agregan **Porcentaje de Merma**, **Origen de Presentación**, **Artículo de
> Merchandising**, las variantes de compra (**Compra Física**, **Compra Digital**, **Precio Congelado**)
> y los términos de los cursos virtuales (**Curso**, **Lección**, **Inscripción**); se separan
> **Nota de Cata** (la escribe el vendedor) y **Reseña** (la escribe el comprador); los ejemplos de
> código usan los métodos reales del dominio.

## Nicho

Marketplace de **café de origen**. No hay revendedores de productos genéricos: los vendedores son
quienes cultivan y transforman el café, y su diferenciador es la **transparencia total** sobre
tierra, proceso y preparación (región y altura en msnm, proceso de beneficio, variedad y estado
del grano).

**Quién compra y por qué**

- **Comprador Final (B2C):** compra café tostado para consumo personal; busca calidad, explorar
  procesos de beneficio y apoyar al caficultor con un precio justo, sin intermediarios.
- **Comprador Comercial (B2B):** tostadurías, cafeterías de especialidad y comercios; compra café
  sin tostar (verde o pergamino) o grandes volúmenes tostados, y necesita trazabilidad exacta
  (finca, cosecha, proceso) para controlar sus curvas de tueste.

**Roles de vendedor** (`RolVendedor`)

- **Caficultor:** dueño de la Finca. Publica su café sin tostar como Presentación (desde su Lote) o
  su propio café tostado, siempre en Perfil Tradicional.
- **Tostador:** tuesta café de los Lotes (Transformación) y publica café tostado con perfiles
  especializados (Claro, Medio, Oscuro).
- **Vendedor de Derivados:** usa café de la plataforma para derivados consumibles (miel, licor,
  cosméticos) y publica Artículos de Merchandising no alimenticios.
- **Formador:** publica cursos virtuales (contenido digital). No publica Presentaciones físicas.

## Cadena de trazabilidad

```
Finca (dueña: Caficultor)
 └── produce → Lote (de una Cosecha, con un Proceso de Beneficio; Verde o Pergamino)
      ├── se publica directo como → Presentación de café sin tostar (tipo CAFE_VERDE)
      └── se tuesta en → Transformación (registra la merma por humedad)
           └── origina → Presentación tostada (con Perfil y Fecha de Tueste)
                         o derivado consumible (licor, miel, cosmético)

Artículo de Merchandising → no tiene Origen (no entra en la cadena)
Ficha de Origen = reconstrucción de la cadena hacia atrás para una Presentación
```

## Glosario — Producción y trazabilidad

### Finca
**Definición:** Terreno de cultivo con nombre, ubicación y altura (msnm), propiedad de un Caficultor.
Punto de partida de la trazabilidad.
**No usar:** Granja, Terreno (a secas).
**Ejemplo:** "Finca La Esperanza", Pitalito, Huila, 1.750 msnm.
```java
Finca finca = Finca.registrar(id, caficultorId, "La Esperanza",
        new Ubicacion("Huila", "Pitalito"), new Altitud(1750));
```

### Cosecha
**Definición:** Año y temporada (Principal o Traviesa) en que se recolectó el café de un Lote.
**Ejemplo:** "Cosecha 2026 - Traviesa".
```java
Cosecha cosecha = new Cosecha(2026, TemporadaDeCosecha.TRAVIESA);
```

### Lote
**Definición:** Cantidad de café recolectada en una misma Finca, Cosecha y Proceso de Beneficio.
Se identifica por su Código de Lote (`LOT-AAAA-NNN`). No se vende como Lote al comprador final.
**Reglas:** solo puede registrarse en estado Verde o Pergamino, nunca Tostado; su cantidad inicial
es mayor que cero; nunca se borra físicamente (solo eliminación lógica).
**Ejemplo:** "LOT-2026-045": 200 kg de pergamino, proceso Lavado.
```java
Lote lote = Lote.registrar(new CodigoDeLote("LOT-2026-045"), new Cantidad(200, "kg"), fincaId,
        ProcesoDeBeneficio.Lavado, EstadoDelCafe.PERGAMINO, cosecha, new VariedadDeCafe("Caturra"));
```

### Proceso de Beneficio
**Definición:** Método para retirar la pulpa y secar el grano después de la cosecha (Lavado, Honey,
Natural). Define buena parte del perfil de sabor.
**Ejemplo:** el Lote LOT-2026-045 usó Lavado: sabor limpio y ácido.
```java
ProcesoDeBeneficio proceso = lote.getProcesoDeBeneficio(); // ProcesoDeBeneficio.Lavado
```

### Transformación
**Definición:** Tueste de uno o varios Lotes. Registra la cantidad de entrada (que se descuenta del
Lote), el Porcentaje de Merma y la cantidad resultante de café tostado.
**Reglas:** no puede usar más café del que tiene el Lote (regla C); la suma de lo publicado desde
ella no puede superar su cantidad resultante.
**Ejemplo:** "Café del Valle" tuesta 50 kg del Lote LOT-2026-045 con 18 % de merma y obtiene 41 kg.
```java
Transformacion tueste = Transformacion.registrar(id, List.of("LOT-2026-045"), new Cantidad(50, "kg"),
        new PorcentajeDeMerma(0.18), tostadorId, LocalDate.now(), PerfilTueste.MEDIO);
```

### Porcentaje de Merma
**Definición:** Peso que pierde el café por humedad al tostarse, entre 12 % y 22 %. La cantidad
resultante es `entrada × (1 − merma)`.
**Ejemplo:** 50 kg con 18 % de merma → 41 kg tostados.
```java
Cantidad resultante = new Cantidad(50, "kg").aplicarMerma(new PorcentajeDeMerma(0.18)); // 41 kg
```

### Perfil de Tueste
**Definición:** Nivel de tueste de un café tostado: Tradicional (el único permitido al Caficultor) o
Claro / Medio / Oscuro (especializados, del Tostador).
**Ejemplo:** Jorge (caficultor) solo ofrece Tueste Tradicional; "Café del Valle" ofrece Medio y Oscuro.
```java
PerfilTueste perfil = PerfilTueste.TRADICIONAL;
```

## Glosario — Comercialización

### Presentación
**Definición:** Unidad final empacada que el Comprador adquiere. Tiene dos variantes: Presentación
Trazable (café o derivado consumible, siempre con Origen) y Artículo de Merchandising (sin Origen).
**Reglas:** su cantidad disponible nunca es negativa; con compras activas no puede darse de baja;
el café tostado no se publica ni se vende pasados 30 días desde su Fecha de Tueste (frescura).
**Ejemplo:** "Café del Valle, Tueste Medio, $28.000", publicada desde la Transformación anterior.
```java
Presentacion presentacion = Presentacion.publicar(id, vendedorId, null, transformacionId, titulo,
        TipoDePresentacion.CAFE_TOSTADO, precio, new Cantidad(10, "kg"), galeria,
        PerfilTueste.MEDIO, RolVendedor.TOSTADOR, new FechaDeTueste(fechaTueste), null, null);
```

### Origen de Presentación
**Definición:** De dónde proviene una Presentación Trazable: **exactamente uno** entre un Lote (café
sin tostar) o una Transformación (tostado y derivados).
**Ejemplo:** el saco de pergamino de 30 kg proviene del Lote LOT-2026-045; la bolsa de Tueste Medio,
de la Transformación de "Café del Valle".
```java
OrigenDePresentacion origen = OrigenDePresentacion.desde(null, transformacionId);
```

### Artículo de Merchandising
**Definición:** Presentación no alimenticia (manillas, tazas, decoración) de un Vendedor de Derivados.
No tiene Origen, Perfil ni Fecha de Tueste, porque su insumo puede no venir de la plataforma (regla B).
Se cuenta en unidades.
**Ejemplo:** "Manilla de semillas de café", 25 unidades.
```java
ArticuloDeMerchandising manilla = publicarArticulo.ejecutar(id, vendedorId, "Manilla de café", precio,
        new Cantidad(25, "unidad"), galeria, "semilla de café e hilo encerado", descripcion,
        RolVendedor.VENDEDOR_DERIVADOS);
```

### Nota de Cata
**Definición:** Descriptores de sabor y aroma que el **vendedor** registra en su Presentación Trazable.
No es una opinión del comprador (eso es una Reseña).
**Ejemplo:** "chocolate, caramelo, ligera acidez cítrica".
```java
presentacionTrazable.registrarNotaCata(new NotaDeCata(List.of("chocolate", "caramelo", "acidez cítrica")));
```

### Ficha de Origen
**Definición:** Reconstrucción de la historia de una Presentación recorriendo la cadena hacia atrás:
Finca → Cosecha → Lote → Transformación. Es de solo lectura, no se persiste.
**Ejemplo:** Finca La Esperanza (Huila, 1.750 msnm) → Cosecha 2026-Traviesa → LOT-2026-045 (Lavado)
→ tostado por Café del Valle (Tueste Medio, 18 % de merma).
```java
FichaDeOrigen ficha = consultarFichaOrigen.ejecutar(transformacionId);
```

### Compra Física
**Definición:** Compra de una o varias Presentaciones. Lleva Dirección de Envío y tramo logístico:
Pendiente → Confirmada → Enviada → Entregada; se cancela sin costo solo antes del envío.
**Ejemplo:** un Comprador pide 2 kg de "Café del Valle, Tueste Medio" con envío a Armenia.
```java
CompraFisica compra = CompraFisica.iniciar(compradorId, new DireccionDeEnvio("Armenia", "Cra 14 #10-20", "Ana"));
compra.agregarDetalle(presentacionId, presentacion.getPrecio(), new Cantidad(2, "kg"));
```

### Compra Digital
**Definición:** Compra de un Curso. No tiene envío; al confirmarse deja una Inscripción. Una vez
confirmada no se cancela: solo se deshace por reembolso del Curso.
**Ejemplo:** un Comprador compra el curso "Latte Art desde cero".
```java
CompraDigital compra = CompraDigital.iniciar(compradorId, cursoId, curso.getPrecio());
```

### Precio Congelado
**Definición:** Precio de la Presentación (o del Curso) copiado en el momento de la compra. No cambia
aunque el vendedor modifique el precio después. Vive en el Detalle de Compra.
**Ejemplo:** se compró a $28.000; si mañana sube a $30.000, la compra sigue en $28.000.
```java
Precio pagado = detalle.precioCongelado();
```

## Glosario — Cursos virtuales

### Curso
**Definición:** Contenido digital que publica un Formador, compuesto por Lecciones. No maneja stock ni
frescura. Solo se publica si tiene al menos una Lección.
**Ejemplo:** "Catación para principiantes", nivel Básico, temática Catación.
```java
Curso curso = Curso.crear(id, formadorId, titulo, descripcion, precio,
        NivelDelCurso.BASICO, TematicaDelCurso.CATACION);
```

### Lección
**Definición:** Unidad de contenido dentro de un Curso, con su Archivo Digital (PDF, MP4, ZIP) y
Duración. Se ubica por su número de orden; insertar una Lección renumera las siguientes.
**Ejemplo:** Lección 1, "Qué es una cata", video de 12 minutos.
```java
curso.insertarLeccion(1, "Qué es una cata", archivoDigital, new Duracion(12));
```

### Inscripción
**Definición:** Acceso de un Comprador a un Curso, originado por una Compra Digital. Registra el primer
acceso; solo se reembolsa dentro de 48 horas y antes de acceder al contenido.
**Ejemplo:** Ana compra el curso y lo puede pedir de vuelta mientras no abra ninguna Lección.
```java
boolean puedePedirReembolso = inscripcion.puedeReembolsarse();
```

## Anti-patrones (términos a evitar)

| No usar | Usar |
| --- | --- |
| "Producto" (genérico) | "Presentación" (o "Curso" si es contenido digital) |
| "Vendedor" cuando el rol importa para la regla | "Caficultor", "Tostador", "Vendedor de Derivados" o "Formador" (`RolVendedor`) |
| "Borrar Lote / Presentación" | "Eliminación lógica" / "dar de baja" |
| "Nota de Cata" para la opinión del comprador | "Reseña" (calificación y comentario del Comprador); la Nota de Cata la escribe el vendedor |
| "Trazabilidad" (como objeto) | "Ficha de Origen" |
| "Producto sin trazabilidad" | "Artículo de Merchandising" |
| "Cancelar un curso" ya confirmado | "Reembolso del Curso" (revoca la Inscripción); cancelar solo aplica a una Compra pendiente o no enviada |
| `presentacion.setLote(lote)` | La Presentación *proviene de* un Lote: se expresa con `OrigenDePresentacion` |
