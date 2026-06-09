# photogear.app – Inventario de Equipo Fotográfico

Sistema web para gestionar inventario fotográfico con backend Java en Apache Tomcat.

---

## Stack tecnológico

| Capa       | Tecnología                                      |
|------------|-------------------------------------------------|
| Backend    | Java 11 · Servlets · JDBC                       |
| Servidor   | Apache Tomcat 10+ (Jakarta EE 9)                |
| BD dev     | H2 (embebida, sin instalación)                  |
| BD prod    | MariaDB                                         |
| Frontend   | Vanilla JavaScript · W3.CSS                     |

> **Tomcat 9**: si usas Tomcat 9, cambia `jakarta.servlet` → `javax.servlet`
> en todos los imports Java y ajusta el namespace de `web.xml` a la versión 4.0.

---

## Requisitos previos

- Java 11+
- Maven 3.6+
- Apache Tomcat 10.x
- (Producción) MariaDB 10.6+

---

## 1. Configurar base de datos

### H2 (desarrollo — sin configuración extra)
La base de datos H2 se crea automáticamente en `~/photogear_db` al arrancar.
No necesitas instalar nada.

### MariaDB (producción)
```sql
CREATE DATABASE photogear CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'photogear_user'@'localhost' IDENTIFIED BY 'tu_contraseña_segura';
GRANT ALL PRIVILEGES ON photogear.* TO 'photogear_user'@'localhost';
FLUSH PRIVILEGES;
```

Edita `src/main/resources/config.properties`:
```properties
db.profile=mariadb
mariadb.username=photogear_user
mariadb.password=tu_contraseña_segura
```

---

## 2. Compilar

```bash
cd photogear
mvn clean package
```

Genera: `target/photogear.war`

---

## 3. Desplegar en Tomcat

### Opción A – Copiar WAR manualmente
```bash
cp target/photogear.war $CATALINA_HOME/webapps/
```

### Opción B – Tomcat Manager
Sube `photogear.war` desde `http://localhost:8080/manager`.

---

## 4. Cambiar perfil de BD en producción

Agrega al archivo `$CATALINA_HOME/bin/setenv.sh`:
```bash
JAVA_OPTS="$JAVA_OPTS -Ddb.profile=mariadb"
```

En Windows (`setenv.bat`):
```bat
set JAVA_OPTS=%JAVA_OPTS% -Ddb.profile=mariadb
```

---

## 5. Acceder a la aplicación

```
http://localhost:8080/photogear/
```

---

## API REST

Base URL: `http://localhost:8080/photogear/api`

| Método | Endpoint                         | Descripción                  |
|--------|----------------------------------|------------------------------|
| GET    | `/equipment`                     | Listar todo (con filtros)    |
| GET    | `/equipment/{id}`                | Obtener uno                  |
| POST   | `/equipment`                     | Crear equipo                 |
| PUT    | `/equipment/{id}`                | Actualizar equipo            |
| DELETE | `/equipment/{id}`                | Eliminar equipo              |
| POST   | `/equipment/{id}/report`         | Reportar perdido/robado      |

### Filtros disponibles en GET /equipment
```
?category=lens
?status=active
?search=canon
```

### Cuerpo de reporte (POST /equipment/{id}/report)
```json
{
  "status":     "stolen",
  "reportDate": "2024-09-10",
  "details":    "Sustraído en Xochimilco. Denuncia FGJ folio #2024-XC-001."
}
```

---

## Estructura del proyecto

