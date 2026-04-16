# 📋 Sistema de Gestión de Reservas de Espacios
> **SpaceWork Perú S.A.C.** — Universidad Tecnológica del Perú  
> Curso Integrador I: Sistemas de Software · Lima, Perú · 2026  
> **Versión 2.0 — Aplicación Web REST + Oracle**  
> **Estado: ✅ COMPLETADO Y EN PRODUCCIÓN**

---

## 📌 Descripción del Proyecto

SpaceWork Perú S.A.C. es una empresa limeña dedicada al alquiler de espacios de trabajo compartido (coworking), con espacios como salas de reunión, aulas de capacitación, auditorios, laboratorios de cómputo y áreas comunes.

Este sistema reemplaza el proceso manual actual (Excel, libretas, WhatsApp) con una **aplicación de escritorio centralizada** que automatiza la gestión de reservas, controla la disponibilidad en tiempo real y genera reportes de gestión.

---

## ⚠️ Problema que Resuelve

| Problema | Impacto |
|----------|---------|
| Doble reserva de espacios | Conflictos y mala experiencia de cliente |
| Pérdida de información dispersa | Dificultad en seguimiento de reservas y pagos |
| Sin control de estados de reserva | Confusión en personal y clientes |
| Reportes manuales lentos | Toma de decisiones tardía |
| Escalabilidad limitada | No soporta crecimiento del negocio |
| Sin auditoría de cambios | Imposible rastrear quién hizo qué y cuándo |

---

## 🎯 Objetivo General

Desarrollar e implementar un **sistema web** en **Java SE + Oracle Database** con arquitectura **REST + MVC + DAO** que automatice el proceso de reservas, controle la disponibilidad en tiempo real, genere reportes de gestión, integre notificaciones por email y proporcione un panel de administración web accesible desde cualquier navegador.

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java SE 8+ |
| Interfaz de usuario | HTML5 + CSS3 + JavaScript + Bootstrap 5 |
| Servidor web | `com.sun.net.httpserver.HttpServer` (embebido) |
| Base de datos | Oracle Database XE 11g o superior |
| Conexión BD | JDBC — Oracle Thin Driver (ojdbc8.jar) |
| Arquitectura | REST + MVC + DAO |
| Seguridad | Hash SHA-256 para contraseñas |
| Email | JavaMail 1.6.2 (javax.mail.jar + activation.jar) + Gmail SMTP TLS |
| Build | `javac` + `java` directo (lib/ locales) |
| Pruebas | JUnit 4.x |
| IDE | VS Code (editor principal) |

---

## 📦 Módulos del Sistema

### 1. 🔐 Autenticación y Roles (Completado ✅)
- Login con hash SHA-256
- **Dos roles**: ADMIN (acceso total), GERENTE (gestión de espacios)
- Registro de auditoría de accesos
- Cambio de contraseña seguro
- Splash Screen animado con gradiente

### 2. 🏢 Gestión de Espacios (Completado ✅)
- CRUD de espacios: salas, aulas, auditorios, laboratorios, áreas comunes
- Campos: capacidad, ubicación, precio por hora, estado activo/inactivo
- Eliminación lógica (estado INACTIVO)
- Tabla con filtro en vivo (TableRowSorter)
- Tooltips en todos los botones

### 3. 👥 Gestión de Clientes (Completado ✅)
- CRUD de clientes con datos personales y de contacto
- Unicidad por DNI y correo electrónico
- Búsqueda por DNI con filtración en tiempo real
- Validaciones avanzadas

### 4. 📅 Gestión de Reservas (Completado ✅)
- Verificación automática de disponibilidad (sin solapamientos)
- Cálculo automático del monto total (precio/hora × horas)
- Flujo de estados: `PENDIENTE → CONFIRMADA → COMPLETADA / CANCELADA`
- Vista en tarjetas con información completa: **nombre y datos del cliente**, espacio, fechas, duración, monto
- Badges de color por estado (amarillo=PENDIENTE, verde=CONFIRMADA, azul=COMPLETADA, rojo=CANCELADA)
- Validación automática de fechas y solapamientos
- Bloqueo de horarios para mantenimiento o eventos privados
- Al completar reserva → se crea pago PENDIENTE automáticamente

