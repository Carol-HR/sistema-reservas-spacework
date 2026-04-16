# 🔧 Guía Técnica — Sistema de Gestión de Reservas de Espacios
> SpaceWork Perú S.A.C. · Arquitectura MVC + DAO · Java SE + Oracle Database

---

## 1. Arquitectura del Sistema

El sistema sigue el patrón **MVC (Modelo-Vista-Controlador)** combinado con el patrón **DAO (Data Access Object)** para separar claramente las responsabilidades de cada capa.

```
┌──────────────────────────────────────┐
│           VISTA (View)               │
│     Java Swing — Formularios UI      │
└─────────────────┬────────────────────┘
                  │ eventos / llamadas
┌─────────────────▼────────────────────┐
│        CONTROLADOR (Controller)      │
│   Lógica de negocio · Validaciones   │
└─────────────────┬────────────────────┘
                  │ consultas / datos
┌─────────────────▼────────────────────┐
│             DAO (Data Access)        │
│   Acceso a BD · PreparedStatement    │
└─────────────────┬────────────────────┘
                  │ JDBC (ojdbc8)
┌─────────────────▼────────────────────┐
│         Oracle Database XE           │
│  Tablas · Secuencias · Triggers      │
└──────────────────────────────────────┘
```

---

## 2. Estructura de Carpetas del Proyecto

```
SistemaReservasSpaceWork/
│
├── src/
│   └── com/
│       └── spacework/
│           │
│           ├── model/                  ← Entidades (POJOs)
│           │   ├── Usuario.java
│           │   ├── Espacio.java
│           │   ├── Cliente.java
│           │   ├── Reserva.java
│           │   ├── Pago.java
│           │   ├── Equipo.java
│           │   ├── Horario.java
│           │   ├── Descuento.java
│           │   ├── Evaluacion.java
│           │   └── Notificacion.java
│           │
│           ├── dao/                    ← Acceso a datos
│           │   ├── UsuarioDAO.java
│           │   ├── EspacioDAO.java
│           │   ├── ClienteDAO.java
│           │   ├── ReservaDAO.java
│           │   ├── PagoDAO.java
│           │   ├── EquipoDAO.java
│           │   ├── HorarioDAO.java
│           │   ├── DescuentoDAO.java
│           │   ├── EvaluacionDAO.java
│           │   └── NotificacionDAO.java
│           │
│           ├── controller/             ← Lógica de negocio
│           │   ├── AuthController.java
│           │   ├── EspacioController.java
│           │   ├── ClienteController.java
│           │   ├── ReservaController.java
│           │   ├── PagoController.java
│           │   ├── EquipoController.java
│           │   ├── HorarioController.java
│           │   ├── DescuentoController.java
│           │   ├── EvaluacionController.java
│           │   └── NotificacionController.java
│           │
│           ├── view/                   ← Interfaz Swing
│           │   ├── LoginForm.java
│           │   ├── MainFrame.java
│           │   ├── EspacioPanel.java
│           │   ├── ClientePanel.java
│           │   ├── ReservaPanel.java
│           │   ├── PagoPanel.java
│           │   ├── EquipoPanel.java
│           │   ├── HorarioPanel.java
│           │   ├── DescuentoPanel.java
│           │   ├── EvaluacionPanel.java
│           │   ├── NotificacionPanel.java
│           │   ├── ReportePanel.java
│           │   └── DashboardPanel.java
│           │
│           └── util/                   ← Utilidades
│               ├── Conexion.java
│               ├── HashUtil.java
│               ├── UITheme.java
│               ├── CSVExporter.java
│               └── DatabaseInitializer.java
│
├── lib/
│   └── ojdbc8.jar                      ← Driver JDBC Oracle
│
├── test/                               ← Pruebas JUnit
│   ├── EspacioDAOTest.java
│   ├── ClienteDAOTest.java
│   ├── ReservaDAOTest.java
│   ├── PagoDAOTest.java
│   ├── EquipoDAOTest.java
│   └── NotificacionDAOTest.java
│
└── sql/
    ├── 00_crear_usuario.sql
    ├── 01_crear_tablas.sql             ← 14 tablas incluidas
    ├── 02_crear_secuencias.sql
    ├── 03_crear_triggers.sql
    ├── 04_datos_iniciales.sql
    └── 05_agregar_roles_y_auditoria.sql
```

