# Café Trazado — Frontend

Aplicación web desarrollada en **Angular 22** para **Café Trazado**, un marketplace de café de origen colombiano con trazabilidad completa desde la finca hasta la taza.

**Programación Avanzada · Universidad del Quindío · 2026-2**

---

## Integrantes del equipo

* **Santiago Guevara**
* **Santiago Ramírez Bernal**
* **Joseph Cortés Escobar**

---

## Nicho del e-commerce

**Café Trazado** es un marketplace especializado en **café de origen** sin intermediarios ni revendedores de productos genéricos. Los vendedores son quienes cultivan y transforman el café, diferenciándose mediante la **transparencia total** sobre el origen del grano (finca, región, altitud en msnm, cosecha, variedad, proceso de beneficio y perfil de tueste).

### Actores y roles del sistema

El sistema maneja una cuenta única (`Usuario`) diferenciada por su `Rol`:

* **Comprador (`COMPRADOR`):** Adquiere presentaciones de café tostado, verde o derivados, gestiona sus compras, deja reseñas sobre productos entregados y solicita reembolsos dentro de las 48 horas posteriores a la entrega.
* **Caficultor (`CAFICULTOR`):** Propietario de fincas; registra lotes de café verde o pergamino y publica presentaciones de café sin tostar o café tostado en **Perfil Tradicional**.
* **Tostador (`TOSTADOR`):** Registra transformaciones (tostión de lotes controlando la merma por humedad entre 12 % y 22 %) y publica café tostado con perfiles especializados (**Claro**, **Medio**, **Oscuro**).
* **Vendedor de Derivados (`VENDEDOR_DERIVADOS`):** Publica derivados consumibles elaborados con café de la plataforma (miel, licor, cosméticos).

---

## Cómo ejecutar el proyecto

### Requisitos previos

* **Java 21** (JDK 21)
* **Node.js 22.22.3 o superior** (o 24.15+) y **npm**, que es lo que exige Angular 22
* **Docker Desktop** (únicamente si se desea ejecutar el backend y MariaDB mediante contenedores)

---

### 1. Ejecutar el Backend (API REST Spring Boot)

El backend se encuentra en la carpeta `Backend/` del repositorio y expone su API en `http://localhost:8080/api` (documentación Swagger en `http://localhost:8080/swagger-ui.html`).

#### Opción A: Con base de datos en memoria H2 (por defecto, recomendada para pruebas)

Carga automáticamente los datos iniciales de prueba (`data-usuarios.sql`, fincas, lotes, transformaciones, presentaciones y compras).

1. Abrir una terminal en la carpeta `Backend/`:

```powershell
cd Backend
```

2. Ejecutar el servidor Spring Boot:

En Windows (PowerShell):

```powershell
.\gradlew.bat bootRun
```


> El servidor estará listo cuando la consola muestre `Started EcommerceApplication`. Para detenerlo, presionar `Ctrl + C`.

#### Opción B: Con MariaDB y Backend en Docker

1. Abrir una terminal en la carpeta `Backend/`, con **Docker Desktop encendido**, generar el jar y levantar los contenedores (`mariadb-ecommerce` y `ecommerce-app`):

```powershell
cd Backend
.\gradlew bootJar
docker compose up --build -d
```

> Estos comandos levantan tanto la base de datos MariaDB como el servidor backend en `http://localhost:8080`, por lo que **no es necesario** ejecutar `bootRun` adicionalmente. En este modo la base **conserva los datos al apagar**, y la primera vez se cargan los mismos datos de ejemplo que en H2 (usuarios de prueba, productos con fotos y compras). Si cambió el código, repite `bootJar` y `--build`: sin ellos Docker reutiliza la imagen anterior. Las claves del chat con IA y de las fotos se explican en el README de la raíz (archivo `Backend/.env`).

2. Para detener y apagar los contenedores al finalizar (los datos se conservan):

```powershell
docker compose down
```

   Para borrar también los datos y empezar de cero con los de ejemplo: `docker compose down -v`.

---

### 2. Ejecutar el Frontend (Angular 22)

