# 🗺️ Plan de Desarrollo — Sistema de Gestión de Reservas
> SpaceWork Perú S.A.C. · Java SE + Oracle Database · **Versión 2.0 WEB**

---

## 📋 Resumen de Fases

| Fase | Descripción | Estado |
|------|-------------|--------|
| 1 | Configuración del entorno y base de datos | ✅ Completado |
| 2 | Estructura del proyecto y conexión | ✅ Completado |
| 3 | Módulo de Autenticación | ✅ Completado |
| 4 | Módulo de Gestión de Espacios | ✅ Completado |
| 5 | Módulo de Gestión de Clientes | ✅ Completado |
| 6 | Módulo de Reservas | ✅ Completado |
| 7 | Módulo de Reportes | ✅ Completado |
| 8 | Pruebas unitarias con JUnit | ✅ Completado |
| 9 | Pruebas integrales y ajustes finales | ✅ Completado |
| **Fase Extra:** | Validación, Auditoría, Dashboard, Roles, CSV Export, Cambio de contraseña | ✅ Completado |
| **Fase 11:** | Módulo de Evaluaciones por Email con TOKENS_EVALUACION | ✅ Completado |
| **Fase 12:** | MailService Gmail SMTP + EvaluacionFormularioHandler + ruta REST pública | ✅ Completado |
| **Fase 13:** | **Módulo web con HTML5 + Bootstrap 5** | ✅ **Completado** |
| **Fase 14:** | **2 emails automáticos al pagar + Token evaluación** | ✅ **Completado** |

---

## FASE 1 — Configuración del Entorno

