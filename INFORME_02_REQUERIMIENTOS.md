# INFORME 02 — Requerimientos y Viabilidad
**SpaceWork | Universidad Tecnológica del Perú | 2026**

---

## 8. Levantamiento de Información

### 8.1 Técnicas Utilizadas

| Técnica | Aplicación | Resultado |
|---------|------------|-----------|
| **Entrevista** | Con el administrador de SpaceWork sobre procesos actuales | Identificación de 10 problemas críticos |
| **Observación directa** | Revisión del proceso manual de reservas (Excel + WhatsApp) | Mapeo del flujo actual |
| **Análisis documental** | Revisión de hojas de cálculo y registros históricos | Entendimiento de la estructura de datos existente |
| **Benchmarking** | Análisis de sistemas similares (WeWork, Regus, Comunal) | Definición de funcionalidades esperadas |

### 8.2 Hallazgos del Levantamiento

**Proceso actual (AS-IS):**
```
Cliente llama/escribe por WhatsApp
        ↓
Admin revisa Excel manualmente
        ↓
Admin responde confirmación (puede demorar horas)
        ↓
Cliente paga en efectivo el día del uso
        ↓
Admin anota el pago en Excel
        ↓
No existe evaluación del servicio
```

**Proceso propuesto (TO-BE):**
```
Admin registra cliente en sistema
        ↓
Admin crea reserva → sistema valida disponibilidad en tiempo real
        ↓
Admin confirma → sistema actualiza estado automáticamente
        ↓
Al completar → sistema crea pago PENDIENTE automáticamente
        ↓
Admin procesa pago → sistema envía email de confirmación
        ↓
Sistema crea notificación de evaluación
        ↓
Admin envía email de evaluación → cliente califica con estrellas
        ↓
Dashboard muestra KPIs en tiempo real
```

### 8.3 Actores del Sistema

| Actor | Rol | Permisos |
|-------|-----|----------|
| **Administrador** | Gestiona todo el sistema | Acceso total: espacios, clientes, reservas, pagos, evaluaciones, notificaciones, descuentos, horarios |
| **Gerente** | Supervisa operaciones | Reportes, evaluaciones, KPIs, sin modificar configuración |
| **Cliente** | Beneficiario del servicio | Solo recibe email con enlace para evaluar su reserva |

---

## 9. Requerimientos Funcionales

Los requerimientos funcionales describen **qué debe hacer** el sistema.

### RF-01: Autenticación y Control de Acceso
| ID | Requerimiento |
|----|---------------|
| RF-01.1 | El sistema debe permitir login con usuario y contraseña |
| RF-01.2 | Las contraseñas deben almacenarse con hash SHA-256 |
| RF-01.3 | El sistema debe soportar roles: ADMIN, GERENTE |
| RF-01.4 | La sesión debe persistir en el navegador (localStorage) |
| RF-01.5 | El sistema debe permitir cerrar sesión (logout) |

### RF-02: Gestión de Espacios
| ID | Requerimiento |
|----|---------------|
| RF-02.1 | El sistema debe permitir crear, editar y eliminar espacios |
| RF-02.2 | Cada espacio debe registrar: nombre, tipo, capacidad, ubicación, precio por hora, estado |
| RF-02.3 | Los espacios eliminados deben desactivarse (eliminación lógica, estado=INACTIVO) |
| RF-02.4 | Solo espacios ACTIVOS deben aparecer disponibles para reserva |
| RF-02.5 | El sistema debe permitir buscar/filtrar espacios por nombre o tipo |

### RF-03: Gestión de Clientes
| ID | Requerimiento |
|----|---------------|
| RF-03.1 | El sistema debe permitir registrar clientes con: nombre, apellido, DNI, email, teléfono |
| RF-03.2 | El DNI y email deben ser únicos por cliente |
| RF-03.3 | El sistema debe permitir editar y desactivar clientes |
| RF-03.4 | El sistema debe permitir buscar clientes por nombre, DNI o email |

### RF-04: Gestión de Reservas
| ID | Requerimiento |
|----|---------------|
| RF-04.1 | El sistema debe permitir crear reservas asignando cliente, espacio, fecha/hora inicio y fin |
| RF-04.2 | El sistema debe calcular automáticamente el monto total (precio/hora × horas) |
| RF-04.3 | El sistema debe rechazar reservas que se solapan con otras del mismo espacio |
| RF-04.4 | El sistema debe rechazar reservas en horarios bloqueados |
| RF-04.5 | El sistema debe gestionar los estados: PENDIENTE → CONFIRMADA → COMPLETADA / CANCELADA |
| RF-04.6 | Al completar una reserva, el sistema debe crear automáticamente un pago PENDIENTE |
| RF-04.7 | El sistema debe mostrar nombre del cliente, espacio, fechas, duración y monto en cada reserva |

