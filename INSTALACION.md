# 🚀 Guía de Instalación — Sistema de Gestión de Reservas

> **SpaceWork Perú S.A.C.** — Universidad Tecnológica del Perú  
> Proyecto: Sistema de Gestión de Reservas de Espacios  
> Lenguaje: Java SE + Oracle Database

---

## 📋 Requisitos Previos

- Windows 10 o superior
- Acceso a internet para descargas
- 8 GB RAM mínimo
- 5 GB espacio en disco

---

## 🔧 Paso 1: Instalar Herramientas Base

### 1.1 Instalar con Chocolatey (Recomendado)

Abre **PowerShell como Administrador** y ejecuta:

```powershell
choco install jdk8 -y
choco install maven -y
```

**Espera a que termine completamente.**

### 1.2 Verificar Instalación

En PowerShell, ejecuta:

```powershell
java -version
javac -version
mvn -version
```

Deberías ver las versiones instaladas (ejemplo: Java 8.0.xxx, Maven 3.9.x).

---

## 📦 Paso 2: Descargar Oracle Database XE

### 2.1 Descarga Manual

1. Ve a: https://www.oracle.com/database/technologies/xe-downloads.html
2. Selecciona **Windows (64-bit)**
3. Descarga el archivo `.zip`
4. Extrae el archivo en una carpeta accesible (ej: `C:\OracleXE\`)

### 2.2 Instalar Oracle Database

1. Ejecuta el archivo `setup.exe` desde la carpeta extraída
2. Sigue el instalador:
   - **Puerto**: 1521 (por defecto)
   - **SYSTEM Password**: escribe una contraseña fuerte y **anótala**
   - **Confirm Password**: repite la contraseña
   - Continúa con siguiente → siguiente → instalar

3. **Espera a que termine** (puede tardar 5-10 minutos)

### 2.3 Crear Usuario `spacework`

Abre **Oracle SQL Developer** (viene con Oracle XE) o **SQL*Plus**:

```sql
-- Conectate como SYSTEM (usa la contraseña que estableciste)
CREATE USER spacework IDENTIFIED BY spacework123;
GRANT CONNECT, RESOURCE, DBA TO spacework;
COMMIT;
```

---

## 📥 Paso 3: Descargar Driver JDBC (ojdbc8.jar)

### Opción A: Desde Maven Central (Automático)

Si estás usando Maven, el driver se descarga automáticamente. Solo agrega al `pom.xml`:

```xml
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>21.9.0.0</version>
</dependency>
```

### Opción B: Descarga Manual (RECOMENDADO — Sin Maven necesario)

1. Ve a: https://www.oracle.com/database/technologies/jdbc-ucp-21c-downloads.html
2. Descarga `ojdbc8.jar` 
3. Guárdalo en: `tu_proyecto/lib/ojdbc8.jar`

> **NOTA:** Este proyecto NO usa Maven. Los JARs se cargan directamente desde `/lib/`

```powershell
# Crear carpeta lib si no existe
mkdir lib -Force

# Descargar javax.mail.jar (JavaMail 1.6.2)
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.2/javax.mail-1.6.2.jar" -OutFile "lib\javax.mail.jar"

# Descargar activation.jar (requerido por JavaMail)
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/javax/activation/activation/1.1.1/activation-1.1.1.jar" -OutFile "lib\activation.jar"
```

Verifica que se descargaron:
```powershell
Get-ChildItem lib\*.jar | Select-Object Name, Length
# javax.mail.jar   ~659 KB
# activation.jar   ~69 KB
```

### Configurar mail.properties

Crea el archivo `src/main/resources/mail.properties`:

```properties
smtp.host=smtp.gmail.com
smtp.port=587
smtp.user=TU_CORREO@gmail.com
smtp.password=xxxx xxxx xxxx xxxx
smtp.from=TU_CORREO@gmail.com
smtp.auth=true
smtp.starttls.enable=true
```

> **Importante:** Usa una **Contraseña de Aplicación** de Google (no tu contraseña normal).
> Ve a: Cuenta de Google → Seguridad → Verificación en 2 pasos → Contraseñas de Aplicación

---

## 💻 Paso 4: Configurar Entorno Java sin Maven

### 4.1 Descargar VS Code (Opcional)

1. Ve a: https://code.visualstudio.com/
2. Descarga la versión **Windows Installer**
3. Ejecuta el instalador

### 4.2 Verificar Compilador Java

En PowerShell:
```powershell
javac -version    # Debe mostrar Java 8 o superior
java -version
```

---

## 📁 Paso 5: Descargar el Proyecto Completo

El proyecto está en tu carpeta `SistemaReservas/`. Contiene:
- `src/` — código fuente Java
- `lib/` — ojdbc8.jar, javax.mail.jar, activation.jar
- `sql/` — scripts de base de datos
- `target/classes/` — archivos compilados

---

## ⚙️ Paso 6: Compilar el Proyecto

Abre **PowerShell** en la carpeta del proyecto:

```powershell
cd "D:\UTP-2026\MARZO\Curso Integrador_Sistemas Sotfware\SistemaReservas"

# Compilar todos los archivos Java
$cp = "lib/ojdbc8.jar;lib/javax.mail.jar;lib/activation.jar"
javac -encoding UTF-8 -cp "target/classes;$cp" -d target/classes `
  (Get-ChildItem -Recurse -Path "src/main/java" -Filter "*.java" | Select-Object -ExpandProperty FullName)

write-Host "Compilación completada"
```
---

## 🗄️ Paso 7: Ejecutar Scripts de Base de Datos

Abre **SQL Developer** o **SQL*Plus** conectado como `spacework`:

### 7.1 Ejecutar scripts en orden:

```powershell
# Desde PowerShell, conectarse a Oracle y ejecutar scripts:
# sqlplus spacework/spacework123@localhost:1521:XE @sql/01_crear_tablas.sql
# sqlplus spacework/spacework123@localhost:1521:XE @sql/02_crear_secuencias.sql
# sqlplus spacework/spacework123@localhost:1521:XE @sql/03_crear_triggers.sql
# sqlplus spacework/spacework123@localhost:1521:XE @sql/04_datos_iniciales.sql
```

O copiar y ejecutar manualmente en SQL Developer los scripts incluidos en `/sql/`:
1. `01_crear_tablas.sql`
2. `02_crear_secuencias.sql`
3. `03_crear_triggers.sql`
4. `04_datos_iniciales.sql`

---

## 🚀 Paso 8: Ejecutar el Servidor

En PowerShell (en la carpeta del proyecto):

```powershell
java -cp "target/classes;lib/ojdbc8.jar;lib/javax.mail.jar;lib/activation.jar" com.spacework.Main
```

**Verás:**
```
╔═══════════════════════════════════════════════════╗
║     SPACEWORK - Sistema de Gestión de Reservas    ║
║           Servidor WEB - SPA (HTML5)              ║
║                                                   ║
║  🚀 Frontend: HTML5 + CSS3 + JavaScript            ║
║  📡 Backend: Java SE 8 + HttpServer               ║
║  💾 Base de Datos: Oracle 11g XE                  ║
║  ❌ Desktop UI: DESHABILITADO (Web Only)          ║
╚═══════════════════════════════════════════════════╝

✅ Servidor WEB iniciado exitosamente
🌐 URL: http://localhost:8080
🔒 Login: admin / admin123 (usuario predeterminado)

⏹️  Presiona Ctrl+C para detener
```

---

## 🌐 Acceder a la Aplicación

1. Abre tu navegador: **http://localhost:8080**
2. **Usuario:** `admin`
3. **Contraseña:** `admin123`

---

## 📧 Configurar Gmail para Envío de Emails

El sistema envía automáticamente 2 correos al procesar un pago:
1. ✅ Confirmación de pago
2. ⭐ Solicitud de evaluación con estrellas

### Configurar mail.properties

Edita `src/main/resources/mail.properties`:

```properties
smtp.host=smtp.gmail.com
smtp.port=587
smtp.user=TU_CORREO@gmail.com
smtp.password=xxxx xxxx xxxx xxxx
smtp.from=TU_CORREO@gmail.com
smtp.auth=true
smtp.starttls.enable=true
```

> **Importante:** Usa una **Contraseña de Aplicación** de Google (no tu contraseña normal).  
> Ve a: Cuenta de Google → Seguridad → Verificación en 2 pasos → Contraseñas de Aplicación

Luego recompila:
```powershell
$cp = "lib/ojdbc8.jar;lib/javax.mail.jar;lib/activation.jar"
javac -encoding UTF-8 -cp "target/classes;$cp" -d target/classes `
  (Get-ChildItem -Recurse -Path "src/main/java" -Filter "*.java" | Select-Object -ExpandProperty FullName)
```

---

## ✅ Verificar Instalación

### 6.3 Crear Secuencias

```sql
CREATE SEQUENCE SEQ_USUARIOS          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_ESPACIOS          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_CLIENTES          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_RESERVAS          START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_AUDITORIA         START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_TOKENS_EVALUACION START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;

COMMIT;
```

### 6.4 Insertar Usuario Administrador Inicial

```sql
INSERT INTO USUARIOS (id_usuario, username, password_hash, nombre, email, rol, estado)
VALUES (SEQ_USUARIOS.NEXTVAL, 'admin', 
        'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
        'Administrador', 'admin@spacework.pe', 'ADMINISTRADOR', 'ACTIVO');

COMMIT;
```

> **Contraseña inicial**: `admin` (ya hasheada con SHA-256)

---

## 📊 Paso 6B: Scripts SQL Adicionales (Ampliaciones del Sistema)

Después de ejecutar los scripts básicos (01 al 04), necesitas ejecutar estos scripts adicionales en el mismo usuario `spacework`:

### 6B.1 Script: ROLES y Actualización de USUARIOS

Abre **SQL Developer** o **SQL*Plus** y ejecuta:

```sql
-- Crear tabla de ROLES
CREATE TABLE ROLES (
    id_rol NUMBER PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL UNIQUE,
    descripcion VARCHAR2(255),
    fecha_creacion DATE DEFAULT SYSDATE
);

CREATE SEQUENCE SEQ_ROLES START WITH 1 INCREMENT BY 1 NOCACHE;

-- Insertar tipos de rol
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (1, 'ADMIN', 'Administrador con acceso total');
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (2, 'GERENTE', 'Gerente con acceso a reportes y cambios de estado');
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (3, 'CLIENTE', 'Cliente con acceso limitado a sus reservas');
COMMIT;

-- Agregar columna ROL a tabla USUARIOS
ALTER TABLE USUARIOS ADD (rol VARCHAR2(20) DEFAULT 'CLIENTE');

-- Actualizar usuario admin como ADMIN
UPDATE USUARIOS SET rol = 'ADMIN' WHERE nombre_usuario = 'admin';
COMMIT;
```

### 6B.2 Script: HORARIOS_BLOQUEADOS

Continúa en el mismo script SQL anterior:

```sql
-- Crear tabla HORARIOS_BLOQUEADOS (para bloquear franjas horarias)
CREATE TABLE HORARIOS_BLOQUEADOS (
    id_bloqueo NUMBER PRIMARY KEY,
    id_espacio NUMBER NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    razon VARCHAR2(255),
    fecha_creacion DATE DEFAULT SYSDATE,
    usuario_creador VARCHAR2(100),
    FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio)
);

