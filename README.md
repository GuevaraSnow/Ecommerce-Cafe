# Café Trazado

Marketplace de café de origen con trazabilidad, desarrollado para Programación Avanzada (Universidad del Quindío). El backend está en `Backend/` (Spring Boot, JPA con H2, Swagger) y la documentación del dominio en `docs/`.

## Cómo correr las pruebas

Desde la carpeta `Backend`:

```powershell
.\gradlew test
```

Todas las pruebas pasan con ese comando. Con `--rerun-tasks` se fuerza a que se ejecuten de nuevo aunque no haya cambios.

## Pruebas avanzadas

Todas siguen el patrón AAA (preparar, ejecutar, verificar) y están en `Backend/src/test/java`.

| Punto | Qué comprueba | Dónde está |
|---|---|---|
| 0.1 Repositorio | Guardar y recuperar una Presentación con su Value Object (precio, cantidad, galería, descuento) contra H2 | `PresentacionRepositorioJpaTest` |
| 0.2 Repositorio | Paginación y orden con los datos de `data-presentacion.sql`: 4 disponibles, páginas de 3, ordenadas por título | `PresentacionRepositorioJpaTest` |
| 1 Seguridad | Sin token 401, rol u otro vendedor 403, dueño con el rol correcto 200 | `SeguridadPresentacionTest` |
| 2 `@DataJpaTest` | La misma capa de persistencia con una prueba por capa | `PresentacionDataJpaTest` |
| 3 Integración | Controlador, caso de uso, repositorio y base de datos sin simular nada | `PresentacionIntegracionTest` |
| 4 Controller | 404 y regla de negocio rota, comprobando con `verify(..., never())` que `guardar` no se llama | `PresentacionControllerNoGuardaTest` |

### 1. Probar un endpoint con seguridad

Una prueba de seguridad comprueba que cada ruta protegida deje pasar solo a quien debe. El 401 significa que la API no sabe quién hace la petición (no hay token o no es válido) y el 403 que sí lo sabe pero no tiene permiso (rol equivocado, o intenta tocar algo de otro vendedor). Usamos `spring-security-test` y su `jwt()`, que le pone a la petición un usuario ya autenticado con el id y el rol que queramos, sin pasar por el login. En `SeguridadPresentacionTest` probamos: sin token (401), un comprador que intenta publicar (403), otro vendedor que intenta cambiar el precio o dar de baja una presentación ajena (403, y se comprueba que no cambió nada) y el dueño que sí puede (200). También usamos tokens reales emitidos por `JwtService`, y comprobamos que uno vencido o firmado con otra clave da 401. Estas pruebas usan `@SpringBootTest` con `@AutoConfigureMockMvc` porque un `@WebMvcTest` no carga nuestro `SecurityConfig`; por eso en los `@WebMvcTest` de los demás controladores pusimos `addFilters = false`. Lo que más costó fue que `jwt()` no convierte nuestro claim `rol` en una autoridad: hay que pasarla con `.authorities(...)`, y al principio todo respondía 403.

### 2. `@DataJpaTest` contra `@SpringBootTest`

`@DataJpaTest` levanta solo la capa de persistencia (las entidades y los repositorios de Spring Data) sobre una base de datos en memoria y deshace cada prueba al terminar; `@SpringBootTest` levanta la aplicación completa. Por eso `@DataJpaTest` arranca más rápido, pero no carga nuestro adaptador (`PresentacionRepositorioJpa`) ni el mapper, y hay que traerlos con `@Import`. En Spring Boot 4 la anotación cambió de paquete a `org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest` y necesita el starter de pruebas `spring-boot-starter-data-jpa-test`, que ya estaba en nuestro `build.gradle`. Lo que costó: por defecto `@DataJpaTest` reemplaza nuestra base de datos por una H2 embebida propia, y con ella el script `data-presentacion.sql` falló con "Check constraint invalid". Agregando `@AutoConfigureTestDatabase(replace = NONE)` usa la H2 de `application.properties` y la prueba pasa; no investigamos más a fondo la causa. En `PresentacionDataJpaTest` comprobamos que los datos de ejemplo cargan y que guardar y recuperar funciona.

### 3. Prueba de integración

Una prueba de integración recorre varias piezas reales funcionando juntas. En `PresentacionIntegracionTest` la petición HTTP entra por el controlador, pasa por el caso de uso, el mapper y el adaptador JPA y termina en H2, sin ningún `@MockitoBean`: por ejemplo, publicamos un artículo, lo consultamos, lo pausamos y comprobamos que sale del catálogo. Se diferencia de una prueba unitaria en que la unitaria simula todo lo que toca y prueba una sola clase, así que es rápida y señala exactamente dónde falló, pero no demuestra que las piezas funcionen juntas; la de integración sí lo demuestra, a cambio de ser más lenta y de depender de los datos de `data-presentacion.sql` (si alguien los cambia, la prueba se rompe; por eso las fechas del SQL son relativas a hoy). Conviene tener pocas, solo para los flujos importantes, y dejar los casos de borde a las unitarias. Cada prueba usa `@Transactional` para deshacer lo que guarda.