### RF-05: Gestión de Horarios Bloqueados
| ID | Requerimiento |
|----|---------------|
| RF-05.1 | El sistema debe permitir bloquear rangos horarios por espacio y razón (mantenimiento, eventos, etc.) |
| RF-05.2 | El sistema debe mostrar el nombre del espacio (no solo el ID) en los bloques |
| RF-05.3 | El sistema debe validar horarios bloqueados al crear nuevas reservas |
| RF-05.4 | El sistema debe permitir eliminar (desbloquear) bloques horarios |

### RF-06: Gestión de Pagos
| ID | Requerimiento |
|----|---------------|
| RF-06.1 | Los pagos deben crearse automáticamente al completar una reserva |
| RF-06.2 | El sistema debe permitir procesar pagos seleccionando método: EFECTIVO, TARJETA, TRANSFERENCIA, YAPE/PLIN |
| RF-06.3 | El sistema debe permitir aplicar códigos de descuento al momento del pago |
| RF-06.4 | Al procesar el pago, el sistema debe enviar email de confirmación al cliente |
| RF-06.5 | Al procesar el pago, el sistema debe crear una notificación tipo EVALUACION |
| RF-06.6 | El sistema debe mostrar estado del pago: PENDIENTE, COMPLETADO, RECHAZADO |

### RF-07: Gestión de Descuentos
| ID | Requerimiento |
|----|---------------|
| RF-07.1 | El sistema debe permitir crear códigos de descuento con porcentaje, descripción y vigencia |
| RF-07.2 | Cada código debe tener un monto mínimo y un límite de usos |
| RF-07.3 | El sistema debe validar automáticamente: vigencia, usos restantes y monto mínimo |
| RF-07.4 | El sistema debe activar/desactivar códigos de descuento |
| RF-07.5 | El sistema debe incrementar el contador de usos al aplicar un descuento |

### RF-08: Sistema de Evaluaciones
| ID | Requerimiento |
|----|---------------|
| RF-08.1 | El sistema debe generar tokens UUID únicos por pago para evaluación |
| RF-08.2 | El token debe enviarse al cliente por email como enlace a formulario web |
| RF-08.3 | El formulario debe permitir calificar de 1 a 5 estrellas y agregar comentario |
| RF-08.4 | El token debe ser de un solo uso y tener vigencia de 30 días |
| RF-08.5 | El módulo de evaluaciones debe ser de solo lectura para el administrador |
| RF-08.6 | El sistema debe mostrar nombre del cliente, email, calificación, comentario y fecha |

### RF-09: Sistema de Notificaciones
| ID | Requerimiento |
|----|---------------|
| RF-09.1 | El sistema debe crear notificaciones por tipo: RESERVA, PAGO, EVALUACION, RECORDATORIO, SISTEMA |
| RF-09.2 | Las notificaciones tipo EVALUACION deben mostrar botón "Enviar Evaluación" si no han sido enviadas |
| RF-09.3 | Al enviar la evaluación, el botón debe deshabilitarse (anti doble envío) |
| RF-09.4 | El sistema debe permitir filtrar notificaciones por tipo |
| RF-09.5 | El sistema debe marcar la notificación como leída al enviar el email de evaluación |

### RF-10: Dashboard y Reportes
| ID | Requerimiento |
|----|---------------|
| RF-10.1 | El dashboard debe mostrar KPIs: total reservas, reservas confirmadas, espacios activos, ocupación %, total clientes, ingresos totales |
| RF-10.2 | El dashboard debe incluir un calendario semanal de disponibilidad por espacio y hora |
| RF-10.3 | El calendario debe mostrar 7 días adelante con bloques de 1 hora (08:00-18:00) |
| RF-10.4 | El calendario debe distinguir: disponible (verde), ocupado (rojo), bloqueado (amarillo) |
| RF-10.5 | El calendario debe permitir filtrar por espacio |

---

## 10. Requerimientos No Funcionales

Los requerimientos no funcionales describen **cómo debe comportarse** el sistema.

### RNF-01: Rendimiento
| ID | Requerimiento |
|----|---------------|
| RNF-01.1 | Las consultas a la base de datos deben responder en menos de 2 segundos |
| RNF-01.2 | El calendario semanal debe cargar en menos de 3 segundos con hasta 10 espacios activos |
| RNF-01.3 | El servidor debe soportar múltiples usuarios concurrentes sin degradación |

### RNF-02: Seguridad
| ID | Requerimiento |
|----|---------------|
| RNF-02.1 | Las contraseñas deben almacenarse con hash SHA-256 (nunca en texto plano) |
| RNF-02.2 | Todas las consultas SQL deben usar PreparedStatement para prevenir inyección SQL |
| RNF-02.3 | Los tokens de evaluación deben ser UUID v4 irrepetibles |
| RNF-02.4 | Los tokens deben expirar después de 30 días y ser de un solo uso |
| RNF-02.5 | El formulario de evaluación debe ser público pero validado por token |

