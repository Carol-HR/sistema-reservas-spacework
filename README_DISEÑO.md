# 📋 SPACEWORK - Sistema de Gestión de Reservas de Espacios

## 🎯 Descripción General
**SPACEWORK** es una plataforma web completa para la gestión de reservas de espacios con funcionalidades avanzadas de calendario, pagos, evaluaciones y notificaciones. Sistema SPA (Single Page Application) con arquitectura cliente-servidor en tiempo real.

---

## 🏗️ Arquitectura del Proyecto

### Stack Tecnológico
- **Frontend**: HTML5 + CSS3 + JavaScript (Bootstrap 5)
- **Backend**: Java SE 8 + HttpServer embebido
- **Base de Datos**: Oracle 11g XE
- **Email**: JavaMail 1.6.2 (SMTP Gmail TLS)
- **Patrón**: MVC + DAO

### Estructura General
```
SistemaReservas/
├── src/main/java/com/spacework/
│   ├── Main.java                    (Punto de entrada)
│   ├── SpaceWorkApplication.java    (Servidor & Rutas REST)
│   ├── controller/                  (Lógica de presentación)
│   ├── dao/                         (Acceso a datos)
│   ├── model/                       (Entidades)
│   ├── util/                        (Utilidades)
│   └── ...
├── src/main/resources/
│   ├── application.properties       (Config BD)
│   ├── mail.properties              (Config email)
│   └── static/
│       ├── index.html               (SPA principal)
│       ├── css/style.css
│       └── js/app.js                (Lógica frontend)
├── sql/                             (Scripts DDL/DML)
├── pom.xml                          (Dependencias Maven)
└── README_DISEÑO.md                 (Este archivo)
```

---

## 📦 MÓDULOS DEL SISTEMA

### 1. 🔐 AUTENTICACIÓN & USUARIOS
**Descripción**: Sistema de login y gestión de sesiones
- **Funcionalidades**:
  - Login con usuario/contraseña
  - Validación de credenciales
  - Sesión persistente (localStorage)
  - Logout
  - Control de acceso basado en roles (ADMIN, USER)

**Entidades BD**:
- `USUARIOS` (id_usuario, username, contraseña, email, estado)

**Endpoints API**:
- `POST /api/auth/login` → Autenticar usuario

**Flujo de UI**:
```
[Pantalla Login] → Validar credenciales → [Dashboard]
```

---

### 2. 📊 DASHBOARD
**Descripción**: Panel principal con KPIs y visión general del sistema

**Widgets**:
1. **KPIs Principales**
   - Total Reservas (con completadas)
   - Reservas Confirmadas
   - Espacios Activos
   - Ocupación (%)
   - Total Clientes
   - Ingresos Totales (S/.)

2. **📅 Calendario Semanal Interactivo**
   - Vista por espacio
   - Bloques de 1 hora (08:00-18:00)
   - 7 días adelante
   - Colores:
     - 🟢 Verde: Disponible
     - 🔴 Rojo: Ocupado (reserva activa)
     - 🟡 Amarillo: Bloqueado (mantenimiento)
   - Filtro por espacio

**Endpoints API**:
- `GET /api/calendario/semanal` → Ocupación por hora/espacio

**Datos Mostrados**:
```
Espacio     | Lun 10  | Mar 11  | Mié 12  | ...
────────────┼─────────┼─────────┼─────────┼────
Sala 1      | 08-09   | 🟢      | 🔴      | 🟢
            | 09-10   | 🟢      | 🟢      | 🟡
            | 10-11   | 🟡      | 🟢      | 🟢
────────────┼─────────┼─────────┼─────────┼────
Auditorio   | 08-09   | 🔴      | 🟢      | 🟢
```

---

### 3. 📅 RESERVAS
**Descripción**: Gestión completa del ciclo de vida de reservas

