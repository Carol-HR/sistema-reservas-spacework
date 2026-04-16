# INFORME 01 — Empresa, Problema y Contexto
**SpaceWork | Universidad Tecnológica del Perú | 2026**

---

## 1. Aspectos Generales

| Campo | Detalle |
|-------|---------|
| **Nombre del proyecto** | SpaceWork — Sistema de Gestión de Reservas de Espacios |
| **Empresa** | SpaceWork Perú S.A.C. |
| **Sector** | Coworking / Alquiler de espacios de trabajo |
| **Ubicación** | Lima, Perú |
| **Año** | 2026 |
| **Tipo de sistema** | Aplicación Web (SPA) — Java SE 8 + Oracle XE |
| **Versión** | 2.0 |
| **Estado** | En producción |
| **Equipo** | Proyecto universitario — Curso Integrador: Sistemas de Software |

---

## 2. Descripción de la Empresa

**SpaceWork Perú S.A.C.** es una empresa limeña fundada con el propósito de ofrecer espacios de trabajo flexible y compartido (coworking) a profesionales independientes, startups y empresas que requieren instalaciones modernas sin los costos fijos de una oficina propia.

### Servicios que ofrece

| Tipo de Espacio | Capacidad Típica | Uso Principal |
|-----------------|-----------------|---------------|
| Sala de reuniones | 4 – 10 personas | Reuniones de equipo, conferencias |
| Auditorio | 50 – 200 personas | Eventos, capacitaciones masivas |
| Sala de capacitación | 15 – 30 personas | Talleres, cursos, formaciones |
| Laboratorio de cómputo | 20 – 40 personas | Clases técnicas, hackathons |
| Área coworking abierta | Ilimitada | Trabajo individual, freelancers |
| Oficina privada | 2 – 6 personas | Trabajo en equipo pequeño |

### Modelo de negocio

SpaceWork cobra por hora de uso de cada espacio. Los clientes realizan reservas con anticipación, las cuales son gestionadas y confirmadas por el personal administrativo. El pago se realiza al momento de confirmar o al finalizar el uso del espacio.

### Alcance geográfico (actual)

Una sola sede en Lima Metropolitana con proyección a expandirse a 3 sedes en los próximos 2 años.

---

## 3. Descripción del Problema

### 3.1 Situación Actual (Antes del Sistema)

Antes del desarrollo de SpaceWork, la empresa gestionaba sus operaciones de forma completamente manual:

- **Reservas**: anotadas en hojas de cálculo Excel o libretas físicas
- **Confirmaciones**: enviadas por WhatsApp o llamadas telefónicas
- **Pagos**: registrados manualmente en Excel, sin trazabilidad digital
- **Disponibilidad**: consultada revisando manualmente el calendario
- **Evaluaciones**: inexistentes — no se recopilaba feedback del cliente

### 3.2 Problemas Identificados

| # | Problema | Impacto en el negocio |
|---|----------|-----------------------|
| P-01 | **Doble reserva del mismo espacio** | Conflictos entre clientes, pérdida de confianza |
| P-02 | **Información dispersa** (Excel, WhatsApp, papel) | Dificultad para consultar historial de reservas y pagos |
| P-03 | **Sin control de estados de reserva** | Personal y clientes confundidos sobre si la reserva está confirmada |
| P-04 | **Reportes manuales lentos** | Toma de decisiones tardía sin visibilidad de ingresos |
| P-05 | **Sin sistema de pagos integrado** | Pagos perdidos o duplicados, sin comprobante digital |
| P-06 | **Sin notificaciones automáticas** | Clientes olvidaban sus reservas, alta tasa de no-shows |
| P-07 | **Sin evaluación del servicio** | Imposible medir satisfacción del cliente ni mejorar el servicio |
| P-08 | **Sin auditoría de cambios** | Imposible rastrear quién modificó o canceló una reserva |
| P-09 | **Escalabilidad limitada** | El sistema manual no podía soportar crecimiento del negocio |
| P-10 | **Sin control de horarios bloqueados** | Mantenimientos o eventos privados causaban conflictos con reservas |