### 1.1 Instalar herramientas
- [x] JDK 8 o superior → [https://www.oracle.com/java/technologies/downloads/](https://www.oracle.com/java/technologies/downloads/)
- [x] Apache NetBeans 12+ → [https://netbeans.apache.org/](https://netbeans.apache.org/)
- [x] Oracle Database XE → [https://www.oracle.com/database/technologies/xe-downloads.html](https://www.oracle.com/database/technologies/xe-downloads.html)
- [x] SQL Developer (opcional, para gestionar la BD visualmente)
- [x] Descargar `ojdbc8.jar` desde Oracle

### 1.2 Crear el esquema en Oracle
Ejecutar el script SQL en este orden:
1. Crear tablas (USUARIOS, ESPACIOS, CLIENTES, RESERVAS, AUDITORIA, PAGOS, EQUIPOS, HORARIOS, DESCUENTOS, EVALUACIONES, NOTIFICACIONES, TOKENS_EVALUACION)
2. Crear secuencias (`SEQ_USUARIOS`, `SEQ_ESPACIOS`, `SEQ_CLIENTES`, `SEQ_RESERVAS`, `SEQ_AUDITORIA`, `SEQ_PAGOS`, `SEQ_EQUIPOS`, `SEQ_HORARIOS`, `SEQ_DESCUENTOS`, `SEQ_EVALUACIONES`, `SEQ_NOTIFICACIONES`, `SEQ_TOKENS_EVALUACION`)
3. Crear triggers (`TRG_AUDITORIA_RESERVAS`, etc.)
4. Insertar datos de prueba (usuario administrador inicial)

```sql
-- Ejemplo: crear usuario administrador inicial
INSERT INTO USUARIOS (id_usuario, username, password_hash, nombre, email, rol, estado)
VALUES (SEQ_USUARIOS.NEXTVAL, 'admin', '<hash_sha256_de_la_contraseña>', 
        'Administrador', 'admin@spacework.pe', 'ADMINISTRADOR', 'ACTIVO');
COMMIT;
```

---

## FASE 2 — Estructura del Proyecto en NetBeans

### 2.1 Crear proyecto Java Application en NetBeans
```
SistemaReservasSpaceWork/
├── src/
│   └── com/spacework/
│       ├── model/
│       │   ├── Usuario.java
│       │   ├── Espacio.java
│       │   ├── Cliente.java
│       │   └── Reserva.java
│       ├── dao/
│       │   ├── UsuarioDAO.java
│       │   ├── EspacioDAO.java
│       │   ├── ClienteDAO.java
│       │   └── ReservaDAO.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── EspacioController.java
│       │   ├── ClienteController.java
│       │   └── ReservaController.java
│       ├── view/
│       │   ├── LoginForm.java
│       │   ├── MainFrame.java
│       │   ├── EspacioPanel.java
│       │   ├── ClientePanel.java
│       │   ├── ReservaPanel.java
│       │   └── ReportePanel.java
│       └── util/
│           ├── Conexion.java
│           └── HashUtil.java
├── lib/
│   └── ojdbc8.jar
└── test/
    ├── EspacioDAOTest.java
    ├── ClienteDAOTest.java
    └── ReservaDAOTest.java
```

### 2.2 Configurar ojdbc8.jar
- [x] Clic derecho en el proyecto → Properties → Libraries → Add JAR/Folder
- [x] Seleccionar `ojdbc8.jar`

### 2.3 Crear `Conexion.java`
```java
public class Conexion {
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String USER = "spacework";
    private static final String PASSWORD = "tu_password";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### 2.4 Crear `HashUtil.java`
```java
public class HashUtil {
    public static String sha256(String input) {
        // Implementar con MessageDigest SHA-256
    }
}
```

---

## FASE 3 — Módulo de Autenticación

### Clases a crear
- [x] `Usuario.java` (model) — atributos: id, username, passwordHash, nombre, email, rol, estado
- [x] `UsuarioDAO.java` (dao) — método: `autenticar(username, passwordHash)`
- [x] `AuthController.java` (controller) — lógica de login y control de sesión
- [x] `LoginForm.java` (view) — formulario Swing con campos usuario/contraseña

### Funcionalidades
- [x] Validar usuario y contraseña hasheada contra la BD
- [x] Guardar usuario en sesión (variable global o singleton)
- [x] Redirigir según rol (Administrador o Operador)
- [x] Mostrar mensaje de error si credenciales incorrectas
- [x] Splash Screen con gradiente animado

---

## FASE 4 — Módulo de Gestión de Espacios

### Clases a crear
- [x] `Espacio.java` (model) — atributos: id, nombre, tipo, capacidad, ubicacion, precioPorHora, estado
- [x] `EspacioDAO.java` (dao) — métodos: `listar()`, `buscarPorId()`, `insertar()`, `actualizar()`, `desactivar()`
- [x] `EspacioController.java` (controller)
- [x] `EspacioPanel.java` (view) — tabla + formulario CRUD

### Funcionalidades
- [x] Listar todos los espacios activos en una JTable
- [x] Registrar nuevo espacio
- [x] Editar espacio existente
- [x] Desactivar espacio (eliminación lógica → estado INACTIVO)
- [x] Filtrar por tipo de espacio
- [x] Filtro en vivo (TableRowSorter)
- [x] Tooltips en botones
- [x] Diseño con UITheme

---

## FASE 5 — Módulo de Gestión de Clientes

### Clases a crear
- [x] `Cliente.java` (model) — atributos: id, nombre, apellido, dni, email, telefono, estado
- [x] `ClienteDAO.java` (dao) — métodos CRUD + búsqueda por DNI y email
- [x] `ClienteController.java` (controller)
- [x] `ClientePanel.java` (view)

### Funcionalidades
- [x] Listar clientes en JTable con búsqueda
- [x] Registrar nuevo cliente (validar DNI y email únicos)
- [x] Editar datos del cliente
- [x] Desactivar cliente (lógico)
- [x] Filtro en vivo por DNI
- [x] Tooltips en botones
- [x] Diseño con UITheme

---

## FASE 6 — Módulo de Reservas (núcleo del sistema)

### Clases a crear
- [x] `Reserva.java` (model) — atributos: id, cliente, espacio, fechaInicio, fechaFin, montoTotal, estado
- [x] `ReservaDAO.java` (dao) — métodos: CRUD + verificar disponibilidad + cambiar estado
- [x] `ReservaController.java` (controller)
- [x] `ReservaPanel.java` (view)

### Funcionalidades
- [x] Verificar disponibilidad antes de confirmar (sin solapamiento de horarios)
- [x] Calcular monto total automáticamente (horas × precio por hora)
- [x] Registrar nueva reserva con estado `PENDIENTE`
- [x] Confirmar reserva → estado `CONFIRMADA`
- [x] Completar reserva → estado `COMPLETADA`
- [x] Cancelar reserva → estado `CANCELADA`
- [x] Historial de reservas por cliente
- [x] JSpinner para selección de fecha y hora
- [x] Columna Estado con colores (PENDIENTE=amarillo, CONFIRMADA=verde, COMPLETADA=azul, CANCELADA=rojo)
- [x] Filtro en vivo
- [x] Validación de solapamientos
- [x] Auditoría de cambios de estado

### Flujo de estados
```
PENDIENTE ──► CONFIRMADA ──► COMPLETADA
    │               │
    └───────────────┴──► CANCELADA
```

---

## FASE 7 — Módulo de Reportes

### Clases a crear
- [x] `ReporteDAO.java` (dao) — consultas SQL de agregación
- [x] `ReporteController.java` (controller)
- [x] `ReportePanel.java` (view) — salida de reportes con CSV export

### Reportes a implementar
- [x] **Ingresos mensuales**: suma de montos de reservas COMPLETADAS por mes/año
- [x] **Ocupación por espacio**: % de uso de cada espacio en un rango de fechas
- [x] **Distribución de estados**: cantidad de reservas por estado

### Funcionalidades adicionales
- [x] Exportación a CSV de reportes
- [x] Interfaz tabular con JTable
- [x] Filtros por rango de fechas

---

## FASE 8 — Pruebas Unitarias con JUnit

### Clases de prueba a crear
- [x] `EspacioDAOTest.java` → testear insertar, listar, actualizar, desactivar
- [x] `ClienteDAOTest.java` → testear insertar, buscar por DNI, actualizar
- [x] `ReservaDAOTest.java` → testear insertar, verificar disponibilidad, cambiar estado

### Funcionalidades
- [x] Tests ejecutables con JUnit 4
- [x] Cobertura de casos principales
- [x] Manejo de excepciones

---

## FASE 9 — Pruebas Integrales y Ajustes Finales

- [x] Prueba de flujo completo: login → crear espacio → registrar cliente → hacer reserva → confirmar → completar
- [x] Probar casos límite: doble reserva, horarios solapados, campos vacíos
- [x] Revisar mensajes de error y validaciones en la UI
- [x] Ajustar estilos de la interfaz Swing (Look and Feel)
- [x] Documentar el código (Javadoc)
- [x] Generar proyecto corriendo sin errores de compilación

---

## FASE 10 — Características Avanzadas Implementadas

### Validación y Prevención de Solapamientos
- [x] Crear `ValidacionController.java` con métodos:
  - `validarNoSolapamiento()` — previene doble reserva en mismo espacio/hora
  - `validarFechas()` — valida que fin > inicio
  - `validarNoEnPasado()` — rechaza fechas pasadas
  - `validarHorariosBloqueados()` — consulta HORARIOS_BLOQUEADOS

### Auditoría y Registro de Cambios
- [x] Crear `AuditoriaLog.java` (model)
- [x] Crear `AuditoriaDAO.java` (dao)
- [x] Crear `AuditoriaController.java` (controller)
- [x] Registrar automáticamente cambios en estado de reservas
- [x] Tabla AUDITORIA en BD con timestamps y usuario responsable

### Gestión de Roles
- [x] Crear `Rol.java` (model) — id_rol, nombre, descripcion
- [x] Crear `RolDAO.java` (dao) — listarRoles, obtenerPorNombre, registrarRol
- [x] Agregar columna `rol` a tabla USUARIOS
- [x] Tres roles predefinidos: ADMIN, GERENTE, CLIENTE

### Horarios Bloqueados
- [x] Crear `HorarioBloqueado.java` (model)
- [x] Crear `HorarioBloqueadoDAO.java` (dao)
- [x] Tabla HORARIOS_BLOQUEADOS para bloquear franjas temporales
- [x] Validación automática en creación de reservas

### Dashboard Ejecutivo
- [x] Crear `DashboardPanel.java` con 4 KPI:
  - Ingresos del mes actual
  - Espacios disponibles (estado ACTIVO)
  - Reservas pendientes (estado PENDIENTE)
  - Porcentaje de ocupación promedio
- [x] Carga de datos en hilo separado (SwingWorker)
- [x] Interfaz card-based con colores corporativos

### Cambio de Contraseña
- [x] Crear `CambioContraseñaDialog.java` (view modal)
- [x] Agregar método `cambiarContraseña()` a `AuthController`
- [x] Agregar método `actualizarContraseña()` a `UsuarioDAO`
- [x] Validaciones: mín 6 caracteres, coincidencia, verificación contraseña actual
- [x] Acceso vía Menú Sistema + botón en header de MainFrame

### Exportación de Reportes a CSV
- [x] Crear `CSVExporter.java` (utility)
- [x] Método `exportarTabla(JTable, String titulo)` (static)
- [x] JFileChooser con filtro .csv
- [x] Exportación de cualquier JTable a archivo CSV
- [x] Botón "Exportar a CSV" en pestaña Reportes

### Inicialización Automática de BD
- [x] Crear `DatabaseInitializer.java` (utility)
- [x] Método `inicializar()` llamado en `Main.java` antes de UI
- [x] Verifica y crea tablas/columnas si faltan
- [x] Crea secuencias automáticamente
- [x] Garantiza compatibilidad incluso sin ejecutar scripts SQL manuales

### Sistema de Temas (UITheme)
- [x] Crear `UITheme.java` con paleta de colores corporativos
- [x] Constantes de colores: PRIMARY, SUCCESS, DANGER, WARNING, BG_MAIN, BG_CARD, etc.
- [x] Constantes de fuentes (9 variantes)
- [x] Métodos factory para componentes estilizados:
  - `makePrimaryButton()`, `makeSecondaryButton()`
  - `makeTextField()`, `makePasswordField()`
  - `styleTable()`, `makeBorder()`
- [x] Look & Feel: Nimbus multiplataforma
- [x] Aplicar a todas las vistas (LoginForm, MainFrame, Panels, etc.)

### Mejoras de UX
- [x] Splash Screen con gradiente animado y barra de progreso
- [x] Tabla de Reservas con columna Estado con colores:
  - PENDIENTE = Amarillo
  - CONFIRMADA = Verde
  - COMPLETADA = Azul
  - CANCELADA = Rojo
- [x] JSpinner para selección de fecha/hora (reemplaza JTextField)
- [x] Tooltips en todos los botones
- [x] Filtros en vivo (TableRowSorter) en todas las tablas
- [x] Mensaje de confirmación antes de cancelar/eliminar
- [x] Validaciones en tiempo real en formularios

### Gestión de Pagos y Transacciones
- [x] Crear `Pago.java` (model) — atributos: id, idReserva, monto, metodoPago, estadoPago, fechaPago
- [x] Crear `PagoDAO.java` (dao) — CRUD + consultas por estado y período
- [x] Crear `PagoController.java` (controller) — lógica de pagos y validaciones
- [x] Crear `PagoPanel.java` (view) — gestión de pagos de reservas
- [x] Métodos de pago: TARJETA, TRANSFERENCIA, EFECTIVO
- [x] Estados de pago: PENDIENTE, COMPLETADO, RECHAZADO, REEMBOLSADO
- [x] Tabla PAGOS en Oracle con FK a RESERVAS
- [x] Validación de monto mínimo y máximo

### Gestión de Equipos y Recursos
- [x] Crear `Equipo.java` (model) — atributos: id, idEspacio, nombre, tipo, cantidad, estado
- [x] Crear `EquipoDAO.java` (dao) — CRUD + búsqueda por espacio
- [x] Crear `EquipoController.java` (controller)
- [x] Crear `EquipoPanel.java` (view) — asignación de equipos a espacios
- [x] Estados de equipo: ACTIVO, MANTENIMIENTO, INACTIVO
- [x] Tabla EQUIPOS en Oracle con FK a ESPACIOS
- [x] Control de cantidad disponible y en uso

### Gestión de Horarios Operacionales
- [x] Crear `Horario.java` (model) — atributos: id, idEspacio, diaSemana, horaApertura, horaCierre
- [x] Crear `HorarioDAO.java` (dao) — CRUD + búsqueda por espacio
- [x] Crear `HorarioController.java` (controller)
- [x] Crear `HorarioPanel.java` (view) — configuración de horarios por día
- [x] Tabla HORARIOS en Oracle con FK a ESPACIOS
- [x] Semana: 0-6 (domingo a sábado)
- [x] Validación de formato HH:MM
- [x] Integración con disponibilidad de reservas

### Sistema de Descuentos y Promociones
- [x] Crear `Descuento.java` (model) — atributos: id, codigo, descripcion, porcentaje, montoMinimo, fechaInicio, fechaFin
- [x] Crear `DescuentoDAO.java` (dao) — CRUD + validación de códigos vigentes
- [x] Crear `DescuentoController.java` (controller)
- [x] Crear `DescuentoPanel.java` (view) — administración de promociones
- [x] Tabla DESCUENTOS en Oracle
- [x] Validación de fechas de vigencia
- [x] Límite de usos por código
- [x] Monto mínimo para aplicar descuento
- [x] Integración con cálculo de montos en reservas

### Sistema de Evaluaciones y Calificaciones
- [x] Crear `Evaluacion.java` (model) — atributos: id, idReserva, **idCliente** (FK → CLIENTES), calificacion, comentario, fechaEvaluacion
- [x] Crear `EvaluacionDAO.java` (dao) — CRUD + promedio de calificaciones por espacio
- [x] Crear `EvaluacionController.java` (controller)
- [x] Crear `EvaluacionPanel.java` (view) — **solo lectura** (el registro es por email)
- [x] Tabla EVALUACIONES en Oracle con FK a RESERVAS y **CLIENTES** (no USUARIOS)
- [x] Calificaciones de 1 a 5 estrellas
- [x] Comentarios opcionales (hasta 500 caracteres)
- [x] Cálculo de promedio de calificaciones por espacio

### Sistema de Notificaciones
- [x] Crear `Notificacion.java` (model) — atributos: id, idUsuario, tipo, asunto, mensaje, leida, fechaCreacion
- [x] Crear `NotificacionDAO.java` (dao) — CRUD + marcar como leída + contar sin leer
- [x] Crear `NotificacionController.java` (controller)
- [x] Crear `NotificacionPanel.java` (view) — inbox de notificaciones
- [x] Tabla NOTIFICACIONES en Oracle con FK a USUARIOS
- [x] Tipos: RESERVA, PAGO, RECORDATORIO, PROMOCION, SISTEMA
- [x] Control de lectura con timestamp
- [x] Integración con eventos de negocio (nueva reserva, pago completado, etc.)

---

## 📅 Cronograma Sugerido

| Semana | Fases |
|--------|-------|
| Semana 1 | Fase 1 + Fase 2 |
| Semana 2 | Fase 3 + Fase 4 |
| Semana 3 | Fase 5 + Fase 6 |
| Semana 4 | Fase 7 + Fase 8 + Fase 9 |
| Semana 5+ | Fase 10 (mejoras, pagos, equipos, horarios, descuentos, evaluaciones, notificaciones) |

---

## FASE 11 — Sistema de Evaluaciones por Email (Tokens UUID)

### Objetivo
Permitir que el cliente evalúe su experiencia sin hacer login, mediante un enlace de estrella enviado por correo al completar el pago.

### Cambios en BD
- [x] Tabla EVALUACIONES: cambiar `id_usuario` → `id_cliente` (FK → CLIENTES)
- [x] Agregar tabla `TOKENS_EVALUACION` con campos: id_token, id_pago, token (UUID), email_cliente, fecha_creacion, fecha_expiracion, utilizado
- [x] Agregar secuencia `SEQ_TOKENS_EVALUACION`

### Clases creadas/modificadas
- [x] `TokenEvaluacion.java` (model) — métodos: `estaExpirado()`, `estaDisponible()`, `esValido()`
- [x] `TokenEvaluacionDAO.java` (dao) — métodos: `crearToken()`, `buscarPorToken()`, `marcarUtilizado()`, `limpiarExpirados()`
- [x] `Evaluacion.java` (model) — campo `idCliente` en lugar de `idUsuario`
- [x] `EvaluacionDAO.java` (dao) — todas las queries usan `id_cliente`
- [x] `EvaluacionController.java` (controller) — parámetro `registrarEvaluacion(idReserva, idCliente, ...)`
- [x] `EvaluacionPanel.java` (view) — convertido a **modo solo-lectura** (sin formulario CRUD)
- [x] `PagoController.completarPago()` — genera UUID token + inserta en TOKENS_EVALUACION
- [x] `ReservaDAO.buscarPorId(int idReserva)` — método nuevo para recuperar reserva desde token

---

## FASE 12 — MailService Gmail SMTP + Endpoint Público de Evaluación

### Objetivo
Enviar el correo HTML con 5 links de estrella al cliente, y recibir su selección registrando la evaluación en BD sin requerir login.

### Configuración de email
- [x] Descargar `javax.mail.jar` (1.6.2) y `activation.jar` (1.1.1) en `lib/` manualmente
- [x] Agregar ambos como dependencia `system` scope en `pom.xml`
- [x] Crear `src/main/resources/mail.properties`:
  - smtp.host=smtp.gmail.com, puerto=587, STARTTLS habilitado
  - smtp.user=juancmduel@gmail.com, App Password (sin espacios)

### Clases creadas
- [x] `MailService.java` (util) — carga `mail.properties` desde classpath; métodos: `enviarTokenEvaluacion()`, `enviarConfirmacionPago()`, `enviarCorreo()`
- [x] `EvaluacionFormularioHandler.java` (controller/api) — `HttpHandler` registrado en `/evaluaciones/formulario`:
  - `GET ?token=xxx` → muestra página HTML de selección de estrellas
  - `GET ?token=xxx&calificacion=N` → valida token, inserta EVALUACIONES, marca token usado, devuelve página de éxito
  - Errores manejados: token nulo, token no encontrado (404), token ya utilizado, token expirado
- [x] `SpaceWorkApplication.java` — ruta `server.createContext("/evaluaciones/formulario", new EvaluacionFormularioHandler())` registrada (sin prefijo `/api/`)

### Flujo completo verificado
```
CompletarPago() → GenerarUUID → InsertarToken → MailService.enviarTokenEvaluacion()
    ↓
Cliente recibe email HTML con 5 links de estrella
    ↓
Cliente hace clic en ⭐⭐⭐⭐⭐
    ↓
GET /evaluaciones/formulario?token=xxx&calificacion=5
    ↓
Servidor valida token → inserta EVALUACIONES → marca token UTILIZADO
    ↓
Pantalla de agradecimiento HTML
```

---

## ✨ Estado Final del Proyecto

**Todas las fases completadas y ampliadas.** El sistema está **listo para producción** con:

- ✅ 15 modelos de datos (+ TokenEvaluacion; Evaluacion usa idCliente)
- ✅ 15 DAOs con acceso a BD Oracle (+ TokenEvaluacionDAO; ReservaDAO con buscarPorId)
- ✅ 11+ controladores con lógica de negocio avanzada (+ EvaluacionFormularioHandler)
- ✅ 10+ vistas Swing con diseño moderno (EvaluacionPanel en modo lectura)
- ✅ 7 utilidades (+ MailService con Gmail SMTP)
- ✅ 12 tablas en Oracle (EVALUACIONES usa id_cliente; + TOKENS_EVALUACION)
- ✅ 5+ tests unitarios con JUnit
- ✅ Compilación limpia sin errores
- ✅ Documentación técnica actualizada
- ✅ Sistema de pagos integrado
- ✅ Gestión completa de recursos y equipos
- ✅ Control horario operacional
- ✅ Sistema de promociones y descuentos
- ✅ Evaluaciones por email con token UUID (sin login)
- ✅ Sistema de notificaciones
- ✅ Gmail SMTP con JavaMail 1.6.2 (correo de evaluación confirmado enviado)