**Funcionalidades**:
- Crear nueva reserva (cliente + espacio + fecha/hora)
- Listar todas las reservas con filtros
- Confirmar reserva (PENDIENTE → CONFIRMADA)
- Completar reserva (CONFIRMADA → COMPLETADA)
- Cancelar reserva (cualquier estado → CANCELADA)
- Validar disponibilidad en tiempo real
- Cálculo automático de monto total

**Estados**:
- `PENDIENTE` → Espera aprobación
- `CONFIRMADA` → Confirmada, pendiente de realizarse
- `COMPLETADA` → Realizada, genera pago automático
- `CANCELADA` → Cancelada

**Entidades BD**:
- `RESERVAS` (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
- `CLIENTES` (id_cliente, nombre, apellido, email, dni, telefono)
- `ESPACIOS` (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)

**Endpoints API**:
- `GET /api/reservas` → Listar todas
- `POST /api/reservas` → Crear nueva
- `PUT /api/reservas/{id}/confirmar` → Confirmar
- `PUT /api/reservas/{id}/completar` → Completar
- `DELETE /api/reservas/{id}` → Cancelar
- `GET /api/horarios` → Obtener bloques horarios ocupados

**Validaciones**:
- ✅ Fecha fin > Fecha inicio
- ✅ Espacio disponible (no solapamiento con otras reservas)
- ✅ Espacio no tiene horario bloqueado
- ✅ Cliente debe existir

---

### 4. 🏛️ ESPACIOS
**Descripción**: Catálogo y gestión de espacios disponibles

**Funcionalidades**:
- Crear espacio (nombre, tipo, capacidad, ubicación, precio/hora)
- Listar espacios con filtros
- Editar información de espacio
- Eliminar espacio (lógico, cambia estado)
- Búsqueda por nombre/tipo

**Entidades BD**:
- `ESPACIOS` (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, estado, fecha_creacion)

**Tipos de Espacios**:
- Sala de Reuniones
- Auditorio
- Sala de Capacitación
- Oficina
- Espacio Abierto
- Otros

**Endpoints API**:
- `GET /api/espacios` → Listar todos
- `GET /api/espacios/{id}` → Obtener detalle
- `POST /api/espacios` → Crear
- `PUT /api/espacios/{id}` → Actualizar
- `DELETE /api/espacios/{id}` → Eliminar

---

### 5. 👥 CLIENTES
**Descripción**: Gestión de información de clientes

**Funcionalidades**:
- Crear cliente (nombre, apellido, DNI, email, teléfono)
- Listar clientes con búsqueda
- Editar datos de cliente
- Eliminar cliente (lógico)
- Búsqueda por DNI/email/nombre

**Entidades BD**:
- `CLIENTES` (id_cliente, nombre, apellido, dni, email, telefono, estado, fecha_alta)

**Endpoints API**:
- `GET /api/clientes` → Listar todos
- `GET /api/clientes/{id}` → Obtener detalle
- `POST /api/clientes` → Crear
- `PUT /api/clientes/{id}` → Actualizar
- `DELETE /api/clientes/{id}` → Eliminar

---

### 6. ⏱️ HORARIOS BLOQUEADOS
**Descripción**: Bloquear espacios en fechas/horas específicas (mantenimiento, eventos, etc.)

**Funcionalidades**:
- Crear bloqueo de horario (espacio + fecha_inicio + fecha_fin + razón)
- Listar bloques horarios
- Desbloquear horario (eliminar registro)
- Visualizar en calendario (color amarillo)
- Previene creación de reservas en esas horas

**Entidades BD**:
- `HORARIOS_BLOQUEADOS` (id_bloqueo, id_espacio, fecha_inicio, fecha_fin, razon, usuario_creador, fecha_creacion)

**Razones Comunes**:
- Mantenimiento
- Limpieza profunda
- Evento especial
- Reparación
- Desinfección

**Endpoints API**:
- `GET /api/horarios` → Listar bloques (JOIN con nombres espacios)
- `POST /api/horarios` → Crear bloqueo
- `DELETE /api/horarios/{id}` → Desbloquear

**Validación en Reservas**:
- Si espaciotienehorariobloqueado en rango solicitado → ❌ Rechazar

---

### 7. 💳 PAGOS
**Descripción**: Gestión de pagos de reservas completadas

**Funcionalidades**:
- Pagos se crean automáticamente al completar reserva
- Mostrar pagos pendientes
- Procesar pago (seleccionar método + aplicar descuento)
- Enviar confirmación por email
- Crear notificación de pago
- Crear EVALUACIÓN (no enviar email aún)

**Estados de Pago**:
- `PENDIENTE` → Espera pago
- `COMPLETADO` → Pagado exitosamente
- `RECHAZADO` → Error en transacción

**Métodos de Pago**:
- 💵 Efectivo
- 💳 Tarjeta Crédito/Débito
- 🏦 Transferencia Bancaria
- 📱 Yape / Plin

**Entidades BD**:
- `PAGOS` (id_pago, id_reserva, monto, monto_final, metodo_pago, estado_pago, id_descuento, fecha_creacion, fecha_pago)

**Endpoints API**:
- `GET /api/pagos` → Listar pendientes
- `PUT /api/pagos/{id}/pagar` → Procesar pago (envía email + crea notificación)
- `POST /api/descuentos/validar` → Validar código descuento

**Flujo de Pago**:
```
Reserva COMPLETADA
    ↓
Crear PAGO (PENDIENTE)
    ↓
Mostrar en módulo Pagos
    ↓
Click "Pagar"
    ↓
Seleccionar método + aplicar descuento (opcional)
    ↓
Confirmar
    ↓
Enviar email confirmación
    ↓
Crear NOTIFICACIÓN (PAGO)
    ↓
Crear EVALUACIÓN (sin email)
    ↓
PAGO = COMPLETADO
```

---

### 8. 🎁 DESCUENTOS
**Descripción**: Códigos de descuento para pagos

**Funcionalidades**:
- Crear código (PROMO25, VERANO50, etc.)
- Configurar porcentaje y rango de fechas
- Establecer monto mínimo
- Limitar usos (máximo X veces)
- Activar/desactivar
- Validar código al pagar

**Entidades BD**:
- `DESCUENTOS` (id_descuento, codigo, descripcion, porcentaje, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado)

**Endpoints API**:
- `GET /api/descuentos` → Listar
- `POST /api/descuentos` → Crear
- `PUT /api/descuentos/{id}` → Actualizar
- `DELETE /api/descuentos/{id}` → Desactivar
- `POST /api/descuentos/validar` → Validar código (usado en pago)

**Validaciones**:
- ✅ Código válido y activo
- ✅ Fecha dentro de rango vigente
- ✅ Monto ≥ monto_minimo
- ✅ Usos < usos_maximos

---

### 9. ⭐ EVALUACIONES
**Descripción**: Sistema de calificaciones y comentarios de clientes

**Funcionalidades**:
- Token único + seguro para formulario de evaluación
- Cliente accede a link en email → `http://localhost:8080/evaluaciones/formulario?token=xxx`
- Calificación de 1 a 5 estrellas
- Campo de comentario (feedback opcional)
- Guardar en BD
- Mostrar tabla de evaluaciones (nombre cliente + email + calificación + comentario + fecha)
- Filtrar por cliente/email
- Vista READ-ONLY (sin editar/eliminar)

**Entidades BD**:
- `EVALUACIONES` (id_evaluacion, id_reserva, id_cliente, calificacion, comentario, fecha_evaluacion)
- `TOKENS_EVALUACION` (id_token, id_pago, token (UUID), email_cliente, fecha_creacion, fecha_expiracion, utilizado)

**Endpoints API**:
- `GET /api/evaluaciones` → Lista con JOIN CLIENTES (nombre, email, apellido)
- `POST /api/evaluaciones` → Guardar evaluación (desde formulario público)
- `GET /evaluaciones/formulario?token=xxx` → Formulario público (GET → HTML)
- `POST /api/evaluaciones/enviar/{idNotificacion}` → Enviar email desde notificación

**Flujo de Evaluación**:
```
Pago completado
    ↓
Crear TOKENS_EVALUACION con UUID
    ↓
Crear NOTIFICACIÓN (tipo=EVALUACION, msg con token)
    ↓
En Notificaciones, click "📧 Enviar Evaluación"
    ↓
Enviar email con link: http://localhost:8080/evaluaciones/formulario?token=uuid
    ↓
Cliente hace click
    ↓
Formulario interactivo con estrellas + comentario
    ↓
Click en estrella → Guardar calificación
    ↓
Click enviar → POST /api/evaluaciones con token + calificacion + comentario
    ↓
Guardar en EVALUACIONES
    ↓
Marcar TOKENS_EVALUACION como utilizado
    ↓
Mostrar: "✅ ¡Gracias por tu evaluación!"
```

**Características Especiales**:
- 🌟 Estrellas interactivas (hover cambia color)
- 📝 Área de comentario con preview
- 🔗 Link válido solo 7 días (fecha_expiracion)
- 🔒 Token único (no reutilizable)
- 📧 Email personalizado con nombre cliente

---

### 10. 🔔 NOTIFICACIONES
**Descripción**: Centro de notificaciones para usuario

**Tipos de Notificaciones**:
- 📅 `RESERVA` → Nueva reserva creada
- 💳 `PAGO` → Pago procesado
- ⏰ `RECORDATORIO` → Recordatorio de reserva próxima
- 🎁 `PROMOCION` → Ofertas/descuentos
- ⚙️ `SISTEMA` → Mensajes del sistema
- ⭐ `EVALUACION` → Solicitud de evaluación

**Funcionalidades**:
- Listar notificaciones
- Filtrar por tipo
- Marcar como leída
- Enviar email de evaluación desde notificación
- Mostrar "✅ Ya enviado" si evaluación ya fue enviada
- Auto-deshabilitación de botón (anti-doble-envío)

**Entidades BD**:
- `NOTIFICACIONES` (id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion, fecha_leida)

**Endpoints API**:
- `GET /api/notificaciones` → Listar todas
- `PUT /api/notificaciones/{id}/leida` → Marcar como leída
- `POST /api/evaluaciones/enviar/{idNotificacion}` → Enviar evaluación (marca notif como leída)

**Formato de Mensaje**:
```json
EVALUACION: "Token: {UUID} | Email: {email@example.com} | Cliente: {nombre}"
PAGO: "Pago #{id} procesado por {metodo}. Monto: S/. {monto_final}"
RECORDATORIO: "Tu reserva #{id} es el {fecha}..."
```

---

### 11. 📧 EMAIL
**Descripción**: Sistema de envío de emails

**Emails Enviados**:
1. **Confirmación de Pago**
   - Trigger: Al procesar pago
   - Contenido: Monto, método, fecha, detalles reserva
   - Destinatario: email cliente

2. **Solicitud de Evaluación**
   - Trigger: Click "Enviar Evaluación" en notificaciones
   - Contenido: Link a formulario con token
   - Destinatario: email cliente
   - Formato: HTML con estilos

**Configuración**:
- SMTP: Gmail (smtp.gmail.com:587, TLS)
- Credenciales: `src/main/resources/mail.properties`
- Formato: HTML con CSS inline

---

### 12. 📊 AUDITORÍA & LOGS
**Descripción**: Registro de actividades del sistema

**Funcionalidades**:
- Registrar cambios en reservas
- Registrar pagos
- Registrar modificaciones de datos
- Filtrar por usuario/fecha/acción

**Entidades BD**:
- `AUDITORIA_LOG` (id_auditoria, id_usuario, accion, tabla, id_registro, valor_anterior, valor_nuevo, fecha)

---

## 🔄 FLUJOS PRINCIPALES

### Flujo 1: Crear Reserva → Pagar → Evaluar
```
┌─────────────────┐
│  RESERVAS       │
│ - Crear         │
│ - Confirmar     │
│ - Completar     │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│  PAGOS          │
│ - Auto-crear    │
│ - Procesar      │ ←─── Email confirmación
└────────┬────────┘
         │
         ↓
┌──────────────────┐
│ NOTIFICACIONES   │
│ - PAGO           │
│ - EVALUACION     │
└────────┬─────────┘
         │
         ↓
┌──────────────────┐
│ EVALUACIONES     │
│ - Token único    │
│ - Email link     │
│ - Formulario web │
│ - Guardar rating │
└──────────────────┘
```

### Flujo 2: Bloquear Espacio
```
┌───────────────────────────┐
│ HORARIOS BLOQUEADOS       │
│ - Fecha/Hora inicio/fin   │
│ - Razón (mantenimiento)   │
└────────┬──────────────────┘
         │
         ↓ Validación en:
┌───────────────────────────┐
│ RESERVAS (Crear)          │
│ - ¿Hay bloqueo en rango?  │
│ - SI → ❌ Rechazar        │
│ - NO → ✅ Permitir        │
└───────────────────────────┘
```

### Flujo 3: Dashboard - Visualizar Ocupación
```
┌────────────────────────────────┐
│ CALENDARIO SEMANAL             │
│ - 7 días adelante              │
│ - Bloques de 1 hora            │
│ - 1 fila por espacio           │
│ - Colores: Disponible/Ocupado/ │
│            Bloqueado           │
└────────┬───────────────────────┘
         │
         ↓ Query a:
         │
    ┌────┴────────────────────────┐
    │                             │
    ↓                             ↓
┌─────────────┐         ┌───────────────┐
│ RESERVAS    │         │ HORARIOS_     │
│ (CONFIRMADA │         │ BLOQUEADOS    │
│  COMPLETADA)│         │               │
└─────────────┘         └───────────────┘
```

---

## 📱 DIAGRAMA DE PANTALLAS

### Estructura del SPA
```
┌────────────────────────────────────────┐
│          NAVBAR (Navegación)           │
│  Logo | Módulos | Usuario | Logout    │
└────────┬─────────────────────────────┬─┘
         │                             │
    ┌────▼──────────────────────┐     │
    │  DASHBOARD (Home)         │     │
    │  ├─ KPIs                  │     │
    │  └─ Calendario Semanal    │     │
    │  └─ Disponibilidad Real   │     │
    └───────────────────────────┘     │
                                      │
    ┌──────────────────────────────┐  │
    │  RESERVAS                    │  │
    │  ├─ Lista (tarjetas)         │  │
    │  ├─ Crear nueva             │  │
    │  ├─ Confirmar/Completar     │  │
    │  ├─ Cancelar                │  │
    │  └─ Filtros                 │  │
    └──────────────────────────────┘  │
                                      │
    ┌──────────────────────────────┐  │
    │  ESPACIOS                    │  │
    │  ├─ Catálogo (tarjetas)      │  │
    │  ├─ Crear/Editar/Eliminar    │  │
    │  └─ Búsqueda                 │  │
    └──────────────────────────────┘  │
    ... otros módulos ...              │
    ┌──────────────────────────────┐  │
    │  NOTIFICACIONES              │  │
    │  ├─ Listar notificaciones    │  │
    │  ├─ Filtrar por tipo         │  │
    │  ├─ Marcar como leída        │  │
    │  └─ Enviar eval (especial)   │  │
    └──────────────────────────────┘  │
                                      │
└────────────────────────────────────────┘
```

---

## 🗄️ MODELO DE DATOS

### Relaciones Principales
```
USUARIOS (1) ─── (N) NOTIFICACIONES
  ↓
CLIENTES (1) ─── (N) RESERVAS
              └── (N) EVALUACIONES
                  └── (1) PAGOS
ESPACIOS  (1) ─── (N) RESERVAS
              └── (N) HORARIOS_BLOQUEADOS

RESERVAS  (1) ─── (1) PAGOS
          └── (1) EVALUACIONES

PAGOS     (1) ─── (N) TOKENS_EVALUACION
          └── (1) DESCUENTOS
```

---

## 🎨 PALETA DE COLORES

| Color | Uso |
|-------|-----|
| 🟢 Verde (#d4edda) | Disponible |
| 🔴 Rojo (#f8d7da) | Ocupado |
| 🟡 Amarillo (#fff3cd) | Bloqueado |
| 🔵 Azul (#0d6efd) | Activo/Confirmado |
| ⚪ Gris (#f0f0f0) | Inactivo/Hist |

---

## 🚀 ENDPOINTS API RESUMEN

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login |
| GET | `/api/calendario/semanal` | Ocupación semanal |
| GET | `/api/reservas` | Listar reservas |
| POST | `/api/reservas` | Crear reserva |
| PUT | `/api/reservas/{id}/confirmar` | Confirmar |
| PUT | `/api/reservas/{id}/completar` | Completar |
| DELETE | `/api/reservas/{id}` | Cancelar |
| GET | `/api/espacios` | Listar espacios |
| POST | `/api/espacios` | Crear espacio |
| PUT | `/api/espacios/{id}` | Editar espacio |
| DELETE | `/api/espacios/{id}` | Eliminar espacio |
| GET | `/api/clientes` | Listar clientes |
| POST | `/api/clientes` | Crear cliente |
| PUT | `/api/clientes/{id}` | Editar cliente |
| DELETE | `/api/clientes/{id}` | Eliminar cliente |
| GET | `/api/horarios` | Listar bloques horarios |
| POST | `/api/horarios` | Crear bloqueo |
| DELETE | `/api/horarios/{id}` | Desbloquear |
| GET | `/api/pagos` | Listar pagos pendientes |
| PUT | `/api/pagos/{id}/pagar` | Procesar pago |
| POST | `/api/descuentos/validar` | Validar descuento |
| GET | `/api/descuentos` | Listar descuentos |
| POST | `/api/descuentos` | Crear descuento |
| PUT | `/api/descuentos/{id}` | Editar descuento |
| DELETE | `/api/descuentos/{id}` | Desactivar descuento |
| GET | `/api/evaluaciones` | Listar evaluaciones |
| POST | `/api/evaluaciones` | Guardar evaluación |
| POST | `/api/evaluaciones/enviar/{id}` | Enviar email evaluación |
| GET | `/api/notificaciones` | Listar notificaciones |
| PUT | `/api/notificaciones/{id}/leida` | Marcar como leída |

---

## 📋 MÓDULOS ORDENADOS POR IMPORTANCIA

1. **AUTENTICACIÓN** (Login/Logout)
2. **RESERVAS** (Core del negocio)
3. **ESPACIOS** (Catálogo)
4. **DASHBOARD** (Visión general + Calendario)
5. **PAGOS** (Monetización)
6. **CLIENTES** (Base de datos de usuarios)
7. **HORARIOS BLOQUEADOS** (Validación)
8. **NOTIFICACIONES** (Comunicación)
9. **EVALUACIONES** (Feedback)
10. **DESCUENTOS** (Promociones)

---

## 🔒 VALIDACIONES CRÍTICAS

- ✅ Email válido (clientes/usuarios)
- ✅ Fechas: fin > inicio
- ✅ Overlapping: no solapamiento de reservas
- ✅ Bloques horarios: validar antes de crear reserva
- ✅ Disponibilidad: espacio + fecha + hora
- ✅ Token único: no reutilización de tokens evaluación
- ✅ Descuentos: vigencia, monto mínimo, usos restantes
- ✅ Estado pagos: solo procesar si PENDIENTE
- ✅ Notificaciones: tipo válido (enum)

---

**Versión**: 1.0  
**Última actualización**: Abril 2026  
**Autor**: Equipo SPACEWORK