### 5. 📊 Reportes y Exportación (Completado ✅)
- Ingresos mensuales por período
- Ocupación por espacio en rango de fechas
- Distribución de estados de reservas
- Exportación a CSV de cualquier reporte
- Visualización directa en la interfaz gráfica

### 6. 📈 Dashboard Ejecutivo (Completado ✅)
- Panel principal con 4 KPIs:
  - Ingresos del mes actual
  - Espacios disponibles (estado ACTIVO)
  - Reservas pendientes (estado PENDIENTE)
  - Porcentaje de ocupación promedio
- Carga de datos en hilo separado (SwingWorker)
- Interfaz card-based con colores corporativos

### 7. 📋 Auditoría y Validación (Completado ✅)
- Registro automático de cambios en estados de reservas
- Tabla AUDITORIA con timestamps y usuario responsable
- Validación automática de solapamientos
- Prevención de doble reserva en mismo espacio/hora
- Horarios bloqueados para mantenimiento

### 8. 🎨 Sistema de Temas Visual (Completado ✅)
- UITheme centralizado con 15+ colores corporativos
- 9 variantes de fuentes
- Métodos factory para componentes estilizados
- Look & Feel Nimbus multiplataforma
- Cohesión visual en todas las vistas

### 9. 💳 Gestión de Pagos (Completado ✅)
- Tabla de pagos con nombre del cliente, monto, método y estado
- Métodos: EFECTIVO, TARJETA, TRANSFERENCIA, YAPE/PLIN
- Estados: PENDIENTE, COMPLETADO, RECHAZADO, REEMBOLSADO
- Aplicación de códigos de descuento al momento del pago
- Cálculo automático del monto final con descuento
- Al confirmar pago → **dispara automáticamente 2 correos**:
  1. ✅ **Email de confirmación de pago** (monto, método, n° reserva)
  2. ⭐ **Email de solicitud de evaluación** con estrellas 1-5 (token UUID único)

### 10. 🛠️ Gestión de Equipos (Completado ✅)
- CRUD de equipamiento disponible en espacios
- Control de cantidad y estado de equipos
- Estados: ACTIVO, MANTENIMIENTO, INACTIVO
- Asignación a espacios específicos
- Registro de fecha de adquisición

### 11. ⏰ Gestión de Horarios (Completado ✅)
- Configuración de horarios por espacio
- Horarios por día de la semana (0-6)
- Hora de apertura y cierre
- Activación/desactivación de horarios
- Control de disponibilidad por franja horaria

### 12. 🎁 Gestión de Descuentos y Promociones (Completado ✅)
- Códigos promocionales con descuentos porcentuales
- Límite de uso por código
- Monto mínimo para aplicar descuento
- Fechas de vigencia (inicio/fin)
- Validación automática de disponibilidad

### 13. ⭐ Sistema de Evaluaciones por Email (Completado ✅)
- Calificaciones de 1 a 5 estrellas por reserva
- **Comentario opcional** del cliente al momento de calificar
- Registro de fecha de evaluación
- Vinculación con **cliente** (id_cliente → CLIENTES) y reserva
- Tokens UUID de un solo uso almacenados en TOKENS_EVALUACION (válidos 30 días)
- Email automático al completar pago con enlace directo a la evaluación
- Endpoint público `GET /evaluaciones/formulario?token=xxx` (sin login requerido)
- Al hacer clic en una estrella: valida token → guarda calificación + comentario → marca token como utilizado
- Panel de administración con tabla de evaluaciones recibidas
- Promedio de calificación calculado por espacio

### 14. 📬 Sistema de Notificaciones (Completado ✅)
- Notificaciones por tipo: RESERVA, PAGO, RECORDATORIO, PROMOCION, SISTEMA, EVALUACION
- Al completar un pago se genera automáticamente un registro de notificación tipo EVALUACION
- Control de lectura (leído/no leído) con fecha de lectura
- Historial de notificaciones por usuario
- Módulo sin botones de editar/eliminar — solo acción de **envío manual único**
- El botón "Enviar evaluación" envía el correo con estrellas al cliente una sola vez
- Soporte para auditoría de comunicaciones enviadas

