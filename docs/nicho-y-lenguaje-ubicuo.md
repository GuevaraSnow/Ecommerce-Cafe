# Nicho y Lenguaje Ubicuo — Café Trazado

Programación Avanzada · Universidad del Quindío · 2026-2
Grupo: Santiago Guevara · Santiago Ramírez Bernal · Joseph Cortés

> **Versión 3 del glosario.** Actualiza el glosario para que coincida con el código de la Entrega 2.
> Cambios respecto de la versión 2: se **eliminaron los cursos virtuales** (Curso, Lección, Inscripción,
> Compra Digital y el rol Formador) por estar fuera del alcance; **Comprador y Vendedor se unificaron en un
> solo Usuario con Rol**; la **Compra es única** (carrito, confirmación, envío y entrega); el código del
> **Lote lo genera el servidor**; y los ejemplos usan los métodos reales del dominio.

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

**Usuario y Rol** (`Usuario`, `Rol`): hay un solo tipo de cuenta, el Usuario, y su Rol dice qué puede hacer.
El **Comprador** compra, reseña y pide reembolsos; los otros tres roles venden:

- **Caficultor:** dueño de la Finca. Publica su café sin tostar como Presentación (desde su Lote) o
  su propio café tostado, siempre en Perfil Tradicional.
- **Tostador:** tuesta café de los Lotes (Transformación) y publica café tostado con perfiles
  especializados (Claro, Medio, Oscuro).
- **Vendedor de Derivados:** usa café de la plataforma para derivados consumibles (miel, licor,
  cosméticos) y publica Artículos de Merchandising no alimenticios.

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
Se identifica por su Código de Lote (`LOT-AAAA-NNN`), que genera el servidor: el año de registro y el
siguiente número de ese año. No se vende como Lote al comprador final.
**Reglas:** solo puede registrarse en estado Verde o Pergamino, nunca Tostado; su cantidad inicial
es mayor que cero; su Cosecha es del año de registro o del anterior; nunca se borra físicamente (solo
eliminación lógica, que hace el Caficultor dueño de su Finca).
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
**Definición:** Tueste de un Lote. Registra la cantidad de entrada (que se descuenta del Lote), el
Porcentaje de Merma y la cantidad resultante de café tostado.
**Reglas:** no puede usar más café del que tiene el Lote (regla C); la suma de lo publicado desde
ella no puede superar su cantidad resultante.
**Ejemplo:** "Café del Valle" tuesta 50 kg del Lote LOT-2026-045 con 18 % de merma y obtiene 41 kg.
```java
Transformacion tueste = Transformacion.registrar(id, "LOT-2026-045", new Cantidad(50, "kg"),
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
        PerfilTueste.MEDIO, Rol.TOSTADOR, new FechaDeTueste(fechaTueste), null, null);
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
        Rol.VENDEDOR_DERIVADOS);
```

### Nota de Cata
**Definición:** Descriptores de sabor y aroma que el **vendedor** registra en su Presentación Trazable.
No es una opinión del comprador (eso es una Reseña).
**Ejemplo:** "chocolate, caramelo, ligera acidez cítrica".
```java
presentacionTrazable.registrarNotaCata(new NotaDeCata(List.of("chocolate", "caramelo", "acidez cítrica")));
```

### Reseña
**Definición:** Opinión del **Comprador** sobre una Presentación que adquirió (Compra entregada):
una Calificación de 1 a 5 y un texto de máximo 500 caracteres. Cada Comprador reseña una
Presentación una sola vez, y solo el vendedor dueño de la Presentación la responde, también una vez.
**Ejemplo:** Ana recibió su café y lo califica con 5: "Notas a panela y buen cuerpo"; el Tostador le responde.
```java
Resena resena = Resena.dejar(id, compradorId, presentacionId, presentacion.getVendedorId(),
        new Calificacion(5), new TextoDeResena("Notas a panela y buen cuerpo"), LocalDateTime.now());
resena.responder(vendedorId, new TextoDeResena("Gracias por tu compra"), LocalDateTime.now());
```

