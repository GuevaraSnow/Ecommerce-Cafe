# Café Trazado — Marketplace de café de origen

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk&logoColor=white"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Gradle-wrapper-02303A?style=flat-square&logo=gradle&logoColor=white"/>
  <img src="https://img.shields.io/badge/JPA-H2-1E88E5?style=flat-square"/>
  <img src="https://img.shields.io/badge/Seguridad-JWT_HS256-black?style=flat-square&logo=jsonwebtokens&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?style=flat-square&logo=swagger&logoColor=black"/>
  <img src="https://img.shields.io/badge/Pruebas-875-brightgreen?style=flat-square"/>
</p>

<p align="center">
  Backend de un marketplace de café con trazabilidad completa — Spring Boot, JPA con H2, Spring Security con JWT y una arquitectura por capas con el dominio puro en el centro.
</p>

---

## Descripción

**Café Trazado** vende café de origen y le muestra al comprador de dónde viene: cada Presentación que se compra se
puede seguir hacia atrás por la cadena **Finca → Lote → Transformación → Presentación → Ficha de Origen**. Los
vendedores son quienes cultivan y transforman el café (Caficultor, Tostador y Vendedor de Derivados), y su
diferenciador es la transparencia total sobre tierra, cosecha, proceso y tueste. El comprador final arma un carrito,
compra, pide reembolsos y reseña lo que recibió.

```
Finca ─► Lote ─► Transformación ─► Presentación ─► Compra ─► Reseña / Reembolso
                                       │
                                       └─► Ficha de Origen (la cadena reconstruida hacia atrás)
```

---

## Funcionalidades

- **Trazabilidad** — Fincas, Lotes (con código `LOT-AAAA-NNN` generado por el servidor), Transformaciones con merma por humedad y la Ficha de Origen de cada café.
- **Catálogo** — búsqueda con filtros (título, tipo, vendedor, rango de precio y calificación mínima), paginación, búsqueda predictiva y ranking de las Presentaciones más vendidas.
- **Gestión de Presentaciones** — publicar café verde, tostado y derivados, con descuento por temporada, nota de cata, galería, reposición de stock y baja lógica.
- **Compras** — carrito que congela el precio vigente, confirmación que descuenta el stock (todo o nada), envío, entrega y cancelación.
- **Reembolsos y reseñas** — reembolso por Presentación dentro de las 48 horas, y reseñas que solo deja quien recibió el café.
- **Usuarios y seguridad** — registro, inicio de sesión con JWT, recuperación de contraseña por código, roles y propiedad de cada recurso.
- **Errores uniformes** — toda respuesta de error trae `{ timestamp, status, mensaje }`: 400 datos mal formados, 401 sin token, 403 sin permiso, 404 no existe, 422 regla de negocio violada.

---

## Tecnologías

- **Java 21** y **Spring Boot 4.1.1** (Gradle con wrapper)
- **Spring Data JPA** con **H2** en memoria, las tablas las crea Hibernate y los datos de ejemplo se cargan al arrancar (`data-*.sql`)
- **Spring Security** con **JWT** (HS256, 60 minutos): el token lleva el id del usuario, su correo, su rol y su vencimiento
- **SpringDoc / Swagger UI** para documentar y probar la API desde el navegador
- **Lombok**, **Bean Validation**
- **JUnit 5**, **Mockito**, `spring-security-test` y **Postman** para las pruebas

---

## Arquitectura

El dominio no conoce a Spring, a JPA ni a Lombok; todo apunta hacia él.

```
Backend/src/main/java/com/uniquindio/ecommerce/
├── Domain/                     # El negocio, Java puro
│   ├── entity/                 # Lote, Transformacion, Presentacion, Compra, Usuario, Finca, Resena...
│   ├── valueobject/            # Cantidad, Precio, CodigoDeLote, Cosecha, Email, Rol...
│   ├── service/                # Domain Services: reglas que cruzan agregados
│   ├── repository/             # Puertos (interfaces) de los repositorios
│   └── exception/              # ReglaDominioException, CredencialesInvalidasException
├── application/
│   ├── usecase/                # Un caso de uso por operación: coordinan, sin reglas de negocio
│   └── dto/                    # Request y Response: solo validan la forma
└── infrastructure/
    ├── rest/                   # Controladores delgados, mappers y manejador global de errores
    ├── persistence/            # Adaptadores JPA (y repositorios en memoria, solo para pruebas)
    ├── security/               # JWT, SecurityConfig y reglas de propiedad
    └── config/                 # Registro de los Domain Services como beans
```