---

## 🗃️ Base de Datos — Oracle Database

La base de datos contiene **12 tablas normalizadas** hasta 3FN con secuencias Oracle para claves primarias y triggers para auditoría automática.

### Tablas principales

| Tabla | Descripción | Estado |
|-------|-------------|--------|
| `USUARIOS` | Credenciales y roles del sistema | ✅ |
| `ESPACIOS` | Espacios disponibles para reserva | ✅ |
| `CLIENTES` | Información personal de clientes | ✅ |
| `RESERVAS` | Reservas con estados y montos | ✅ |
| `PAGOS` | Pagos por reserva (tarjeta, transferencia, efectivo) | ✅ |
| `EQUIPOS` | Equipamiento disponible en espacios | ✅ |
| `HORARIOS` | Horarios de apertura/cierre por día y espacio | ✅ |
| `DESCUENTOS` | Códigos promocionales y descuentos | ✅ |
| `EVALUACIONES` | Calificaciones de clientes (id_cliente FK → CLIENTES) | ✅ |
| `TOKENS_EVALUACION` | Tokens UUID para evaluar desde email (sin login) | ✅ |
| `NOTIFICACIONES` | Notificaciones a usuarios (reserva, pago, recordatorio, etc) | ✅ |
| `AUDITORIA` | Registro automático de operaciones críticas | ✅ |

### Objetos Oracle utilizados
- **SEQUENCE**: generación automática de claves primarias
- **FOREIGN KEYS**: integridad referencial
- **CHECK CONSTRAINTS**: validación de reglas de negocio
- **PreparedStatement**: protección contra inyección SQL
- **DatabaseInitializer**: crea tablas faltantes automáticamente al startup

---

## 🔄 Flujo de Estados de una Reserva

```
(creación) ──► PENDIENTE ──► CONFIRMADA ──► COMPLETADA
                   │               │
                   └───────────────┴──► CANCELADA
```

---

## 🚀 Configuración e Instalación

### Requisitos Previos
- JDK 8 o superior
- Apache Maven 3.6+ (opcional, para compilación offline)
- Oracle Database XE (o superior) en localhost:1521:XE
- ojdbc8.jar (incluido en carpeta lib/)
- Windows, Linux o macOS

### Opción 1: Ejecutar desde IntelliJ IDEA / NetBeans / VS Code

1. **Importar el proyecto** (File → Open → seleccionar carpeta)
2. **Verificar configuración en `Conexion.java`**:
   ```java
   private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
   private static final String USER = "spacework";
   private static final String PASSWORD = "spacework123";
   ```
3. **Ejecutar scripts SQL** (ver documentación en INSTALACION.md Paso 6B):
   - Crear tablas: 01_crear_tablas.sql
   - Crear secuencias: 02_crear_secuencias.sql
   - Crear triggers: 03_crear_triggers.sql
   - Datos iniciales: 04_datos_iniciales.sql
   - **Nuevas características**: 05_agregar_roles_y_auditoria.sql (opcional, se crean automáticamente)

4. **Ejecutar Main.java** o desde IDE → Run

### Opción 2: Compilar y Ejecutar desde Terminal (PowerShell) ✅ RECOMENDADO

```powershell
# 1. Compilar todos los fuentes Java
$cp = "lib/ojdbc8.jar;lib/javax.mail.jar;lib/activation.jar"
javac -encoding UTF-8 -cp "target/classes;$cp" -d target/classes `
  (Get-ChildItem -Recurse -Path "src/main/java" -Filter "*.java" | Select-Object -ExpandProperty FullName)

