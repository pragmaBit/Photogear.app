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