- Las **reglas de una entidad** viven en la entidad; las que **cruzan agregados** van en un **Domain Service**
  (`ServicioDePublicacion`, `ServicioDeCompra`, `ServicioDeReembolso`, `ServicioDeBajaDePresentacion`,
  `ServicioDeReposicion`, `ServicioDeTransformacion`, `ServicioDeLote`, `ServicioDeAutenticacion`,
  `ServicioDeContrasena`, `VerificadorDeCompra` y `VerificadorDeEmail`).
- Los **casos de uso** no llevan `if` de negocio: cargan, delegan y guardan.
- La documentación del dominio (casos de uso, reglas de negocio, clasificación de entidades y Value Objects,
  diagramas de agregado, DTO y lenguaje ubicuo) está en [`docs/`](./docs).

---

## Cómo ejecutarlo

**Requisitos:** Java 21.

```bash
cd Backend
./gradlew bootRun        # en Windows (PowerShell): .\gradlew bootRun
```

| Qué | Dónde |
|-----|-------|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| Consola de H2 | `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:ecommercedb`, usuario `sa`, sin contraseña |
| Colección de Postman | `Backend/postman/CafeTrazado.postman_collection.json` |

- **Clave del JWT:** por defecto se usa una clave de desarrollo; para cambiarla, define la variable de entorno `JWT_SECRET` antes de arrancar.
- **Probar con Swagger:** haz `POST /api/usuarios/login` con un usuario de la tabla de abajo, copia el `token` de la respuesta, pulsa **Authorize** y pégalo (sin la palabra `Bearer`).
- **Postman:** importa la colección y córrela en orden con el Runner (`baseUrl` = `http://localhost:8080`); cada corrida registra usuarios nuevos, así que se puede repetir.
- **Pruebas:** `./gradlew test` (ver [Pruebas](#pruebas)).

---

## Usuarios de prueba

Se cargan al arrancar. Todos usan la contraseña **`Cafe2026*`**.

| Correo | Contraseña | Rol |
|--------|------------|-----|
| `carlos@finca.com` | `Cafe2026*` | CAFICULTOR (dueño de la Finca Villa Laura) |
| `teresa@tueste.com` | `Cafe2026*` | TOSTADOR |
| `valeria@derivados.com` | `Cafe2026*` | VENDEDOR_DERIVADOS |
| `ana@correo.com` | `Cafe2026*` | COMPRADOR |
| `bruno@correo.com` | `Cafe2026*` | COMPRADOR |
| `carla@correo.com` | `Cafe2026*` | COMPRADOR |
| `diego@correo.com` | `Cafe2026*` | COMPRADOR |

Los datos de ejemplo ya traen una Finca, tres Lotes, tres Transformaciones, ocho Presentaciones y siete Compras en todos
los estados, para recorrer la trazabilidad de punta a punta. Por ejemplo, la Ficha de Origen de la Transformación
`trans-001` va desde la Finca Villa Laura hasta el café tostado que publica Teresa.

---

## Endpoints principales

**Acceso:** *Público* no pide token; *Autenticado* pide un token de cualquier rol; *Vendedor* es un usuario con rol
`CAFICULTOR`, `TOSTADOR` o `VENDEDOR_DERIVADOS`. Todo lo que no es público exige `Authorization: Bearer <token>`.

<details>
<summary><strong>Usuarios</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `POST` | `/api/usuarios/registro` | Público | `RegistrarUsuario` |
| `POST` | `/api/usuarios/login` | Público | `IniciarSesion` |
| `POST` | `/api/usuarios/recuperar-contrasena` | Público | `RecuperarContrasena` |
| `POST` | `/api/usuarios/restablecer-contrasena` | Público | `RestablecerContrasena` |
| `PUT` | `/api/usuarios/perfil` | Autenticado | `EditarPerfil` |
| `PUT` | `/api/usuarios/perfil/contrasena` | Autenticado | `CambiarContrasena` |

</details>

<details>
<summary><strong>Fincas, Lotes y Transformaciones (trazabilidad)</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `POST` | `/api/fincas` | `CAFICULTOR` | `RegistrarFinca` |
| `GET` | `/api/fincas/{id}` | Autenticado | `ConsultarFinca` |
| `POST` | `/api/lotes` | `CAFICULTOR` | `RegistrarLote` |
| `GET` | `/api/lotes/{codigo}` | Autenticado | `ConsultarLote` |
| `DELETE` | `/api/lotes/{codigo}` | `CAFICULTOR` dueño de la Finca del Lote | `EliminarLote` |
| `POST` | `/api/transformaciones` | `TOSTADOR` | `RegistrarTransformacion` |
| `GET` | `/api/transformaciones/{id}/ficha-origen` | Autenticado | `ConsultarFichaOrigen` |

</details>

<details>
<summary><strong>Catálogo de Presentaciones (consultas públicas)</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `GET` | `/api/presentaciones` | Público | `BuscarPresentaciones` |
| `GET` | `/api/presentaciones/{id}` | Público | `ConsultarPresentacion` |
| `GET` | `/api/presentaciones/sugerencias` | Público | `SugerirPresentaciones` |
| `GET` | `/api/presentaciones/mas-vendidas` | Público | `ConsultarMasVendidas` |
| `GET` | `/api/resenas?presentacionId=` | Público | `ConsultarResenasDePresentacion` |

</details>

<details>
<summary><strong>Gestión de Presentaciones</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `POST` | `/api/presentaciones` | Vendedor | `PublicarPresentacion` |
| `POST` | `/api/presentaciones/merchandising` | Vendedor (el dominio exige `VENDEDOR_DERIVADOS`) | `PublicarArticuloMerchandising` |
| `PUT` | `/api/presentaciones/{id}/precio` | Vendedor dueño | `CambiarPrecioDePresentacion` |
| `POST` | `/api/presentaciones/{id}/reposiciones` | Vendedor dueño | `ReponerStockDePresentacion` |
| `POST` | `/api/presentaciones/{id}/retiros` | Vendedor dueño | `RetirarStockDePresentacion` |
| `POST` | `/api/presentaciones/{id}/pausa` | Vendedor dueño | `PausarPresentacion` |
| `POST` | `/api/presentaciones/{id}/reactivacion` | Vendedor dueño | `ReactivarPresentacion` |
| `PUT` | `/api/presentaciones/{id}/galeria` | Vendedor dueño | `ReemplazarGaleriaDePresentacion` |
| `PUT` | `/api/presentaciones/{id}/nota-de-cata` | Vendedor dueño | `RegistrarNotaDeCata` |
| `PUT` | `/api/presentaciones/{id}/descuento` | Vendedor dueño | `ProgramarDescuentoDePresentacion` |
| `DELETE` | `/api/presentaciones/{id}/descuento` | Vendedor dueño | `QuitarDescuentoDePresentacion` |
| `DELETE` | `/api/presentaciones/{id}` | Vendedor dueño | `EliminarPresentacion` |

</details>

<details>
<summary><strong>Compras</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `POST` | `/api/compras` | `COMPRADOR` | `CrearCarrito` |
| `POST` | `/api/compras/{id}/presentaciones` | `COMPRADOR` dueño de la Compra | `AgregarAlCarrito` |
| `PUT` | `/api/compras/{id}/confirmar` | `COMPRADOR` dueño de la Compra | `ConfirmarCompra` |
| `PUT` | `/api/compras/{id}/cancelar` | `COMPRADOR` dueño de la Compra | `CancelarCompra` |
| `PUT` | `/api/compras/{id}/enviar` | Vendedor de algo de la Compra | `MarcarEnviada` |
| `PUT` | `/api/compras/{id}/entregar` | Vendedor de algo de la Compra | `MarcarEntregada` |
| `GET` | `/api/compras` | `COMPRADOR` | `ConsultarHistorialDeCompras` |
| `GET` | `/api/compras/{id}` | Comprador dueño o Vendedor de la Compra | `ConsultarCompra` |

</details>

<details>
<summary><strong>Reembolsos y reseñas</strong></summary>

| Método | Ruta | Acceso | Caso de uso |
|--------|------|--------|-------------|
| `POST` | `/api/reembolsos` | `COMPRADOR` | `SolicitarReembolso` |
| `PUT` | `/api/reembolsos/{id}/aprobar` | Vendedor (dueño de la Presentación) | `AprobarReembolso` |
| `PUT` | `/api/reembolsos/{id}/rechazar` | Vendedor (dueño de la Presentación) | `RechazarReembolso` |
| `POST` | `/api/resenas` | `COMPRADOR` | `DejarResena` |
| `PUT` | `/api/resenas/{id}/respuesta` | Vendedor (dueño de la Presentación) | `ResponderResena` |

</details>

---

## Pruebas

```bash
cd Backend
./gradlew test
```

La suite tiene **875 pruebas** y todas pasan. Siguen el patrón AAA (preparar, ejecutar, verificar) y están en
`Backend/src/test/java`; cuando una regla se rompe, la prueba también comprueba que el estado **no cambió** y que
no se guardó nada.

### Cobertura por capa

| Capa | Mínimo pedido | Pruebas | Dónde están |
|------|---------------|---------|-------------|
| **Dominio** | 8 | **325** | Entidades (`LoteTest`, `CompraEstadoTest`, `PresentacionTest`...), Value Objects (`CosechaTest`, `CantidadTest`...) y Domain Services (`ServicioDeLoteTest`, `ServicioDeReembolsoTest`...) |
| **Casos de uso** | 4 | **126** | `application/usecase`, con Mockito o con repositorios en memoria: `RegistrarLoteTest`, `ConfirmarCompraTest`, `EliminarPresentacionTest`, `IniciarSesionTest`... |
| **Controladores** | 1 | **134** | `@WebMvcTest` de cada controlador: códigos 201, 200, 400, 401, 403, 404 y 422, y `verify(..., never())` para comprobar que el caso de uso no se llama si la petición es inválida |
| **Repositorios** | 1 | **89** | `@DataJpaTest` y pruebas contra H2 (`LoteDataJpaTest`, `MasVendidasDataJpaTest`, `PresentacionRepositorioJpaTest`...), repositorios en memoria y los datos de ejemplo |
| Seguridad y errores HTTP | — | 82 | 401, 403 y propiedad de cada recurso, el formato uniforme de los errores y el token JWT |
| Mappers | — | 63 | Conversión entre capas (REST ⇄ dominio y dominio ⇄ entidades JPA) |
| DTO | — | 38 | Validación de cada Request |
| Integración, configuración y E2E | — | 18 | Flujo completo por HTTP, y que Spring arma bien los beans |
| **Total** | | **875** | |

### Pruebas avanzadas

| Punto | Qué comprueba | Dónde está |
|---|---|---|
| 0.1 Repositorio | Guardar y recuperar una Presentación con su Value Object (precio, cantidad, galería, descuento) contra H2 | `PresentacionRepositorioJpaTest` |
| 0.2 Repositorio | Paginación y orden con los datos de `data-presentacion.sql`: 4 disponibles, páginas de 3, ordenadas por título | `PresentacionRepositorioJpaTest` |
| 1 Seguridad | Sin token 401, rol u otro vendedor 403, dueño con el rol correcto 200 | `SeguridadPresentacionTest` |
| 2 `@DataJpaTest` | La misma capa de persistencia con una prueba por capa | `PresentacionDataJpaTest` |
| 3 Integración | Controlador, caso de uso, repositorio y base de datos sin simular nada | `PresentacionIntegracionTest` |
| 3 Flujo completo | El recorrido de una compra de punta a punta, solo por HTTP y con los tokens del login | `FlujoCompletoE2ETest` |
| 4 Controller | 404 y regla de negocio rota, comprobando con `verify(..., never())` que `guardar` no se llama | `PresentacionControllerNoGuardaTest` |

#### 1. Probar un endpoint con seguridad

Una prueba de seguridad comprueba que cada ruta protegida deje pasar solo a quien debe. El 401 significa que la API no sabe quién hace la petición (no hay token o no es válido) y el 403 que sí lo sabe pero no tiene permiso (rol equivocado, o intenta tocar algo de otro vendedor). Usamos `spring-security-test` y su `jwt()`, que le pone a la petición un usuario ya autenticado con el id y el rol que queramos, sin pasar por el login. En `SeguridadPresentacionTest` probamos: sin token (401), un comprador que intenta publicar (403), otro vendedor que intenta cambiar el precio o dar de baja una presentación ajena (403, y se comprueba que no cambió nada) y el dueño que sí puede (200). También usamos tokens reales emitidos por `JwtService`, y comprobamos que uno vencido o firmado con otra clave da 401. Estas pruebas usan `@SpringBootTest` con `@AutoConfigureMockMvc` porque un `@WebMvcTest` solo carga nuestro `SecurityConfig` si se lo importamos: los `@WebMvcTest` de las rutas protegidas lo importan y usan `springSecurity()`, y los de las consultas públicas siguen con `addFilters = false`. Lo que más costó fue que `jwt()` no convierte nuestro claim `rol` en una autoridad: hay que pasarla con `.authorities(...)`, y al principio todo respondía 403.

#### 2. `@DataJpaTest` contra `@SpringBootTest`

`@DataJpaTest` levanta solo la capa de persistencia (las entidades y los repositorios de Spring Data) sobre una base de datos en memoria y deshace cada prueba al terminar; `@SpringBootTest` levanta la aplicación completa. Por eso `@DataJpaTest` arranca más rápido, pero no carga nuestro adaptador (`PresentacionRepositorioJpa`) ni el mapper, y hay que traerlos con `@Import`. En Spring Boot 4 la anotación cambió de paquete a `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` y necesita el starter de pruebas `spring-boot-starter-data-jpa-test`, que ya estaba en nuestro `build.gradle`. Lo que costó: por defecto `@DataJpaTest` reemplaza nuestra base de datos por una H2 embebida propia, y con ella el script `data-presentacion.sql` falló con "Check constraint invalid". Agregando `@AutoConfigureTestDatabase(replace = NONE)` usa la H2 de `application.properties` y la prueba pasa; no investigamos más a fondo la causa. En `PresentacionDataJpaTest` comprobamos que los datos de ejemplo cargan y que guardar y recuperar funciona.

#### 3. Prueba de integración

Una prueba de integración recorre varias piezas reales funcionando juntas. En `PresentacionIntegracionTest` la petición HTTP entra por el controlador, pasa por el caso de uso, el mapper y el adaptador JPA y termina en H2, sin ningún `@MockitoBean`: por ejemplo, publicamos un artículo, lo consultamos, lo pausamos y comprobamos que sale del catálogo. Se diferencia de una prueba unitaria en que la unitaria simula todo lo que toca y prueba una sola clase, así que es rápida y señala exactamente dónde falló, pero no demuestra que las piezas funcionen juntas; la de integración sí lo demuestra, a cambio de ser más lenta y de depender de los datos de `data-presentacion.sql` (si alguien los cambia, la prueba se rompe; por eso las fechas del SQL son relativas a hoy). Conviene tener pocas, solo para los flujos importantes, y dejar los casos de borde a las unitarias. Cada prueba usa `@Transactional` para deshacer lo que guarda.

---

## Correcciones de la Entrega 1

| Observación | Qué cambiamos | Dónde se ve |
|-------------|---------------|-------------|
| **Proyecto demasiado grande** | Se eliminó el módulo de cursos (Curso, Lección, Inscripción y Compra Digital). Comprador y Vendedor se unificaron en un solo **Usuario con Rol**, y la Compra Física quedó como una **Compra única** | Commits `5fd0eb3` (PR #45 y #46) y `15b9b88` (PR #51); el Usuario con Rol, en los PR #55, #61 y #65. En el código: `Usuario`, `Rol` y `Compra`; ya no existe nada de cursos |
| **Casos de uso con lógica; usar Domain Services para orquestar** | La lógica que cruzaba agregados pasó a Domain Services (`Domain/service`, sin anotaciones, registrados con `@Bean` en `infrastructure/config`) y los casos de uso quedaron sin `if` de negocio | `ServicioDePublicacion` (`7f9928c`, PR #44); `ServicioDeCompra` (`9fe2be4` y `a414e7f`, PR #53); y además `ServicioDeReembolso`, `ServicioDeBajaDePresentacion`, `ServicioDeReposicion`, `VerificadorDeCompra`, `ServicioDeLote`, `ServicioDeTransformacion` y `ServicioDeAutenticacion` |
| **Congelar el precio en el carrito** | La Compra pendiente es el carrito y cada `DetalleDeCompra` guarda el precio vigente al agregar la Presentación | `2fd05c7` (PR #51) y `af8172d` (PR #53); `Compra.agregarPresentacion()` y `DetalleDeCompra` |
| **Descuentos** | Se agregó el Value Object `Descuento` y `Presentacion.precioVigente(hoy)`; el carrito congela el precio **con** el descuento si rige ese día | `dcb4781` (PR #48); `Descuento`, `Precio.aplicarDescuento()` y `ServicioDeCompra.agregarAlCarrito()` |

---

## Autores

| Nombre | GitHub |
|--------|--------|
| Santiago Guevara | https://github.com/GuevaraSnow |
| Santiago Ramírez Bernal | https://github.com/SantiagoRB17 |
| Joseph Cortés | https://github.com/Escobar7C |

---

<p align="center">
  <sub>Programación Avanzada — Universidad del Quindío · 2026-2</sub>
</p>