```
photogear/
├── pom.xml
└── src/main/
    ├── java/app/photogear/
    │   ├── dao/
    │   │   ├── DatabaseManager.java    ← Conexión H2/MariaDB
    │   │   ├── EquipmentDAO.java       ← CRUD con PreparedStatements
    │   │   └── UserDAO.java            ← Persistencia de usuarios
    │   ├── filter/
    │   │   ├── CorsFilter.java         ← CORS para desarrollo
    │   │   └── AuthFilter.java         ← Validación de JWT
    │   ├── listener/
    │   │   └── AppContextListener.java ← Inicializa BD al arrancar
    │   ├── model/
    │   │   ├── Equipment.java          ← Modelo de datos
    │   │   └── User.java               ← Usuario autenticado
    │   ├── servlet/
    │   │   ├── EquipmentServlet.java   ← API REST de equipo
    │   │   └── AuthServlet.java        ← Login con Google + JWT
    │   └── util/
    │       ├── GsonConfig.java         ← Gson con adaptadores java.time
    │       ├── AppConfig.java          ← Lectura de config.properties
    │       └── JwtUtil.java            ← Emisión/validación de JWT
    ├── resources/
    │   ├── config.properties           ← Configuración de BD
    │   └── schema.sql                  ← DDL (ejecutado al arrancar)
    └── webapp/
        ├── WEB-INF/web.xml
        ├── index.html
        ├── css/photogear.css
        └── js/
            ├── api.js                  ← Servicio API + fallback localStorage
            └── app.js                  ← Lógica de la aplicación
```

---

## Notas de producción

- **Contraseñas**: nunca incluyas contraseñas de producción en `config.properties`
  dentro del WAR. Usa variables de entorno o JNDI DataSource.

- **CORS**: configura el origen real antes de desplegar:
  ```bash
  # setenv.sh de Tomcat
  JAVA_OPTS="$JAVA_OPTS -Dcors.allowed.origins=https://tuapp.com"
  ```
  O edita `cors.allowed.origins` en `config.properties`.

- **Pool de conexiones**: incluido vía HikariCP. Ajusta los parámetros
  `hikari.*` en `config.properties` según la carga esperada.

---

## Seguridad

La aplicación está endurecida para operar detrás de un WAF (mod_security /
OWASP CRS) y cumplir buenas prácticas OWASP.

### Cabeceras de seguridad (`SecurityHeadersFilter`)
Se aplican a toda respuesta:

| Cabecera | Valor |
|----------|-------|
| `Content-Security-Policy` | `default-src 'self'` + orígenes de Google/W3.CSS/Fonts |
| `X-Content-Type-Options` | `nosniff` |
| `X-Frame-Options` | `DENY` (+ `frame-ancestors 'none'`) |
| `Referrer-Policy` | `no-referrer` |
| `Permissions-Policy` | cámara/micrófono/geo deshabilitados |
| `Cross-Origin-Opener-Policy` | `same-origin-allow-popups` |
| `Strict-Transport-Security` | `max-age=31536000` (solo sobre HTTPS) |
| `Cache-Control: no-store` | en respuestas `/api/*` |

> La CSP usa `'unsafe-inline'` en `script-src`/`style-src` porque el frontend
> emplea manejadores en línea (`onclick`). Para una CSP estricta con nonces
> habría que refactorizar esos manejadores.

### Gestión de secretos
`AppConfig` resuelve cada clave en este orden: **propiedad JVM → variable de
entorno → `config.properties`**. Inyecta los secretos por entorno (no en el WAR):

```bash
export JWT_SECRET="$(openssl rand -base64 48)"
export MARIADB_PASSWORD="…"
export CORS_ALLOWED_ORIGINS="https://tuapp.com"
```

La app **no arranca** si `jwt.secret` es el valor de ejemplo o mide < 32 caracteres.

### CORS
Lista blanca de orígenes (no se refleja un `Origin` arbitrario). `*` queda
reservado para desarrollo y es incompatible con credenciales.

### Validación y manejo de errores
- Entradas validadas contra listas blancas (categoría, estado, condición) y
  límites de longitud; toda la persistencia usa `PreparedStatement`.
- Los errores 5xx devuelven un mensaje genérico; el detalle solo va al log
  (sin fuga de SQL, rutas ni versiones).
- Páginas de error propias evitan que el contenedor exponga stack traces.

### Recomendaciones de despliegue (fuera de la app)
- Terminar TLS en Apache/WAF y propagar `X-Forwarded-Proto: https`.
- En Tomcat: `server.xml` con `server=" "`, `xpoweredBy="false"`,
  `allowTrace="false"`, y deshabilitar el listado de directorios.
- Mantener mod_security con OWASP CRS en modo bloqueo.