# 2. Ejecutar el servidor en el puerto 8080
java -cp "target/classes;lib/ojdbc8.jar;lib/javax.mail.jar;lib/activation.jar" com.spacework.Main
```

> **Nota:** Maven no es necesario. El proyecto usa JARs locales en la carpeta `lib/`.

---

## 🔑 Credenciales Predeterminadas

```
Usuario: admin
Contraseña: admin123
Rol: ADMIN (acceso total)
```

Este usuario es creado automáticamente en el script `04_datos_iniciales.sql`.

---

## 📁 Estructura del Proyecto

```
SistemaReservas/
├── pom.xml                          # Configuración Maven
├── INSTALACION.md                   # Guía de instalación BD (6 Pasos)
├── GUIA_TECNICA.md                  # Documentación técnica de componentes
├── PLAN_DESARROLLO.md               # Fases completadas
├── README.md                         # Este archivo
├── lib/
│   ├── ojdbc8.jar                   # Driver Oracle JDBC
│   ├── javax.mail.jar               # JavaMail 1.6.2 (Gmail SMTP)
│   ├── activation.jar               # Activation 1.1.1 (requerido por JavaMail)
│   ├── junit-4.13.2.jar             # JUnit testing
│   └── hamcrest-core-1.3.jar        # Hamcrest matchers
├── sql/
│   ├── 00_crear_usuario.sql         # Crear usuario spacework
│   ├── 01_crear_tablas.sql          # Crear 14 tablas (completas)
│   ├── 02_crear_secuencias.sql      # Crear SEQ_* para todas las tablas
│   ├── 03_crear_triggers.sql        # Crear triggers de auditoría
│   ├── 04_datos_iniciales.sql       # Admin user + datos de prueba
│   └── 05_agregar_roles_y_auditoria.sql  # Configuración avanzada (auto)
├── src/main/java/com/spacework/
│   ├── Main.java                    # Punto de entrada
│   ├── controller/                  # 11 controladores MVC + REST handlers
│   │   ├── AuthController.java
│   │   ├── ClienteController.java
│   │   ├── EspacioController.java
│   │   ├── ReservaController.java
│   │   ├── PagoController.java      # completarPago() genera token + envía email
│   │   ├── EquipoController.java
│   │   ├── HorarioController.java
│   │   ├── DescuentoController.java
│   │   ├── EvaluacionController.java
│   │   ├── NotificacionController.java
│   │   ├── ReporteController.java
│   │   ├── ValidacionController.java
│   │   ├── AuditoriaController.java
│   │   └── api/
│   │       ├── ...RestController.java       # controladores REST existentes
│   │       └── EvaluacionFormularioHandler.java # NUEVO — recibe click estrella email
│   ├── dao/                         # 15 DAOs (acceso BD)
│   │   ├── UsuarioDAO.java
│   │   ├── ClienteDAO.java
│   │   ├── EspacioDAO.java
│   │   ├── ReservaDAO.java          # + buscarPorId() agregado
│   │   ├── PagoDAO.java
│   │   ├── EquipoDAO.java
│   │   ├── HorarioDAO.java
│   │   ├── DescuentoDAO.java
│   │   ├── EvaluacionDAO.java
│   │   ├── TokenEvaluacionDAO.java  # NUEVO — tokens para evaluar por email
│   │   ├── NotificacionDAO.java
│   │   ├── AuditoriaDAO.java
│   │   ├── ReporteDAO.java
│   │   ├── HorarioBloqueadoDAO.java
│   │   └── RolDAO.java
│   ├── model/                       # 15 modelos de datos
│   │   ├── Usuario.java
│   │   ├── Cliente.java
│   │   ├── Espacio.java
│   │   ├── Reserva.java
│   │   ├── Pago.java
│   │   ├── Equipo.java
│   │   ├── Horario.java
│   │   ├── Descuento.java
│   │   ├── Evaluacion.java          # usa idCliente (FK → CLIENTES)
│   │   ├── TokenEvaluacion.java     # NUEVO — modelo de token UUID por email
│   │   ├── Notificacion.java
│   │   ├── AuditoriaLog.java
│   │   ├── HorarioBloqueado.java
│   │   ├── Rol.java
│   │   └── ReporteData.java
│   └── util/                        # 7 utilidades
│       ├── Conexion.java            # Conexión Oracle JDBC
│       ├── HashUtil.java            # SHA-256 hashing
│       ├── UITheme.java             # Sistema de temas Swing
│       ├── CSVExporter.java         # Exportador CSV
│       ├── DatabaseInitializer.java # Auto-inicialización BD
│       └── MailService.java         # Envío de correos Gmail SMTP (evaluaciones)
│   └── view/                        # 14 vistas Swing
│       ├── LoginForm.java           # Pantalla login
│       ├── MainFrame.java           # Ventana principal (JTabbedPane)
│       ├── SplashScreen.java        # Pantalla inicio
│       ├── DashboardPanel.java      # Panel KPIs
│       ├── EspacioPanel.java        # CRUD espacios
│       ├── ClientePanel.java        # CRUD clientes
│       ├── ReservaPanel.java        # CRUD + flujo reservas
│       ├── PagoPanel.java                   # NUEVO - Gestión pagos
│       ├── EquipoPanel.java                 # NUEVO - Control equipos
│       ├── HorarioPanel.java                # NUEVO - Configuración horarios
│       ├── DescuentoPanel.java              # NUEVO - Códigos promocionales
│       ├── EvaluacionPanel.java             # NUEVO - Calificaciones usuarios
│       ├── NotificacionPanel.java           # NUEVO - Inbox notificaciones
│       ├── ReportePanel.java        # Reportes + CSV
│       ├── HorarioBloqueadoPanel.java       # Gestión bloqueos horarios
│       ├── BloquearHorarioDialog.java       # Modal para crear bloqueos
│       └── CambioContraseñaDialog.java      # Modal cambio password
├── src/test/java/com/spacework/
│   ├── EspacioDAOTest.java
│   ├── ClienteDAOTest.java
│   ├── ReservaDAOTest.java
│   ├── PagoDAOTest.java                     # NUEVO
│   ├── EquipoDAOTest.java                   # NUEVO
│   └── NotificacionDAOTest.java             # NUEVO
└── target/                          # Compilados (generado por Maven)
    └── classes/
        └── com/spacework/...