CREATE SEQUENCE SEQ_BLOQUEOS START WITH 1 INCREMENT BY 1 NOCACHE;
COMMIT;
```

### 6B.3 Verificación

Ejecuta estas queries para verificar que todo se creó correctamente:

```sql
SELECT COUNT(*) FROM ROLES;           -- Debe retornar 3
SELECT COUNT(*) FROM HORARIOS_BLOQUEADOS;  -- Puede retornar 0 (tabla vacía)
SELECT rol FROM USUARIOS WHERE nombre_usuario = 'admin';  -- Debe retornar 'ADMIN'
```

---

## ⚙️ Paso 7: Configurar Conexión a Base de Datos en el Proyecto

### 7.1 Crear `Conexion.java`

En tu proyecto, crea el archivo `src/main/java/com/spacework/util/Conexion.java`:

```java
package com.spacework.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "spacework";
    private static final String PASSWORD = "spacework123";

    public static Connection getConexion() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### 7.2 Editar `pom.xml`

Agrega dependencies:

```xml
<dependencies>
    <!-- Oracle JDBC Driver (system scope = JAR local) -->
    <dependency>
        <groupId>com.oracle.database.jdbc</groupId>
        <artifactId>ojdbc8</artifactId>
        <version>21.9.0.0</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/ojdbc8.jar</systemPath>
    </dependency>

    <!-- JavaMail API para envío de correos de evaluación -->
    <dependency>
        <groupId>com.sun.mail</groupId>
        <artifactId>javax.mail</artifactId>
        <version>1.6.2</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/javax.mail.jar</systemPath>
    </dependency>

    <!-- Activation requerido por JavaMail -->
    <dependency>
        <groupId>javax.activation</groupId>
        <artifactId>activation</artifactId>
        <version>1.1.1</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/activation.jar</systemPath>
    </dependency>

    <!-- JUnit para pruebas -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Ejecuta en PowerShell:
```powershell
mvn compile -o -q
# Para ejecutar:
java -cp "target\classes;lib\*" com.spacework.Main
```

---

## ✅ Verificación Final

### Checklist:

- [ ] JDK 8+ instalado (`java -version` funciona)
- [ ] Maven instalado (`mvn -version` funciona)
- [ ] Oracle Database XE corriendo
- [ ] Usuario `spacework` creado en Oracle
- [ ] Tablas creadas en la BD (incluye TOKENS_EVALUACION)
- [ ] Secuencias creadas (incluye SEQ_TOKENS_EVALUACION)
- [ ] ojdbc8.jar en `lib/` (driver Oracle)
- [ ] javax.mail.jar en `lib/` (JavaMail 1.6.2)
- [ ] activation.jar en `lib/` (requerido por JavaMail)
- [ ] `src/main/resources/mail.properties` configurado con cuenta Gmail + App Password
- [ ] VS Code + Extension Pack for Java instalado
- [ ] Archivo `Conexion.java` configurado con credenciales Oracle

---

## 🚨 Troubleshooting

### Error: "java.sql.SQLRecoverableException"
- Verifica que Oracle Database XE esté corriendo
- Comprueba credenciales en `Conexion.java`
- Asegúrate que el puerto 1521 no esté bloqueado

### Error: "oracle.jdbc.OracleDriver not found"
- Verifica que `ojdbc8.jar` esté en el classpath
- Si usas Maven, ejecuta: `mvn clean install`

### Error: "usuario spacework ya existe"
- El usuario ya fue creado
- Continúa con los siguientes pasos

---

## 📞 Soporte

Si tienes problemas, verifica:
1. La ruta de instalación de Java (`C:\Program Files\Java\`)
2. Las variables de entorno (PATH debe incluir Java y Maven)
3. El puerto 1521 no esté en uso
4. Que la contraseña de Oracle sea la correcta

---

> **Última actualización**: Abril 2026 — Fase 12 completada (MailService + EvaluacionFormularioHandler)
> Universidad Tecnológica del Perú — Curso Integrador I: Sistemas