---

## 3. Modelos (Entidades)

### `Usuario.java`
```java
public class Usuario {
    private int idUsuario;
    private String username;
    private String passwordHash;
    private String nombre;
    private String email;
    private String rol;         // ADMINISTRADOR / OPERADOR
    private String estado;      // ACTIVO / INACTIVO
    // getters y setters...
}
```

### `Espacio.java`
```java
public class Espacio {
    private int idEspacio;
    private String nombre;
    private String tipo;         // SALA / AULA / AUDITORIO / LABORATORIO / AREA_COMUN
    private int capacidad;
    private String ubicacion;
    private double precioPorHora;
    private String estado;       // ACTIVO / INACTIVO
    // getters y setters...
}
```

### `Cliente.java`
```java
public class Cliente {
    private int idCliente;
    private String nombre;
    private String apellido;
    private String dni;          // UNIQUE
    private String email;        // UNIQUE
    private String telefono;
    private String estado;       // ACTIVO / INACTIVO
    // getters y setters...
}
```

### `Reserva.java`
```java
public class Reserva {
    private int idReserva;
    private Cliente cliente;
    private Espacio espacio;
    private Date fechaInicio;
    private Date fechaFin;
    private double montoTotal;
    private String estado;       // PENDIENTE / CONFIRMADA / COMPLETADA / CANCELADA
    private Date fechaRegistro;
    // getters y setters...
}
```

### `Pago.java`
```java
public class Pago {
    private int idPago;
    private int idReserva;
    private double monto;
    private String metodoPago;   // TARJETA / TRANSFERENCIA / EFECTIVO
    private String estadoPago;   // PENDIENTE / COMPLETADO / RECHAZADO / REEMBOLSADO
    private Date fechaPago;
    private Date fechaCreacion;
    // getters y setters...
}
```

### `Equipo.java`
```java
public class Equipo {
    private int idEquipo;
    private int idEspacio;
    private String nombre;
    private String tipo;
    private int cantidad;
    private String estado;       // ACTIVO / MANTENIMIENTO / INACTIVO
    private Date fechaAdquisicion;
    // getters y setters...
}
```

### `Horario.java`
```java
public class Horario {
    private int idHorario;
    private int idEspacio;
    private int diaSemana;       // 0-6 (Sunday-Saturday)
    private String horaApertura; // HH:MM format
    private String horaCierre;   // HH:MM format
    private String estado;       // ACTIVO / INACTIVO
    // getters y setters...
}
```

### `Descuento.java`
```java
public class Descuento {
    private int idDescuento;
    private String codigo;
    private String descripcion;
    private double porcentaje;
    private double montoMinimo;
    private Date fechaInicio;
    private Date fechaFin;
    private int usosMaximos;
    private int usosActuales;
    private String estado;       // ACTIVO / INACTIVO
    // getters y setters...
}
```

### `Evaluacion.java`
```java
public class Evaluacion {
    private int idEvaluacion;
    private int idReserva;
    private int idCliente;       // FK → CLIENTES (vinculado al cliente que pagó)
    private int calificacion;    // 1-5 estrellas
    private String comentario;   // Comentario opcional del cliente
    private Date fechaEvaluacion;
    // getters y setters...
}
```

### `Notificacion.java`
```java
public class Notificacion {
    private int idNotificacion;
    private int idUsuario;
    private String tipo;         // RESERVA / PAGO / RECORDATORIO / PROMOCION / SISTEMA
    private String asunto;
    private String mensaje;
    private boolean leida;
    private Date fechaCreacion;
    private Date fechaLeida;
    // getters y setters...
}
```

