# INFORME 03 — Objetivos, Alcances, Limitaciones y Justificación
**SpaceWork | Universidad Tecnológica del Perú | 2026**

---

## 12. Definición de Objetivos del Proyecto

### 12.1 Objetivo General

Desarrollar e implementar un **sistema web de gestión de reservas de espacios** para SpaceWork Perú S.A.C., utilizando **Java SE 8**, **Oracle Database XE** y arquitectura **REST + MVC + DAO**, que automatice el ciclo completo de operaciones: desde la reserva y disponibilidad de espacios hasta el pago, generación de evaluaciones por email y visualización de indicadores de gestión en tiempo real, eliminando los procesos manuales actuales y mejorando la eficiencia operativa y la experiencia del cliente.

---

### 12.2 Objetivos Específicos

| # | Objetivo Específico | Módulo Asociado | Indicador de Éxito |
|---|--------------------|-----------------|--------------------|
| OE-01 | Implementar un módulo de autenticación seguro con roles (ADMIN, GERENTE) y hash SHA-256 | Autenticación | Login funcional, contraseñas encriptadas |
| OE-02 | Desarrollar el CRUD completo de espacios con control de estado activo/inactivo | Espacios | Espacios creados, editados y desactivados correctamente |
| OE-03 | Implementar la gestión de clientes con validación de DNI y email únicos | Clientes | Clientes registrados sin duplicados |
| OE-04 | Crear el módulo de reservas con validación automática de solapamientos y flujo de estados | Reservas | 0 dobles reservas, flujo PENDIENTE→COMPLETADA funcional |
| OE-05 | Implementar el sistema de horarios bloqueados que impida reservas en horas no disponibles | Horarios Bloqueados | Reservas rechazadas en horarios bloqueados |
| OE-06 | Desarrollar el módulo de pagos con múltiples métodos y aplicación de descuentos | Pagos | Pagos procesados correctamente con descuentos |
| OE-07 | Integrar envío automático de email de confirmación de pago vía Gmail SMTP | Email | Email recibido por cliente tras cada pago |
| OE-08 | Implementar el sistema de evaluaciones por token UUID con formulario web público | Evaluaciones | Token generado, email enviado, evaluación guardada en BD |
| OE-09 | Desarrollar el centro de notificaciones con envío manual de email de evaluación | Notificaciones | Botón envía email una sola vez, sin duplicados |
| OE-10 | Crear el dashboard ejecutivo con KPIs y calendario visual semanal de disponibilidad | Dashboard | KPIs correctos, calendario muestra disponibilidad real |
| OE-11 | Implementar el módulo de descuentos con validación automática de vigencia y usos | Descuentos | Códigos válidos aplicados, inválidos rechazados |
| OE-12 | Documentar el sistema completamente (README, guía técnica, scripts SQL, instalación) | Documentación | Documentos completos y verificados |

---

## 13. Alcances

### 13.1 Alcance Funcional

El sistema **SÍ incluye** las siguientes funcionalidades:

#### Módulos Implementados

| Módulo | Funcionalidades incluidas |
|--------|--------------------------|
| **Autenticación** | Login, logout, sesión persistente, roles ADMIN/GERENTE, hash SHA-256 |
| **Dashboard** | KPIs en tiempo real, calendario semanal por espacio/hora, filtro por espacio |
| **Espacios** | CRUD completo, estado ACTIVO/INACTIVO, filtros de búsqueda |
| **Clientes** | CRUD completo, validación DNI/email únicos, búsqueda en tiempo real |
| **Reservas** | Crear/confirmar/completar/cancelar, cálculo automático de monto, validación de solapamientos, tarjetas con datos completos |
| **Horarios Bloqueados** | Crear/eliminar bloqueos con razón, validación en reservas, nombre del espacio visible |
| **Pagos** | Lista de pagos, procesar con método y descuento opcional, email de confirmación |
| **Descuentos** | CRUD de códigos, validación de vigencia/usos/monto mínimo, aplicación en pago |
| **Evaluaciones** | Token UUID único, formulario público (sin login), calificación 1-5 estrellas + comentario, tabla solo lectura |
| **Notificaciones** | Lista por tipo, filtro, botón envío manual de email evaluación (una sola vez) |

#### Flujos de Negocio Cubiertos

1. **Ciclo de vida completo de una reserva**: Crear → Confirmar → Completar → Pago → Evaluación
2. **Control de disponibilidad**: Validación en tiempo real de solapamientos y horarios bloqueados
3. **Comunicación con el cliente**: Emails automáticos de confirmación de pago y solicitud de evaluación
4. **Gestión financiera básica**: Pagos, descuentos, registro de métodos de pago
5. **Visibilidad ejecutiva**: Dashboard con KPIs y calendario de ocupación

### 13.2 Alcance Técnico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Lenguaje Backend | Java SE | 8+ |
| Servidor HTTP | com.sun.net.httpserver.HttpServer | Embebido JDK |
| Base de Datos | Oracle Database XE | 11g+ |
| Driver BD | JDBC ojdbc8 | 8+ |
| Frontend | HTML5 + Bootstrap + JavaScript | Bootstrap 5.1 |
| Email | JavaMail + Gmail SMTP TLS | 1.6.2 |
| Patrón | MVC + DAO + REST | — |
| Puerto | 8080 (local) | — |

### 13.3 Alcance de Usuarios