```

---

## 📐 Arquitectura MVC + DAO

```
com.spacework/
├── model/          → Entidades (Usuario, Espacio, Cliente, Reserva, Rol, HorarioBloqueado, AuditoriaLog)
├── dao/            → Acceso a datos (8 DAOs para operaciones CRUD)
├── controller/     → Lógica de negocio, validación, auditoría (6 controllers)
├── view/           → Formularios y ventanas Java Swing (8 panels/dialogs)
└── util/           → Conexión BD, UITheme, utilidades transversales
```

**Patrón**: Singleton para AuthController, Dependency Injection en controllers, SwingWorker para operaciones asincrónicas en UI.

---

## ✅ Características Completadas

- [x] Autenticación con hash SHA-256
- [x] Roles y control de acceso (ADMIN, GERENTE)
- [x] CRUD en tiempo real para: espacios, clientes, reservas
- [x] Validación automática de solapamientos (doble reserva)
- [x] Bloqueo de horarios para mantenimiento
- [x] Dashboard con 4 KPIs
- [x] Cambio de contraseña seguro
- [x] Auditoría de cambios
- [x] Exportación de reportes a CSV
- [x] Diseño moderno con UITheme Nimbus
- [x] Filtros en vivo en todas las tablas
- [x] Selección fecha/hora con JSpinner
- [x] Estados de reservas con colores
- [x] Splash Screen animado
- [x] Tolerante a offline (sin internet en red corporativa)
- [x] Compilación Maven offline-ready
- [x] Tests unitarios con JUnit 4
- [x] Documentación técnica completa
- [x] **NUEVO: Gestión integral de pagos** (métodos: tarjeta, transferencia, efectivo)
- [x] **NUEVO: Control de equipos y recursos** por espacio
- [x] **NUEVO: Horarios operacionales** configurables por día y espacio
- [x] **NUEVO: Sistema de descuentos y promociones** con códigos
- [x] **NUEVO: Evaluaciones de usuarios** (calificaciones 1-5 estrellas)
- [x] **NUEVO: Sistema de notificaciones** por tipo (reserva, pago, recordatorio, etc)
- [x] **NUEVO: Evaluaciones por email con token UUID** (TOKENS_EVALUACION)
- [x] **NUEVO: MailService con Gmail SMTP** (JavaMail 1.6.2, App Password)
- [x] **NUEVO: EvaluacionFormularioHandler** — endpoint /evaluaciones/formulario sin login
- [x] **NUEVO: Flujo completo** pago → email → clic estrella → evaluación registrada en BD

---

## 🆕 Nuevas Características (Ampliación de Funcionalidades)

### 💳 Gestión Integral de Pagos
- CRUD de pagos por reserva
- Tres métodos soportados: **TARJETA**, **TRANSFERENCIA**, **EFECTIVO**
- Estados de pago: PENDIENTE, COMPLETADO, RECHAZADO, REEMBOLSADO
- Validación automática de montos
- Registro de fecha de pago
- Tabla PAGOS vinculada a RESERVAS

### 🛠️ Control de Equipos y Recursos
- Asignación de equipamiento a espacios específicos
- Control de cantidad disponible
- Estados: ACTIVO, MANTENIMIENTO, INACTIVO
- Seguimiento de fecha de adquisición
- Tabla EQUIPOS para inventario centralizado

### ⏰ Horarios Operacionales Configurables
- Definición de horarios por día de la semana (Monday-Sunday)
- Control de hora de apertura y cierre
- Activación/desactivación flexible
- Integración con sistema de disponibilidad
- Tabla HORARIOS por espacio y día

### 🎁 Sistema de Descuentos y Promociones
- Códigos promocionales con porcentajes personalizables
- Límite configurable de usos por código
- Monto mínimo para aplicar descuento
- Control de vigencia (fecha inicio/fin)
- Validación automática en creación de reservas
- Tabla DESCUENTOS para administración

### ⭐ Evaluaciones y Calificaciones (Por Email)
- Calificaciones de 1 a 5 estrellas por reserva
- Comentarios opcionales del cliente
- Cálculo automático de promedio por espacio
- Tabla EVALUACIONES vinculada a `id_cliente` (FK → CLIENTES, **no** USUARIOS)
- Tokens UUID de un solo uso — tabla TOKENS_EVALUACION (válidos 30 días)
- Email HTML automático al completar pago con 5 links de estrella
- `GET /evaluaciones/formulario?token=xxx&calificacion=N` registra la evaluación en BD
- Token marcado como utilizado al evaluar (no reutilizable)
- Vista EvaluacionPanel en modo solo-lectura (sin formulario CRUD)

### 📬 Sistema de Notificaciones
- Notificaciones por tipo: RESERVA, PAGO, RECORDATORIO, PROMOCION, SISTEMA
- Control de lectura con timestamps
- Inbox personal por usuario
- Integración con eventos de negocio
- Tabla NOTIFICACIONES para auditoría de comunicaciones

---

## 🗃️ Ampliación de Base de Datos

**De 5 tablas originales a 12 tablas expandidas:**

| Originales | Nuevas | Total |
|----------|--------|-------|
| USUARIOS, ESPACIOS, CLIENTES, RESERVAS, AUDITORIA | PAGOS, EQUIPOS, HORARIOS, DESCUENTOS, EVALUACIONES, NOTIFICACIONES, TOKENS_EVALUACION | **12 tablas** |

Todas las tablas incluyen:
- Claves primarias con secuencias Oracle
- Foreign keys para integridad referencial
- CHECK constraints para validación
- Índices para rendimiento
- Triggers para auditoría automática

---

**¿QUÉ SON?** Períodos de tiempo en los que un espacio NO está disponible para reservas, aún cuando técnicamente exista.

**¿PARA QUÉ SIRVEN?**

| Caso | Ejemplo |
|------|---------|
| **Mantenimiento** | Lunes 10:00-12:00 → Sala A se limpia y se revisan equipos |
| **Eventos privados** | Viernes 18:00-22:00 → Auditorio reservado para evento corporativo (no se alquila) |
| **Reuniones internas** | Martes 09:00-10:00 → Directorio se reúne en Sala B |
| **Cierre de emergencia** | Jueves 14:00-17:00 → Aire acondicionado se daña, espacio inutilizable |
| **Desinfección** | Diario 08:00-08:30 → Personal de limpieza desinfecta antes de abrir |

**¿CÓMO FUNCIONA?**

1. **Ir a pestaña "⏱️ Horarios Bloqueados"**
2. **Hacer clic "Agregar Bloqueo"**
3. **Ingresar**:
   - Espacio: (seleccionar cuál)
   - Fecha inicio: (JSpinner con fecha/hora)
   - Fecha fin: (JSpinner con fecha/hora)
   - Razón: (descripción del bloqueo)
4. **Guardar**

**¿QUÉ PASA CUANDO HAY UN BLOQUEO?**

Cuando un usuario intenta crear una **reserva en un horario bloqueado**:
```
Usuario: Quiero Sala A el viernes 18:00-20:00
Sistema: ❌ "Existe un bloqueo activo en este horario: Evento privado"
Resultado: La reserva es RECHAZADA automáticamente
```

**TABLA EN BD**: `HORARIOS_BLOQUEADOS`
```
id_bloqueo → 1
id_espacio → 2 (Auditorio)
fecha_inicio → 2026-04-11 18:00:00
fecha_fin → 2026-04-11 22:00:00
razon → "Evento corporativo"
usuario_creador → "admin"
fecha_creacion → 2026-04-04 14:30:00
```

---

## ✨ Lo que hace este sistema único

1. **Offline-Ready**: Funciona perfectamente sin internet (para red corporativa)
2. **Auto-Inicialización BD**: Si faltan tablas, se crean automáticamente
3. **Auditoría Automática**: Cada cambio en reservas es registrado
4. **Validación Inteligente**: Previene solapamientos y errores comunes
5. **Dashboard KPIs**: Visualización ejecutiva en tiempo real
6. **Exportación CSV**: Reportes descargables para Excel
7. **Diseño Moderno**: UITheme centralizado para consistencia visual
8. **Roles Flexibles**: Prepara para control de acceso granular

---

## ❌ Limitaciones Conocidas (versión 1.0)

- Diseñado para una sola sede
- Sin módulo de pagos en línea
- Sin interfaz web ni app móvil
- Sin notificaciones automáticas (email/SMS)
- Sin integración SUNAT para facturación

---

## 🐛 Troubleshooting

### Error: "ORA-01950: no privileges on tablespace"
```sql
GRANT UNLIMITED TABLESPACE TO spacework;
```

### Error: "java.sql.SQLException: IO Error: Connection refused"
Verifica que Oracle esté corriendo:
```bash
# Windows
sqlplus system@XE   # Ingresa como system para verificar

# Oracle debe estar en puerto 1521
```

### Error: "Cannot find ojdbc8.jar"
Asegurate que esté en `lib/` y configurado en el IDE

---

## 📞 Soporte y Documentación

- **INSTALACION.md**: Pasos 1-7 para configurar BD desde cero
- **GUIA_TECNICA.md**: Descripción de las 11 secciones técnicas
- **PLAN_DESARROLLO.md**: Fases de desarrollo 1-10 (todas completadas)
- **pom.xml**: Dependencias Maven disponibles si el sistema antivirus/proxy lo permite

---

## 📝 Log de Cambios

### Versión 1.0 (Actual)
- Control completar de reservas con auditoría
- Dashboard de KPIs
- Gestión de roles
- Horarios bloqueados
- Cambio de contraseña
- Exportación CSV
- Validación avanzada
- DatabaseInitializer para auto-setup

### Mejoras Futuras
- Panel de administración de roles (UI)
- Reportes PDF
- Interfaz web (Spring Boot)
- App móvil (Android)

---

> **Universidad Tecnológica del Perú — Curso Integrador I: Sistemas · 2024**  
> Desarrollo completado: 2024  
> Sistema de Gestión de Reservas · SpaceWork Perú S.A.C.