### `TokenEvaluacion.java` (NUEVO)
```java
public class TokenEvaluacion {
    private int idToken;
    private int idPago;
    private String token;        // UUID único y seguro
    private String emailCliente;
    private Date fechaCreacion;
    private Date fechaExpiracion; // 30 días
    private int utilizado;       // 0=Pendiente, 1=Evaluado
    // getters y setters...
}
```

---

## 4. DAOs — Acceso a Datos

### `Conexion.java`
```java
public class Conexion {
    private static final String URL  = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "spacework";
    private static final String PASS = "tu_password";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
```

### `HashUtil.java`
```java
import java.security.MessageDigest;

public class HashUtil {
    public static String sha256(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
```

### `TokenEvaluacionDAO.java` (NUEVO)
```java
public class TokenEvaluacionDAO {
    public boolean crearToken(TokenEvaluacion token) { ... }
    public TokenEvaluacion buscarPorToken(String token) { ... }
    public boolean marcarUtilizado(int idToken) { ... }
    public boolean tokenValido(String token) { ... }
    public boolean tokenExpirado(String token) { ... }
}
```

### Métodos principales por DAO

| DAO | Métodos |
|-----|---------|
| `UsuarioDAO` | `autenticar(username, hash)`, `buscarPorId(id)` |
| `EspacioDAO` | `listar()`, `buscarPorId(id)`, `insertar(e)`, `actualizar(e)`, `desactivar(id)` |
| `ClienteDAO` | `listar()`, `buscarPorDni(dni)`, `insertar(c)`, `actualizar(c)`, `desactivar(id)` |
| `ReservaDAO` | `listar()`, `insertar(r)`, `verificarDisponibilidad(espacioId, inicio, fin)`, `cambiarEstado(id, estado)`, `listarPorCliente(clienteId)` |
| `PagoDAO` | `listar()`, `buscarPorReserva(idReserva)`, `insertar(p)`, `actualizar(p)`, `cambiarEstado(id, estado)` |
| `EquipoDAO` | `listar()`, `listarPorEspacio(idEspacio)`, `insertar(e)`, `actualizar(e)`, `desactivar(id)` |
| `HorarioDAO` | `listar()`, `listarPorEspacio(idEspacio)`, `obtenerPorDia(idEspacio, dia)`, `insertar(h)`, `actualizar(h)` |
| `DescuentoDAO` | `listar()`, `validarCodigo(codigo)`, `buscarPorCodigo(codigo)`, `insertar(d)`, `actualizar(d)` |
| `EvaluacionDAO` | `listar()`, `insertar(e)`, `promedioCalificacion(idEspacio)`, `listarPorReserva(idReserva)` |
| `NotificacionDAO` | `listar()`, `listarPorUsuario(idUsuario)`, `insertar(n)`, `marcarComoLeida(idNotificacion)`, `contarPendientes(idUsuario)` |

---

## 5. Base de Datos — Oracle Database

### Tablas

#### `USUARIOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_usuario | NUMBER | PK (SEQ_USUARIOS) |
| username | VARCHAR2(50) | UNIQUE, NOT NULL |
| password_hash | VARCHAR2(64) | NOT NULL |
| nombre | VARCHAR2(100) | NOT NULL |
| email | VARCHAR2(150) | UNIQUE, NOT NULL |
| rol | VARCHAR2(20) | NOT NULL |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |
| fecha_registro | DATE | DEFAULT SYSDATE |

#### `ESPACIOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_espacio | NUMBER | PK (SEQ_ESPACIOS) |
| nombre | VARCHAR2(100) | NOT NULL |
| tipo | VARCHAR2(30) | NOT NULL |
| capacidad | NUMBER(4) | NOT NULL |
| ubicacion | VARCHAR2(200) | NOT NULL |
| precio_por_hora | NUMBER(10,2) | NOT NULL |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |

#### `CLIENTES`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_cliente | NUMBER | PK (SEQ_CLIENTES) |
| nombre | VARCHAR2(100) | NOT NULL |
| apellido | VARCHAR2(100) | NOT NULL |
| dni | VARCHAR2(15) | UNIQUE, NOT NULL |
| email | VARCHAR2(150) | UNIQUE, NOT NULL |
| telefono | VARCHAR2(15) | NULL |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |

