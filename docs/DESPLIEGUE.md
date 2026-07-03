# Guía de Despliegue - SpaceWork

**Sistema:** Sistema de Gestión de Reservas de Espacios  
**Fecha:** Julio 2026  
**Entorno:** Local (sin dominio)  

---

## 1. Requisitos Previos

### Software Requerido

- **JDK 8 o superior** (Java Development Kit)
  - Verificar instalación: `java -version`
  - Descargar: https://www.oracle.com/java/technologies/downloads/

- **Maven 3.6 o superior**
  - Verificar instalación: `mvn -version`
  - Descargar: https://maven.apache.org/download.cgi

- **Oracle Database XE 11g o superior**
  - Descargar: https://www.oracle.com/database/technologies/express-edition.html
  - Usuario/Contraseña por defecto: spacework/spacework

### Hardware Mínimo

- **CPU:** 2 núcleos
- **RAM:** 4 GB
- **Disco:** 10 GB libres

---

## 2. Configuración de Base de Datos

### 2.1 Instalar Oracle XE

1. Descargar Oracle Database XE desde el sitio oficial
2. Ejecutar el instalador y seguir los pasos
3. Configurar puerto: 1521
4. Configurar SID: XE
5. Establecer contraseña para usuario SYSTEM

### 2.2 Crear Usuario de Base de Datos

```sql
-- Conectar como SYSTEM
sqlplus system/password

-- Crear usuario
CREATE USER spacework IDENTIFIED BY spacework;

-- Otorgar privilegios
GRANT CONNECT, RESOURCE TO spacework;
GRANT CREATE SESSION TO spacework;
GRANT CREATE TABLE TO spacework;
GRANT CREATE SEQUENCE TO spacework;
GRANT CREATE TRIGGER TO spacework;

-- Confirmar
ALTER USER spacework DEFAULT TABLESPACE USERS;
ALTER USER spacework QUOTA UNLIMITED ON USERS;

COMMIT;
```

### 2.3 Ejecutar Script de Inicialización

```bash
# Desde la raíz del proyecto
sqlplus spacework/spacework @sql/spacework_database.sql
```

### 2.4 Verificar Conexión

```bash
# Probar conexión
sqlplus spacework/spacework@//localhost:1521/XE

# Debería mostrar: Connected to Oracle Database...
```

---

## 3. Configuración del Proyecto

### 3.1 Configurar Credenciales de Base de Datos

Editar archivo: `src/main/java/com/spacework/util/Conexion.java`

```java
private static final String URL  = "jdbc:oracle:thin:@localhost:1521:XE";
private static final String USER = "spacework";
private static final String PASS = "spacework";
```

**Nota:** Si usas diferentes credenciales, actualiza estos valores.

### 3.2 Configurar Puerto de la Aplicación

Editar archivo: `src/main/resources/application.properties`

```properties
server.port=8080
```

Si el puerto 8080 está ocupado, cambia a otro puerto (ej: 8081).

### 3.3 Configurar Correo SMTP (Opcional)

Editar archivo: `src/main/resources/mail.properties`

```properties
mail.smtp.host=smtp.gmail.com
mail.smtp.port=587
mail.smtp.auth=true
mail.smtp.starttls.enable=true
mail.username=tu_correo@gmail.com
mail.password=tu_contraseña_de_aplicación
```

**Nota:** Para Gmail, necesitas generar una contraseña de aplicación.

---

## 4. Compilación y Empaquetado con Maven

### 4.1 Limpiar Proyecto Anterior

```bash
cd sistema-de-reservas
mvn clean
```

### 4.2 Compilar el Proyecto

```bash
mvn compile
```

### 4.3 Empaquetar el Proyecto

```bash
mvn package -DskipTests
```

Esto generará el archivo JAR en: `target/SistemaReservas-1.0-SNAPSHOT.jar`

### 4.4 Empaquetar con Todas las Dependencias

```bash
mvn assembly:single
```

Esto generará: `target/SistemaReservas-1.0-SNAPSHOT-jar-with-dependencies.jar`

---

## 5. Ejecución de la Aplicación

### 5.1 Opción A: Ejecutar con Maven (Desarrollo)

```bash
mvn spring-boot:run
```

**Ventajas:**
- Recarga automática con spring-boot-devtools
- Fácil para desarrollo
- Logs en consola

### 5.2 Opción B: Ejecutar JAR Empaquetado (Producción)

```bash
java -jar target/SistemaReservas-1.0-SNAPSHOT.jar
```

**Ventajas:**
- Más rápido
- No requiere Maven instalado
- Ideal para producción

### 5.3 Opción C: Ejecutar con start.bat (Windows)

Doble clic en: `start.bat`

**Contenido de start.bat:**
```bat
@echo off
mvn spring-boot:run
pause
```

---

## 6. Verificación del Despliegue

### 6.1 Verificar que la Aplicación Inició

**Indicadores de éxito:**
- Mensaje en consola: `Started SpaceWorkApplication in X seconds`
- Tomcat iniciado en puerto configurado
- Sin errores en los logs

### 6.2 Probar Endpoints de Monitoreo

```bash
# Estado de salud
curl http://localhost:8080/actuator/health

# Debe responder: {"status":"UP"}

# Métricas disponibles
curl http://localhost:8080/actuator/metrics

# Memoria JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

### 6.3 Probar API REST

```bash
# Listar espacios
curl http://localhost:8080/api/espacios

# Login de cliente
curl -X POST http://localhost:8080/api/auth/login/cliente \
  -H "Content-Type: application/json" \
  -d '{"emailODni":"12345678","password":"Test1234"}'