### RNF-03: Usabilidad
| ID | Requerimiento |
|----|---------------|
| RNF-03.1 | La interfaz debe ser responsiva y funcionar en navegadores modernos (Chrome, Firefox, Edge) |
| RNF-03.2 | El sistema debe mostrar mensajes de error claros al usuario |
| RNF-03.3 | Cada módulo debe incluir filtros de búsqueda en tiempo real |
| RNF-03.4 | Los botones destructivos deben solicitar confirmación antes de ejecutar |

### RNF-04: Disponibilidad y Confiabilidad
| ID | Requerimiento |
|----|---------------|
| RNF-04.1 | El sistema debe funcionar en red local sin requerir internet (excepto email) |
| RNF-04.2 | La base de datos debe garantizar integridad referencial mediante Foreign Keys |
| RNF-04.3 | El sistema debe registrar errores críticos en consola para diagnóstico |

### RNF-05: Mantenibilidad
| ID | Requerimiento |
|----|---------------|
| RNF-05.1 | El código debe seguir el patrón MVC + DAO para separación de responsabilidades |
| RNF-05.2 | Las consultas SQL deben estar centralizadas en los DAOs correspondientes |
| RNF-05.3 | La configuración de BD y email debe estar en archivos .properties separados |

### RNF-06: Portabilidad
| ID | Requerimiento |
|----|---------------|
| RNF-06.1 | El sistema debe ejecutarse en Windows, Linux y macOS |
| RNF-06.2 | No debe requerir instalación adicional más allá de JDK 8+ y Oracle XE |
| RNF-06.3 | Los archivos JAR de dependencias deben estar incluidos en la carpeta lib/ |

---

## 11. Selección y Viabilidad de la Solución

### 11.1 Alternativas Evaluadas

| Alternativa | Tecnología | Ventajas | Desventajas | Seleccionada |
|-------------|-----------|----------|-------------|--------------|
| **A: Sistema Web Java SE** | Java 8 + HttpServer + Oracle | Sin dependencias externas, control total, ligero | Requiere más código manual | ✅ **Sí** |
| **B: Spring Boot + Oracle** | Spring Boot + JPA | Framework robusto, menos código | Requiere Maven, mayor complejidad inicial | ❌ No |
| **C: PHP + MySQL** | PHP 8 + MySQL | Fácil hosting, gran comunidad | Inconsistente con stack Java del curso | ❌ No |
| **D: Excel + Access** | Microsoft Office | Sin desarrollo | No escala, sin acceso web, manual | ❌ No |

### 11.2 Justificación de la Solución Seleccionada

La **Alternativa A (Java SE + Oracle)** fue seleccionada porque:

1. **Alineación académica**: El curso usa Java SE como lenguaje base
2. **Control total**: Sin frameworks que oculten la lógica de negocio al evaluador
3. **Oracle**: Base de datos empresarial requerida por el sílabo
4. **Cero dependencias adicionales**: Solo JDK 8 + Oracle XE + JARs en lib/
5. **Demonstra competencias**: Implementación manual de REST, DAO, MVC
6. **Portabilidad**: Funciona en cualquier SO con JDK 8+

### 11.3 Viabilidad Técnica

| Aspecto | Evaluación | Detalle |
|---------|------------|---------|
| Lenguaje | ✅ Viable | Java SE 8+ disponible gratuitamente |
| Base de datos | ✅ Viable | Oracle XE gratuito para desarrollo |
| Email | ✅ Viable | Gmail SMTP con App Password (gratuito) |
| Servidor | ✅ Viable | HttpServer embebido en JDK (sin costo) |
| Frontend | ✅ Viable | Bootstrap 5 CDN (sin instalación) |

### 11.4 Viabilidad Operativa

| Aspecto | Evaluación | Detalle |
|---------|------------|---------|
| Adopción del personal | ✅ Alta | Interfaz web intuitiva (Bootstrap 5) |
| Curva de aprendizaje | ✅ Baja | Módulos simples y navegación clara |
| Capacitación requerida | ✅ Mínima | 1-2 horas de demostración |
| Mantenimiento | ✅ Simple | Código bien estructurado MVC+DAO |

### 11.5 Viabilidad Económica

| Ítem | Costo |
|------|-------|
| JDK 8 | S/. 0 (gratuito) |
| Oracle Database XE | S/. 0 (gratuito para desarrollo) |
| Gmail SMTP | S/. 0 (gratuito con App Password) |
| Bootstrap 5 (CDN) | S/. 0 |
| Hosting (red local) | S/. 0 (servidor en oficina) |
| **Total** | **S/. 0** |

---

> Continúa en: [INFORME_03_OBJETIVOS.md](INFORME_03_OBJETIVOS.md)