#### `RESERVAS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_reserva | NUMBER | PK (SEQ_RESERVAS) |
| id_cliente | NUMBER | FK → CLIENTES |
| id_espacio | NUMBER | FK → ESPACIOS |
| fecha_inicio | TIMESTAMP | NOT NULL |
| fecha_fin | TIMESTAMP | NOT NULL |
| monto_total | NUMBER(10,2) | NOT NULL |
| estado | VARCHAR2(15) | DEFAULT 'PENDIENTE' |
| fecha_registro | TIMESTAMP | DEFAULT SYSTIMESTAMP |

#### `PAGOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_pago | NUMBER | PK (SEQ_PAGOS) |
| id_reserva | NUMBER | FK → RESERVAS |
| monto | NUMBER(10,2) | NOT NULL |
| metodo_pago | VARCHAR2(30) | NOT NULL |
| estado_pago | VARCHAR2(15) | DEFAULT 'PENDIENTE' |
| fecha_pago | DATE | NULL |
| fecha_creacion | DATE | DEFAULT SYSDATE |

#### `EQUIPOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_equipo | NUMBER | PK (SEQ_EQUIPOS) |
| id_espacio | NUMBER | FK → ESPACIOS |
| nombre | VARCHAR2(100) | NOT NULL |
| tipo | VARCHAR2(50) | NOT NULL |
| cantidad | NUMBER(4) | NOT NULL |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |
| fecha_adquisicion | DATE | NULL |

#### `HORARIOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_horario | NUMBER | PK (SEQ_HORARIOS) |
| id_espacio | NUMBER | FK → ESPACIOS |
| dia_semana | NUMBER(1) | NOT NULL (0-6) |
| hora_apertura | VARCHAR2(5) | NOT NULL |
| hora_cierre | VARCHAR2(5) | NOT NULL |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |

#### `DESCUENTOS`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_descuento | NUMBER | PK (SEQ_DESCUENTOS) |
| codigo | VARCHAR2(20) | UNIQUE, NOT NULL |
| descripcion | VARCHAR2(200) | NULL |
| porcentaje | NUMBER(5,2) | NOT NULL |
| monto_minimo | NUMBER(10,2) | NULL |
| fecha_inicio | DATE | NOT NULL |
| fecha_fin | DATE | NOT NULL |
| usos_maximos | NUMBER(5) | NULL |
| usos_actuales | NUMBER(5) | DEFAULT 0 |
| estado | VARCHAR2(10) | DEFAULT 'ACTIVO' |

#### `EVALUACIONES`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_evaluacion | NUMBER | PK (SEQ_EVALUACIONES) |
| id_reserva | NUMBER | FK → RESERVAS |
| id_cliente | NUMBER | FK → CLIENTES |
| calificacion | NUMBER(2) | NOT NULL (1-5) |
| comentario | VARCHAR2(500) | NULL |
| fecha_evaluacion | DATE | DEFAULT SYSDATE |

#### `TOKENS_EVALUACION` (NUEVO)
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_token | NUMBER | PK (SEQ_TOKENS_EVALUACION) |
| id_pago | NUMBER | FK → PAGOS |
| token | VARCHAR2(50) | UNIQUE, NOT NULL |
| email_cliente | VARCHAR2(150) | NOT NULL |
| fecha_creacion | DATE | DEFAULT SYSDATE |
| fecha_expiracion | DATE | NOT NULL |
| utilizado | NUMBER(1) | DEFAULT 0 (0=Pendiente, 1=Evaluado) |

#### `NOTIFICACIONES`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_notificacion | NUMBER | PK (SEQ_NOTIFICACIONES) |
| id_usuario | NUMBER | FK → USUARIOS |
| tipo | VARCHAR2(30) | NOT NULL (RESERVA, PAGO, RECORDATORIO, PROMOCION, SISTEMA, EVALUACION) |
| asunto | VARCHAR2(100) | NOT NULL |
| mensaje | VARCHAR2(500) | NOT NULL |
| leida | NUMBER(1) | DEFAULT 0 |
| fecha_creacion | DATE | DEFAULT SYSDATE |
| fecha_leida | DATE | NULL |