### Reembolso de café físico
**Definición:** Devolución del dinero de **una Presentación** de una Compra entregada, pedida por quien
compró dentro de las 48 horas siguientes a la entrega. Pedir el de todo el carrito crea una Solicitud de
Reembolso por cada Presentación, y cada vendedor aprueba o rechaza solo las suyas. La Compra queda
**Reembolsada** solo cuando se aprueban todas sus Presentaciones; con un reembolso parcial sigue
**Entregada**. El stock no se devuelve.
**Ejemplo:** Ana recibe 4 cafés y uno llega húmedo: pide el reembolso de ese, el Tostador lo aprueba y la
Compra sigue Entregada.
```java
SolicitudReembolso solicitud = servicioDeReembolso.solicitar(id, compra, compradorId, presentacion,
        "Llegó húmedo", LocalDateTime.now());
servicioDeReembolso.aprobar(solicitud, compra, vendedorId, LocalDateTime.now());
```

### Ficha de Origen
**Definición:** Reconstrucción de la historia de una Presentación recorriendo la cadena hacia atrás:
Finca → Cosecha → Lote → Transformación. Es de solo lectura, no se persiste.
**Ejemplo:** Finca La Esperanza (Huila, 1.750 msnm) → Cosecha 2026-Traviesa → LOT-2026-045 (Lavado)
→ tostado por Café del Valle (Tueste Medio, 18 % de merma).
```java
FichaDeOrigen ficha = consultarFichaOrigen.ejecutar(transformacionId);
```

### Compra
**Definición:** Pedido de una o varias Presentaciones. Sigue el tramo Pendiente → Confirmada →
Enviada → Entregada; se cancela sin costo solo antes del envío. La Dirección de Envío se fija al
confirmarla y la fecha y hora de entrega se registran al entregarla.
**Ejemplo:** un Comprador pide 2 kg de "Café del Valle, Tueste Medio" con envío a Armenia.
```java
compra.confirmar(new DireccionDeEnvio("Armenia", "Cra 14 #10-20", "Ana"));
```

### Carrito
**Definición:** La Compra mientras está Pendiente. Se le agregan, cambian o quitan Presentaciones;
cada Presentación ocupa una sola línea (si se agrega otra vez se suma la cantidad) y su precio
vigente queda congelado al agregarla.
**Ejemplo:** Ana agrega 2 kg de café y luego 1 kg más del mismo: queda una línea de 3 kg al precio del primero.
```java
Compra carrito = Compra.abrirCarrito(compraId, compradorId);
carrito.agregarPresentacion(presentacionId, presentacion.precioVigente(hoy), new Cantidad(2, "kg"));
```

### Precio Congelado
**Definición:** Precio de la Presentación copiado en el momento de la compra. No cambia
aunque el vendedor modifique el precio después. Vive en el Detalle de Compra.
**Ejemplo:** se compró a $28.000; si mañana sube a $30.000, la compra sigue en $28.000.
```java
Precio pagado = detalle.precioCongelado();
```

## Anti-patrones (términos a evitar)

| No usar | Usar |
| --- | --- |
| "Producto" (genérico) | "Presentación" |
| "Vendedor" o "Comprador" como si fueran cuentas distintas | "Usuario" con un `Rol`; y cuando el rol importa para la regla, "Caficultor", "Tostador", "Vendedor de Derivados" o "Comprador" |
| "Borrar Lote / Presentación" | "Eliminación lógica" / "dar de baja" |
| "Nota de Cata" para la opinión del comprador | "Reseña" (calificación y comentario del Comprador); la Nota de Cata la escribe el vendedor |
| "Trazabilidad" (como objeto) | "Ficha de Origen" |
| "Producto sin trazabilidad" | "Artículo de Merchandising" |
| "Cancelar" una Compra ya enviada | Solo se cancela una Compra pendiente o confirmada que no se haya enviado; después aplica el "Reembolso" |
| `presentacion.setLote(lote)` | La Presentación *proviene de* un Lote: se expresa con `OrigenDePresentacion` |