### 3.3 Impacto Económico

- Pérdida estimada del **15-20% de reservas** por errores de doble asignación
- **2-3 horas diarias** del personal administrativo dedicadas a tareas manuales
- Clientes insatisfechos sin canal formal de feedback → **baja fidelización**
- Imposibilidad de generar reportes financieros en menos de 1 día de trabajo

---

## 4. Contexto

El mercado de coworking en Lima creció significativamente entre 2022 y 2026, impulsado por el trabajo remoto e híbrido post-pandemia. Empresas como WeWork, Regus y Comunal ofrecen sistemas digitales avanzados de gestión de espacios, mientras SpaceWork operaba con herramientas manuales, perdiendo competitividad.

### Tendencias del mercado

- **85%** de empresas coworking en Lima ofrecen plataforma digital de reservas (2025)
- Clientes esperan confirmación inmediata de reservas (preferencia por sistemas en línea)
- Creciente demanda de transparencia en precios, disponibilidad y pagos digitales
- Evaluaciones y feedback digital son factores clave en la decisión de compra

### Oportunidad

La automatización del proceso de reservas y pagos permite a SpaceWork:
1. Reducir errores operativos al 0%
2. Liberar tiempo del personal para tareas de mayor valor
3. Ofrecer una experiencia digital moderna comparable a competidores líderes
4. Escalar el negocio sin incrementar costos administrativos proporcionales

---

## 5. Misión

> *"Facilitar el acceso a espacios de trabajo de calidad mediante una plataforma tecnológica eficiente, confiable y centrada en la experiencia del cliente, que automatice y optimice cada etapa del proceso de reserva, pago y evaluación del servicio."*

---

## 6. Visión

> *"Ser la plataforma de gestión de espacios de coworking más utilizada en Lima para 2028, reconocida por su tecnología intuitiva, su confiabilidad operativa y su contribución al crecimiento de las empresas y profesionales que confían en nuestros espacios."*

---

## 7. Estrategias

### 7.1 Estrategia Tecnológica

| Estrategia | Descripción | Impacto |
|------------|-------------|---------|
| **Digitalización total** | Eliminar procesos manuales (Excel, libretas, WhatsApp) | Reducir errores al 0% |
| **Arquitectura web SPA** | Sistema accesible desde cualquier navegador sin instalación | Mayor adopción por usuarios |
| **Base de datos centralizada** | Oracle XE para garantizar consistencia e integridad de datos | Trazabilidad completa |
| **Email automático** | JavaMail + Gmail SMTP para confirmaciones y evaluaciones | Mejora experiencia del cliente |

### 7.2 Estrategia Operativa

| Estrategia | Descripción |
|------------|-------------|
| **Flujo de estados** | PENDIENTE → CONFIRMADA → COMPLETADA previene errores de proceso |
| **Validación automática** | Sistema rechaza automáticamente solapamientos y horarios bloqueados |
| **Dashboard ejecutivo** | KPIs en tiempo real para toma de decisiones rápida |
| **Calendario visual** | Vista semanal de disponibilidad por espacio y hora |

### 7.3 Estrategia Comercial

| Estrategia | Descripción |
|------------|-------------|
| **Sistema de descuentos** | Códigos promocionales para fidelizar clientes y aumentar ocupación |
| **Evaluaciones post-reserva** | Recopilar feedback para mejorar continuamente el servicio |
| **Notificaciones automáticas** | Reducir no-shows y mejorar comunicación con el cliente |
| **Reportes financieros** | Visibilidad de ingresos y ocupación para planificación estratégica |

---

> Continúa en: [INFORME_02_REQUERIMIENTOS.md](INFORME_02_REQUERIMIENTOS.md)