#### `AUDITORIA`
| Campo | Tipo | Restricción |
|-------|------|-------------|
| id_auditoria | NUMBER | PK (SEQ_AUDITORIA) |
| tabla_afectada | VARCHAR2(50) | NOT NULL |
| operacion | VARCHAR2(10) | NOT NULL |
| id_registro | NUMBER | NOT NULL |
| id_usuario | NUMBER | FK → USUARIOS |
| fecha_operacion | TIMESTAMP | DEFAULT SYSTIMESTAMP |
| descripcion | VARCHAR2(500) | NULL |

---

### Scripts SQL

#### `01_crear_tablas.sql` (fragmento)
```sql
-- Todas las 14 tablas se encuentran en el archivo:
-- 01. USUARIOS - Credenciales y roles
-- 02. ESPACIOS - Espacios disponibles
-- 03. CLIENTES - Información de clientes
-- 04. RESERVAS - Reservas de espacios
-- 05. PAGOS - Pagos por reserva
-- 06. EQUIPOS - Equipamiento de espacios
-- 07. HORARIOS - Horarios de operación
-- 08. DESCUENTOS - Promociones y descuentos
-- 09. EVALUACIONES - Calificaciones de usuarios
-- 10. NOTIFICACIONES - Notificaciones a usuarios
-- 11. AUDITORIA - Registro de cambios

-- Ejemplo de tabla RESERVAS:
CREATE TABLE RESERVAS (
    id_reserva    NUMBER          CONSTRAINT pk_reservas PRIMARY KEY,
    id_cliente    NUMBER          CONSTRAINT fk_res_cliente REFERENCES CLIENTES(id_cliente),
    id_espacio    NUMBER          CONSTRAINT fk_res_espacio REFERENCES ESPACIOS(id_espacio),
    fecha_inicio  TIMESTAMP       NOT NULL,
    fecha_fin     TIMESTAMP       NOT NULL,
    monto_total   NUMBER(10,2)    NOT NULL,
    estado        VARCHAR2(15)    DEFAULT 'PENDIENTE' NOT NULL,
    fecha_registro TIMESTAMP      DEFAULT SYSTIMESTAMP,
    CONSTRAINT chk_fechas CHECK (fecha_fin > fecha_inicio)
);

-- El archivo completo contiene las definiciones de todas las tablas
-- con constraints, triggers y validaciones
```

#### `02_crear_secuencias.sql`
```sql
CREATE SEQUENCE SEQ_USUARIOS      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_ESPACIOS      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_CLIENTES      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_RESERVAS      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_PAGOS         START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_EQUIPOS       START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_HORARIOS      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_DESCUENTOS    START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_EVALUACIONES  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_NOTIFICACIONES START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE SEQ_AUDITORIA     START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
```

#### `03_crear_triggers.sql`
```sql
CREATE OR REPLACE TRIGGER TRG_AUDITORIA_RESERVAS
AFTER INSERT OR UPDATE OR DELETE ON RESERVAS
FOR EACH ROW
BEGIN
    IF INSERTING THEN
        INSERT INTO AUDITORIA (id_auditoria, tabla_afectada, operacion, id_registro, fecha_operacion)
        VALUES (SEQ_AUDITORIA.NEXTVAL, 'RESERVAS', 'INSERT', :NEW.id_reserva, SYSTIMESTAMP);
    ELSIF UPDATING THEN
        INSERT INTO AUDITORIA (id_auditoria, tabla_afectada, operacion, id_registro, fecha_operacion)
        VALUES (SEQ_AUDITORIA.NEXTVAL, 'RESERVAS', 'UPDATE', :NEW.id_reserva, SYSTIMESTAMP);
    ELSIF DELETING THEN
        INSERT INTO AUDITORIA (id_auditoria, tabla_afectada, operacion, id_registro, fecha_operacion)
        VALUES (SEQ_AUDITORIA.NEXTVAL, 'RESERVAS', 'DELETE', :OLD.id_reserva, SYSTIMESTAMP);
    END IF;
END;
/
```

