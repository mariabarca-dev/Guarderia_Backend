# GuarderiaCentral – Backend (API REST)

Trabajo práctico de **Programación 2** – Tecnicatura Universitaria en Informática Aplicada, UTN-INSPT.
**Grupo A – Tema 9.** Profesor: Miguel Silva.

**Integrantes:** Franco Buyatti, Daniela Forclaz, Héctor Machaca y María Eugenia Barca.

Este repositorio es el **backend** del sistema de la cochera "Guardería Central". Expone una API REST con la lógica de negocio, la persistencia en MySQL y la seguridad por rol. La interfaz web está en el repositorio del **frontend** (`Guarderia_Frontend`), que consume esta API.

## Tecnologías

| Componente | Versión / detalle |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 (Maven, empaquetado WAR) |
| Persistencia | Spring Data JPA + Hibernate, MySQL |
| Seguridad | Spring Security con sesión HTTP (cookie `GUARDERIA_BACK`) |
| Validación | Bean Validation (`jakarta.validation`) |
| Utilidades | Lombok |
| Puerto | `8080` |

## Arquitectura

```
Navegador → Frontend (Spring MVC + JSP, :8081) → Backend (REST/JSON, :8080) → MySQL
```

El backend se organiza en capas dentro del paquete `com.guarderiaCentral.guarderia_Backend`:

| Paquete | Contenido |
|---|---|
| `modelos` | Entidades JPA y enumeraciones (`Rol`, `TipoVehiculo`). |
| `repositories` | Un subpaquete por entidad: repositorio, `Request`, `Update` y `Response` (los DTO y el mapeo viven junto al repositorio). |
| `services` / `services.impl` | Interfaces y reglas de negocio. |
| `restcontrollers` | Controladores REST (`/api/...`). |
| `security` | `SecurityConfig` (autorización por rol), login y manejo de la sesión. |
| `exceptions` | Excepciones de negocio y `GlobalExceptionHandler`, que las traduce a respuestas HTTP. |
| `config` | Constantes de negocio y creación del SYSADMIN inicial. |

### Modelo de datos

Herencia **JOINED**: `Usuario` (abstracta) → `Socio`, `Empleado`, `Administrador`. El **SYSADMIN** es una fila de `Administrador` con `Rol.SYSADMIN`.

Entidades de negocio: `Zona`, `Garage` (cochera), `Vehiculo` (rodado), `PropiedadGarage` (compra de una cochera por un socio) y `AsignacionVehiculoGarage` (ocupación de una cochera por un rodado). `AsignacionEmpleadoZona` representa el **sector** de un empleado en una zona.

**Borrado lógico:** nada se elimina físicamente; cada entidad tiene el campo `activo` y se filtra con `@SQLRestriction("activo = true")`. Los historiales de bajas usan consultas nativas.

## Roles y permisos

Toda la autorización está **centralizada en `security/SecurityConfig`** (rutas por rol; gana la primera regla que coincide).

| Rol | Qué puede hacer |
|---|---|
| `SOCIO` | Consultar solo lo propio: perfil, rodados, cocheras compradas y cocheras ocupadas. |
| `EMPLEADO` | Consultar solo lo propio: legajo, sectores y rodados a su cargo. |
| `ADMINISTRADOR` | Gestionar zonas, cocheras, rodados, compras, sectores y ocupaciones, y las cuentas de socios, empleados y administradores. No ve las cuentas SYSADMIN. |
| `SYSADMIN` | Ver todas las cuentas del sistema y gestionar las cuentas de socios, empleados y administradores. |

La contraseña se guarda cifrada (`PasswordEncoder`).

## Endpoints principales

| Ruta base | Recurso |
|---|---|
| `/api/auth` | Login (`POST /api/auth/login`) y logout |
| `/api/usuarios` | Cuenta propia (`/me`), socios, empleados, administradores y consulta general |
| `/api/zonas` | Zonas |
| `/api/garages` | Cocheras |
| `/api/vehiculos` | Rodados |
| `/api/propiedades-garage` | Compras de cocheras |
| `/api/asignaciones-vehiculo-garage` | Ocupaciones |
| `/api/asignaciones-empleado-zona` | Sectores |

Los errores de negocio se devuelven como JSON (`ErrorResponse`) con el código HTTP correspondiente (400, 403, 404, 409).

## Requisitos previos

- JDK 21
- MySQL 8 en ejecución
- IntelliJ IDEA (Maven integrado) o Maven 3.9+ instalado

## Configuración

### 1. Base de datos

Creá la base vacía:

```sql
CREATE DATABASE guarderia_db;
```

Las tablas las crea Hibernate al iniciar (`spring.jpa.hibernate.ddl-auto=update`).

### 2. Conexión (`src/main/resources/application.properties`)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/guarderia_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
```

Si la base está en otra PC, cambiá `localhost` por su IP (y usá un usuario con permiso de acceso remoto).

### 3. Credenciales (`entorno/config.properties`)

Las claves **no se suben al repositorio**: la carpeta `entorno/` está en `.gitignore`. Antes de arrancar, creala en la raíz del proyecto (al lado de `pom.xml`) con este contenido:

```properties
spring.datasource.password=<clave de MySQL>
app.cors.allowed-origin=http://localhost:8081
app.sysadmin.username=<usuario del SYSADMIN inicial>
app.sysadmin.password=<clave del SYSADMIN inicial>
```

- `app.cors.allowed-origin`: origen del frontend. Si el frontend corre en otra PC, poné su dirección (por ejemplo `http://192.168.0.10:8081`).
- Al iniciar, si el SYSADMIN no existe, se crea con esas credenciales.

## Cómo ejecutarlo

**Desde IntelliJ IDEA**

1. `File > Open` y elegí la carpeta del proyecto (la que contiene `pom.xml`).
2. Esperá a que Maven descargue las dependencias y confirmá el SDK 21 en `File > Project Structure`.
3. Abrí `GuarderiaBackendApplication` y ejecutala con el botón verde.

**Desde la terminal**

```bash
mvn spring-boot:run
```

La API queda en `http://localhost:8080`. **Levantá primero el backend y después el frontend.**

## Orden para cargar datos

Al iniciar, el sistema crea solo el usuario SYSADMIN. El resto de los datos se carga desde la web, y conviene respetar este orden porque cada paso necesita lo anterior:

1. **Zona:** letra, tipo de rodado, capacidad y medidas de las cocheras.
2. **Cocheras:** cada una pertenece a una zona.
3. **Socio:** cuenta del socio.
4. **Rodado:** se registra a nombre de un socio.
5. **Compra:** un socio compra una cochera.
6. **Ocupación:** un rodado ocupa una cochera.
7. **Empleado:** cuenta del empleado.
8. **Sector:** se asigna un empleado a una zona (la zona debe existir).

Un registro no se puede dar de baja mientras otro activo dependa de él. Por ejemplo, una zona con cocheras o sectores activos, un socio con rodados o compras, o una cochera ocupada o comprada. Para dar de baja, se hace en el orden inverso.

# Estructura del proyecto

```
Guarderia_Backend/
├── entorno/                  ← credenciales (NO se sube a Git)
│   └── config.properties
├── src/main/java/.../guarderia_Backend/
│   ├── config/ exceptions/ modelos/ repositories/
│   ├── restcontrollers/ security/ services/
├── src/main/resources/application.properties
└── pom.xml
```