1. Abrir una segunda terminal en la carpeta `frontend/`:

```powershell
cd frontend
```

2. Instalar las dependencias del proyecto:

```powershell
npm install
```



3. Iniciar el servidor de desarrollo:

```powershell
npm start
```

4. Abrir en el navegador: **`http://localhost:4200/`**

> **Chat de asesoría y fotos (opcionales):** para que el asesor con IA responda y para subir fotos, el Backend debe arrancar con las variables de entorno `GROQ_API_KEY` y `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` (ver el README de la raíz). Sin ellas todo lo demás funciona y esas dos funciones avisan que no están disponibles.

---

## Usuarios de prueba

Al iniciar el backend en modo **H2** (Opción A), se cargan automáticamente los siguientes usuarios en la base de datos. **Todos comparten la misma contraseña:** `Cafe2026*`

| Rol | Nombre | Correo electrónico | Contraseña |
| :--- | :--- | :--- | :--- |
| **Caficultor** | Carlos Caficultor | `carlos@finca.com` | `Cafe2026*` |
| **Tostador** | Teresa Tostadora | `teresa@tueste.com` | `Cafe2026*` |
| **Vendedor de Derivados** | Valeria Vendedora | `valeria@derivados.com` | `Cafe2026*` |
| **Comprador** | Ana Gomez | `ana@correo.com` | `Cafe2026*` |
| **Comprador** | Bruno Rios | `bruno@correo.com` | `Cafe2026*` |
| **Comprador** | Carla Mejia | `carla@correo.com` | `Cafe2026*` |
| **Comprador** | Diego Arango | `diego@correo.com` | `Cafe2026*` |

---

## Pantallas de la aplicación y control de acceso por rol

| Pantalla | Ruta | Acceso / Roles permitidos | Descripción funcional                                                                                                                                                              |
| :--- | :--- | :--- |:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Inicio** | `/` | Público (todos) | Página principal de bienvenida y presentación de **Café Trazado**.                                                                                                                 |
| **Catálogo** | `/catalogo` | Público (todos) | Listado paginado de presentaciones disponibles con búsqueda por nombre y filtro por tipo (`CAFE_VERDE`, `CAFE_TOSTADO`, `DERIVADO_CONSUMIBLE`, `MERCHANDISING`).                                     |
| **Detalle del producto** | `/producto/:id` | Público (trazabilidad completa autenticada) | Muestra galería, precio, precio vigente con descuento, nota de cata y la sección **"De dónde viene este café"** con la ficha de origen (`Finca → Cosecha → Lote → Tostión`).       |
| **Registro** | `/registro` | Público / Invitados | Creación de cuenta nueva con selección de rol y validación de mayoría de edad.                                                                                                     |
| **Iniciar sesión** | `/login` | Solo invitados (`guestGuard`) | Autenticación mediante correo y contraseña con emisión de token JWT.                                                                                                               |
| **Comprar presentación** | `/comprar/:id` | Autenticado — `COMPRADOR` (`authGuard`) | Flujo de compra directa de una presentación ingresando cantidad y dirección de envío.                                                                                              |
| **Mis compras** | `/mis-compras` | Autenticado — `COMPRADOR` (`authGuard`) | Historial de compras del comprador con su estado actual (`PENDIENTE`, `CONFIRMADA`, `ENVIADA`, `ENTREGADA`, `CANCELADA`, `REEMBOLSADA`) y opción para cancelar pedidos pendientes. |
| **Mis fotos** | `/mis-fotos` | Autenticado — `CAFICULTOR`, `TOSTADOR`, `VENDEDOR_DERIVADOS` (`rolGuard`) | Subida de fotos a Cloudinary (JPG, PNG o WebP de hasta 2 MB), elección de la foto principal y guardado de la galería de cada presentación propia. |
| **Mis descuentos** | `/descuentos` | Autenticado — `CAFICULTOR`, `TOSTADOR`, `VENDEDOR_DERIVADOS` | Gestión de descuentos por temporada (1 % a 50 % entre fechas) sobre las presentaciones propias del vendedor.                                                                       |