---

## 6. Seguridad

- Las contraseñas se almacenan como **hash SHA-256** (nunca en texto plano)
- Todas las consultas SQL usan **PreparedStatement** para prevenir inyección SQL
- La eliminación de registros es **lógica** (campo `estado = 'INACTIVO'`), nunca física
- El control de acceso se maneja por **rol** (ADMINISTRADOR / OPERADOR)

---

## 7. Pruebas Unitarias — JUnit

### Ejemplo `EspacioDAOTest.java`
```java
import org.junit.Test;
import static org.junit.Assert.*;

public class EspacioDAOTest {

    @Test
    public void testListarEspacios() {
        EspacioDAO dao = new EspacioDAO();
        List<Espacio> lista = dao.listar();
        assertNotNull(lista);
        assertTrue(lista.size() >= 0);
    }

    @Test
    public void testInsertarEspacio() {
        EspacioDAO dao = new EspacioDAO();
        Espacio e = new Espacio();
        e.setNombre("Sala Test");
        e.setTipo("SALA");
        e.setCapacidad(10);
        e.setUbicacion("Piso 1");
        e.setPrecioPorHora(50.00);
        boolean resultado = dao.insertar(e);
        assertTrue(resultado);
    }
}
```

---

## 8. Convenciones de Código

| Elemento | Convención |
|----------|-----------|
| Clases | PascalCase: `ClienteDAO`, `ReservaPanel` |
| Métodos | camelCase: `listarEspacios()`, `verificarDisponibilidad()` |
| Variables | camelCase: `idCliente`, `montoTotal` |
| Constantes | UPPER_SNAKE_CASE: `ESTADO_ACTIVO` |
| Paquetes | lowercase: `com.spacework.model` |
| SQL | UPPER_CASE para palabras reservadas |

---

## 9. Flujo de Estados — Reservas

```
                    ┌─────────────┐
    (crear reserva) │   PENDIENTE │
                    └──────┬──────┘
                           │ operador confirma
                    ┌──────▼──────┐
                    │  CONFIRMADA │
                    └──────┬──────┘
                           │ servicio prestado
                    ┌──────▼──────┐
                    │  COMPLETADA │ ◄── estado final positivo
                    └─────────────┘

    PENDIENTE  ──► CANCELADA  (cliente o admin)
    CONFIRMADA ──► CANCELADA  (cliente o admin)
```

---

## 10. Configuración de NetBeans

1. **Crear proyecto**: File → New Project → Java → Java Application
2. **Agregar ojdbc8.jar**: clic derecho en Libraries → Add JAR/Folder
3. **Agregar JUnit**: clic derecho en Libraries → Add Library → JUnit 4
4. **Crear paquetes**: clic derecho en Source Packages → New → Java Package
5. **Generar JAR**: Clean and Build → el JAR queda en `/dist`

---

## 11. Nuevas Funcionalidades Implementadas (Fase 5+)

### Validación y Control de Solapamientos
**Archivo:** `com.spacework.controller.ValidacionController`

Previene reservas conflictivas en espacios:
```java
ValidacionController validador = ValidacionController.getInstance();
validador.validarNoSolapamiento(espacio, fechaInicio, fechaFin);
validador.validarFechas(fechaInicio, fechaFin);
validador.validarNoEnPasado(fecha);
validador.validarHorariosBloqueados(idEspacio, fechaInicio, fechaFin);
```

**Lanza:** `RuntimeException` si hay conflictos.

---

### Auditoría y Registro de Cambios
**Archivo:** `com.spacework.controller.AuditoriaController`

Registra modificaciones en tablas:
```java
AuditoriaController auditoria = AuditoriaController.getInstance();
auditoria.registrarCambio("USUARIOS", idUsuario, "UPDATE", cambiosMap);
auditoria.registrarCambioReserva(idReserva, estadoAnterior, estadoNuevo, usuario);
```