- **1 sede** operativa de SpaceWork Perú S.A.C.
- Usuarios del sistema: administradores y gerentes de la empresa
- Clientes externos: solo interactúan vía email (formulario de evaluación público)

---

## 14. Limitaciones

Las siguientes funcionalidades están **fuera del alcance** de la versión actual (v2.0):

| # | Limitación | Razón | Versión futura |
|---|-----------|-------|----------------|
| L-01 | **Sin módulo de pagos en línea** (PasareLas: Izipay, Culqi, PayPal) | Requiere integración con pasarelas comerciales y certificados SSL | v3.0 |
| L-02 | **Sin aplicación móvil** (iOS/Android) | Fuera del alcance del curso universitario | v3.0 |
| L-03 | **Sin notificaciones push** (WhatsApp, SMS) | Requiere APIs de terceros (Twilio, Meta) | v3.0 |
| L-04 | **Diseñado para una sola sede** | Sin módulo multiempresa/multisede | v3.0 |
| L-05 | **Sin integración SUNAT** (facturación electrónica) | Requiere certificado digital y API de SUNAT | v3.0 |
| L-06 | **Sin reportes PDF** | Solo exportación CSV disponible | v2.5 |
| L-07 | **Sin reservas por el propio cliente** (portal cliente) | El admin gestiona todas las reservas | v2.5 |
| L-08 | **Sin recuperación de contraseña** por email | Requiere flujo adicional de reset password | v2.5 |
| L-09 | **Sin histórico de auditoría visible** en UI | El módulo de auditoría existe en BD pero sin interfaz gráfica completa | v2.5 |
| L-10 | **Sin modo offline** para formulario de evaluación | Requiere conexión al servidor local | v2.5 |

---

## 15. Justificación

### 15.1 Justificación Técnica

El proyecto demuestra la aplicación de conocimientos avanzados de ingeniería de software:

- **Arquitectura MVC + DAO**: Separación clara de responsabilidades en capas (model, view, controller, dao, util)
- **Patrón REST**: API bien definida con endpoints HTTP (GET, POST, PUT, DELETE)
- **Base de datos relacional**: Diseño normalizado con 12 tablas, Foreign Keys, Sequences, Constraints
- **Seguridad**: Hash SHA-256, PreparedStatement (anti SQL Injection), tokens UUID
- **Integración de servicios**: Email SMTP, manejo de tokens, formularios públicos con validación
- **SPA (Single Page Application)**: Frontend moderno con Bootstrap 5 sin recargas de página

### 15.2 Justificación Académica

El proyecto cumple con los objetivos del **Curso Integrador: Sistemas de Software** de la UTP:

| Competencia | Evidencia en el proyecto |
|-------------|--------------------------|
| Análisis de requerimientos | 10 RF + 6 RNF documentados con IDs y criterios |
| Diseño de base de datos | 12 tablas normalizadas, modelo relacional, objetos Oracle |
| Programación orientada a objetos | 15 modelos, 14 DAOs, 11 controladores en Java |
| Desarrollo web | SPA con REST API, Bootstrap 5, JavaScript vanilla |
| Integración de sistemas | Email SMTP, JDBC, HttpServer embebido |
| Documentación técnica | 5 archivos de documentación + scripts SQL comentados |
| Pruebas | Tests unitarios con JUnit 4 para DAOs críticos |

### 15.3 Justificación Empresarial

La implementación de SpaceWork genera valor directo para la empresa:

| Antes (Manual) | Después (SpaceWork) | Mejora |
|----------------|---------------------|--------|
| 2-3 horas/día en gestión manual | 15-30 min/día | **-85% tiempo administrativo** |
| 15-20% reservas con errores | 0% dobles reservas | **-100% errores de solapamiento** |
| 0 evaluaciones recopiladas | Evaluación automática post-servicio | **+100% feedback capturado** |
| Reportes en 1 día de trabajo | Dashboard en tiempo real | **Decisiones inmediatas** |
| Sin confirmaciones digitales | Email automático al cliente | **+Satisfacción del cliente** |
| Sin control de descuentos | Gestión de códigos promocionales | **+Herramientas de fidelización** |

### 15.4 Impacto Social y Económico

- **Formalización digital** del negocio: SpaceWork puede competir con grandes operadores de coworking
- **Eficiencia operativa**: Mismo personal puede gestionar mayor volumen de reservas
- **Experiencia del cliente**: Confirmaciones inmediatas y canal de feedback elevan la percepción del servicio
- **Base para escalar**: La arquitectura permite agregar nuevas sedes y funcionalidades sin rediseñar desde cero

---

## Resumen Final

| Sección | Estado |
|---------|--------|
| Aspectos generales definidos | ✅ |
| Descripción de empresa documentada | ✅ |
| Problema identificado con impacto | ✅ |
| Misión y visión establecidas | ✅ |
| Estrategias definidas | ✅ |
| Levantamiento de información realizado | ✅ |
| 10 RF documentados por módulo | ✅ |
| 6 RNF con criterios verificables | ✅ |
| Viabilidad técnica/económica analizada | ✅ |
| Objetivo general definido | ✅ |
| 12 objetivos específicos con indicadores | ✅ |
| Alcance funcional y técnico definido | ✅ |
| 10 limitaciones documentadas | ✅ |
| Justificación técnica/académica/empresarial | ✅ |

---

> Volver al índice: [INFORME_MAIN.md](INFORME_MAIN.md)