```

### 6.4 Abrir Dashboard de Monitoreo

Navegar a: `http://localhost:8080/monitoring.html`

Deberías ver:
- Estado de la aplicación (Activo)
- Estado de la base de datos (Activo)
- Métricas en tiempo real
- Gráficas de memoria y CPU

---

## 7. Ejecución de Pruebas

### 7.1 Ejecutar Todas las Pruebas

```bash
mvn test
```

### 7.2 Ejecutar Solo Pruebas de Software

```bash
mvn test -Dtest=PruebasSoftwareCompletas
```

### 7.3 Ejecutar Solo Pruebas de Seguridad

```bash
mvn test -Dtest=PruebasSeguridadCompletas
```

### 7.4 Ejecutar Pruebas con VS Code

1. Abrir el archivo de test en VS Code
2. Click derecho en el nombre de la clase
3. Seleccionar "Run Test" o "Debug Test"
4. Ver resultados en la consola de testing

---

## 8. Solución de Problemas Comunes

### 8.1 Puerto 8080 Ocupado

**Error:** `Port 8080 is already in use`

**Solución:**
```bash
# Windows: Encontrar proceso
netstat -ano | findstr :8080

# Matar proceso
taskkill /F /PID <PID>

# O cambiar puerto en application.properties
server.port=8081
```

### 8.2 Oracle XE No Inicia

**Error:** `Connection refused` o `ORA-12541`

**Solución:**
```bash
# Windows: Iniciar servicio Oracle
services.msc
# Buscar "OracleXE...TNSListener" y dar click derecho → Start

# Linux:
sudo systemctl start oracle-xe
```

### 8.3 Error de Conexión a Base de Datos

**Error:** `ORA-01017: invalid username/password`

**Solución:**
1. Verificar credenciales en `Conexion.java`
2. Verificar que el usuario spacework existe
3. Verificar que Oracle XE está corriendo
4. Probar conexión manual: `sqlplus spacework/spacework@//localhost:1521/XE`

### 8.4 Error de Compilación Maven

**Error:** `Could not resolve dependencies`

**Solución:**
```bash
# Limpiar caché de Maven
mvn clean

# Forzar descarga de dependencias
mvn dependency:purge-local-repository

# Reintentar compilación
mvn compile
```

### 8.5 Tests Fallan por Base de Datos

**Error:** Tests fallan con SQLException

**Solución:**
1. Verificar que Oracle XE está iniciado
2. Verificar que el script de inicialización se ejecutó
3. Verificar credenciales en `Conexion.java`
4. Algunos tests requieren datos de prueba en la BD

---

## 9. Configuración para Producción

### 9.1 Crear application-prod.properties

```properties
server.port=8080
spring.application.name=SistemaReservas

# Logging nivel producción
logging.level.root=INFO
logging.level.com.spacework=INFO

# Actuator
management.endpoints.web.exposure.include=health,metrics
management.endpoint.health.show-details=when-authorized
```

### 9.2 Ejecutar en Modo Producción

```bash
java -jar -Dspring.profiles.active=prod target/SistemaReservas-1.0-SNAPSHOT.jar
```

### 9.3 Configurar Firewall

Asegurar que el puerto 8080 esté abierto:
```bash
# Windows Firewall
netsh advfirewall firewall add rule name="SpaceWork" dir=in action=allow protocol=TCP localport=8080
```

---

## 10. Monitoreo Post-Despliegue

### 10.1 Verificar Logs

```bash
# Ver log principal
tail -f logs/spacework.log

# Ver log de errores
tail -f logs/spacework.error.log

# Ver log de seguridad
tail -f logs/spacework.security.log
```

### 10.2 Verificar Métricas

Acceder a: `http://localhost:8080/actuator/health`

Verificar:
- Status: UP
- Componentes: diskSpace, ping, db

### 10.3 Verificar Dashboard

Acceder a: `http://localhost:8080/monitoring.html`

Verificar:
- Todos los indicadores en verde
- Métricas actualizándose
- Sin alertas críticas

---

## 11. Resumen de Comandos

```bash
# Compilar y ejecutar (desarrollo)
mvn clean spring-boot:run

# Empaquetar
mvn clean package -DskipTests

# Ejecutar JAR
java -jar target/SistemaReservas-1.0-SNAPSHOT.jar

# Ejecutar pruebas
mvn test

# Ejecutar pruebas específicas
mvn test -Dtest=PruebasSoftwareCompletas
mvn test -Dtest=PruebasSeguridadCompletas

# Verificar salud
curl http://localhost:8080/actuator/health

# Ver logs
tail -f logs/spacework.log
```

---

## 12. Checklist de Despliegue

- [ ] JDK 8+ instalado
- [ ] Maven 3.6+ instalado
- [ ] Oracle XE instalado e iniciado
- [ ] Usuario spacework creado en Oracle
- [ ] Script de inicialización ejecutado
- [ ] Credenciales configuradas en Conexion.java
- [ ] Puerto configurado en application.properties
- [ ] Proyecto compilado con `mvn compile`
- [ ] Proyecto empaquetado con `mvn package`
- [ ] Aplicación iniciada con `mvn spring-boot:run`
- [ ] Endpoint /actuator/health responde UP
- [ ] Dashboard de monitoreo accesible
- [ ] Pruebas ejecutadas exitosamente
- [ ] Logs generados correctamente

---

**Última actualización:** Julio 2026  
**Versión:** 1.0  
**Entorno:** Local (localhost:8080)