**Tabla BD:** `AUDITORIA` con campos: id_auditoria, tabla, id_registro, tipo_cambio, usuario, fecha_hora, cambios.

---

### Gestión de Roles
**Archivo:** `com.spacework.model.Rol` + `com.spacework.dao.RolDAO`

Estructura de roles para control de permisos:
- **ADMIN**: acceso total al sistema
- **GERENTE**: gestión de espacios y ocupación
- **CLIENTE**: solo ver y crear sus propias reservas

**Métodos DAO:**
```java
RolDAO.listarRoles();
RolDAO.obtenerPorNombre("ADMIN");
RolDAO.registrarRol(rolNombre, descripcion);
```

---

### Horarios Bloqueados
**Archivo:** `com.spacework.model.HorarioBloqueado` + `com.spacework.dao.HorarioBloqueadoDAO`

Bloquea franjas de tiempo en espacios (mantenimiento, eventos privados, etc.):
```java
HorarioBloqueadoDAO.registrar(idEspacio, fechaInicio, fechaFin, razon);
HorarioBloqueadoDAO.listarPorEspacio(idEspacio);
HorarioBloqueadoDAO.eliminar(idBloqueo);
```

**Tabla BD:** `HORARIOS_BLOQUEADOS` — valida antes de confirmar reservas.

---

### Dashboard con KPI
**Archivo:** `com.spacework.view.DashboardPanel`

Panel principal con 4 tarjetas de métricas:
1. **Ingresos Mes Actual** — suma de montos de reservas completadas
2. **Espacios Disponibles** — espacios con estado ACTIVO
3. **Reservas Pendientes** — reservas sin confirmar
4. **Ocupación Promedio** — % de horas reservadas vs. totales

Se carga automáticamente en segundo plano (SwingWorker). Acceso: pestaña "Dashboard" en MainFrame.

---

### Cambio de Contraseña
**Archivo:** `com.spacework.view.CambioContraseñaDialog`

Diálogo modal para que usuarios cambien su contraseña:
- Validaciones: mínimo 6 caracteres, coincidencia, verificación de contraseña actual
- Acceso: Menú "Sistema" → "Cambiar contraseña" o botón en header
- Usa `AuthController.cambiarContraseña(usuario, passwordActual, passwordNueva)`

---

### Exportación a CSV
**Archivo:** `com.spacework.util.CSVExporter`

Exporta cualquier JTable a archivo CSV:
```java
CSVExporter.exportarTabla(table, "Reporte Reservas");
```

- Abre JFileChooser con filtro *.csv
- Codifica valores entre comillas para manejar comas
- Acceso: botón "Exportar a CSV" en pestaña Reportes

---

### Inicialización Automática de BD
**Archivo:** `com.spacework.util.DatabaseInitializer`

Ejecuta al startup (desde `Main.java`) para crear tablas y columnas si faltan:
```java
DatabaseInitializer.inicializar();
```

**Verifica y crea:**
- Tabla ROLES (si no existe)
- Columna rol en USUARIOS (si no existe)
- Tabla HORARIOS_BLOQUEADOS (si no existe)
- Secuencias: SEQ_ROLES, SEQ_BLOQUEOS

Este componente garantiza compatibilidad incluso si el usuario no ha ejecutado manualmente los scripts SQL adicionales.

---

### Tema Visual (UITheme)
**Archivo:** `com.spacework.util.UITheme`

Sistema centralizado de colores y fuentes:
```java
// Colores principales
Color primary   = UITheme.PRIMARY;      // Azul corporativo
Color success   = UITheme.SUCCESS;      // Verde
Color danger    = UITheme.DANGER;       // Rojo
Color warning   = UITheme.WARNING;      // Amarillo

// Componentes estilizados
JButton btn = UITheme.makePrimaryButton("Guardar");
JTextField field = UITheme.makeTextField(20);
UITheme.styleTable(table);
```

**Look & Feel:** Nimbus (multiplataforma) con overrides de colores base.

---

> Universidad Tecnológica del Perú — Curso Integrador I: Sistemas · 2024
