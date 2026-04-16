-- ============================================================
-- 01_crear_tablas.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Ejecutar conectado como usuario: spacework
-- ============================================================

-- TABLA USUARIOS
CREATE TABLE USUARIOS (
    id_usuario     NUMBER          CONSTRAINT pk_usuarios PRIMARY KEY,
    username       VARCHAR2(50)    CONSTRAINT uq_username UNIQUE NOT NULL,
    password_hash  VARCHAR2(64)    NOT NULL,
    nombre         VARCHAR2(100)   NOT NULL,
    email          VARCHAR2(150)   CONSTRAINT uq_email_usuario UNIQUE NOT NULL,
    rol            VARCHAR2(20)    NOT NULL,
    estado         VARCHAR2(10)    DEFAULT 'ACTIVO' NOT NULL,
    fecha_registro DATE            DEFAULT SYSDATE,
    CONSTRAINT chk_rol_usuario   CHECK (rol    IN ('ADMINISTRADOR', 'GERENTE', 'CLIENTE')),
    CONSTRAINT chk_estado_usuario CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- TABLA ESPACIOS
CREATE TABLE ESPACIOS (
    id_espacio      NUMBER         CONSTRAINT pk_espacios PRIMARY KEY,
    nombre          VARCHAR2(100)  NOT NULL,
    tipo            VARCHAR2(30)   NOT NULL,
    capacidad       NUMBER(4)      NOT NULL,
    ubicacion       VARCHAR2(200)  NOT NULL,
    precio_por_hora NUMBER(10,2)   NOT NULL,
    estado          VARCHAR2(10)   DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT chk_tipo_espacio   CHECK (tipo   IN ('SALA_REUNION', 'OFICINA', 'COWORKING', 'AUDITORIO')),
    CONSTRAINT chk_estado_espacio CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_precio          CHECK (precio_por_hora > 0),
    CONSTRAINT chk_capacidad       CHECK (capacidad > 0)
);

-- TABLA CLIENTES
CREATE TABLE CLIENTES (
    id_cliente     NUMBER          CONSTRAINT pk_clientes PRIMARY KEY,
    nombre         VARCHAR2(100)   NOT NULL,
    apellido       VARCHAR2(100)   NOT NULL,
    dni            VARCHAR2(15)    CONSTRAINT uq_dni UNIQUE NOT NULL,
    email          VARCHAR2(150)   CONSTRAINT uq_email_cliente UNIQUE NOT NULL,
    telefono       VARCHAR2(15),
    estado         VARCHAR2(10)    DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT chk_estado_cliente CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- TABLA RESERVAS
CREATE TABLE RESERVAS (
    id_reserva     NUMBER          CONSTRAINT pk_reservas PRIMARY KEY,
    id_cliente     NUMBER          NOT NULL,
    id_espacio     NUMBER          NOT NULL,
    fecha_inicio   TIMESTAMP       NOT NULL,
    fecha_fin      TIMESTAMP       NOT NULL,
    monto_total    NUMBER(10,2)    NOT NULL,
    estado         VARCHAR2(15)    DEFAULT 'PENDIENTE' NOT NULL,
    fecha_creacion DATE            DEFAULT SYSDATE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente),
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT chk_estado_reserva CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'COMPLETADA', 'CANCELADA')),
    CONSTRAINT chk_fechas_reserva CHECK (fecha_fin > fecha_inicio)
);

-- TABLA AUDITORIA
CREATE TABLE AUDITORIA (
    id_auditoria   NUMBER          CONSTRAINT pk_auditoria PRIMARY KEY,
    id_reserva     NUMBER          NOT NULL,
    estado_anterior VARCHAR2(15),
    estado_nuevo   VARCHAR2(15),
    fecha_cambio   DATE            DEFAULT SYSDATE,
    usuario_sistema VARCHAR2(50)   DEFAULT USER,
    CONSTRAINT fk_auditoria_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva)
);

-- TABLA PAGOS
CREATE TABLE PAGOS (
    id_pago            NUMBER          CONSTRAINT pk_pagos PRIMARY KEY,
    id_reserva         NUMBER          NOT NULL,
    monto              NUMBER(10,2)    NOT NULL,
    metodo_pago        VARCHAR2(30),
    estado_pago        VARCHAR2(15)    DEFAULT 'PENDIENTE' NOT NULL,
    fecha_pago         DATE,
    fecha_creacion     DATE            DEFAULT SYSDATE,
    descuento_aplicado NUMBER(10,2)    DEFAULT 0,
    id_descuento       NUMBER,
    CONSTRAINT fk_pago_reserva   FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva),
    CONSTRAINT chk_metodo_pago   CHECK (metodo_pago IN ('TARJETA', 'TRANSFERENCIA', 'EFECTIVO')),
    CONSTRAINT chk_estado_pago   CHECK (estado_pago IN ('PENDIENTE', 'COMPLETADO', 'RECHAZADO', 'REEMBOLSADO')),
    CONSTRAINT chk_monto_pago    CHECK (monto > 0)
);

-- TABLA HORARIOS
CREATE TABLE HORARIOS (
    id_horario     NUMBER          CONSTRAINT pk_horarios PRIMARY KEY,
    id_espacio     NUMBER          NOT NULL,
    dia_semana     NUMBER(1)       NOT NULL,
    hora_apertura  VARCHAR2(5)     NOT NULL,
    hora_cierre    VARCHAR2(5)     NOT NULL,
    estado         VARCHAR2(10)    DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT fk_horario_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT chk_dia_semana    CHECK (dia_semana BETWEEN 0 AND 6),
    CONSTRAINT chk_estado_horario CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- TABLA DESCUENTOS
CREATE TABLE DESCUENTOS (
    id_descuento   NUMBER          CONSTRAINT pk_descuentos PRIMARY KEY,
    codigo         VARCHAR2(20)    CONSTRAINT uq_codigo_descuento UNIQUE NOT NULL,
    descripcion    VARCHAR2(200),
    porcentaje     NUMBER(5,2)     NOT NULL,
    monto_minimo   NUMBER(10,2),
    fecha_inicio   DATE            NOT NULL,
    fecha_fin      DATE            NOT NULL,
    usos_maximos   NUMBER(5),
    usos_actuales  NUMBER(5)       DEFAULT 0,
    estado         VARCHAR2(10)    DEFAULT 'ACTIVO' NOT NULL,
    CONSTRAINT chk_porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT chk_estado_descuento CHECK (estado IN ('ACTIVO', 'INACTIVO')),
    CONSTRAINT chk_fechas_descuento CHECK (fecha_fin >= fecha_inicio)
);

-- TABLA EVALUACIONES
CREATE TABLE EVALUACIONES (
    id_evaluacion  NUMBER          CONSTRAINT pk_evaluaciones PRIMARY KEY,
    id_reserva     NUMBER          NOT NULL,
    id_cliente     NUMBER          NOT NULL,
    calificacion   NUMBER(2)       NOT NULL,
    comentario     VARCHAR2(500),
    fecha_evaluacion DATE          DEFAULT SYSDATE,
    CONSTRAINT fk_evaluacion_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_evaluacion_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente),
    CONSTRAINT chk_calificacion CHECK (calificacion BETWEEN 1 AND 5)
);

-- TABLA NOTIFICACIONES
CREATE TABLE NOTIFICACIONES (
    id_notificacion NUMBER          CONSTRAINT pk_notificaciones PRIMARY KEY,
    id_usuario     NUMBER          NOT NULL,
    tipo           VARCHAR2(30)    NOT NULL,
    asunto         VARCHAR2(100)   NOT NULL,
    mensaje        VARCHAR2(500)   NOT NULL,
    leida          NUMBER(1)       DEFAULT 0 NOT NULL,
    fecha_creacion DATE            DEFAULT SYSDATE,
    fecha_leida    DATE,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario),
    CONSTRAINT chk_tipo_notificacion CHECK (tipo IN ('RESERVA', 'PAGO', 'RECORDATORIO', 'PROMOCION', 'SISTEMA', 'EVALUACION')),
    CONSTRAINT chk_leida CHECK (leida IN (0, 1))
);

-- TABLA TOKENS_EVALUACION (NUEVA)
-- Almacena tokens temporales para permitir evaluación por email sin autenticación
CREATE TABLE TOKENS_EVALUACION (
    id_token        NUMBER          CONSTRAINT pk_tokens_evaluacion PRIMARY KEY,
    id_pago         NUMBER          NOT NULL,
    token           VARCHAR2(200)   CONSTRAINT uq_token_evaluacion UNIQUE NOT NULL,
    email_cliente   VARCHAR2(150)   NOT NULL,
    fecha_creacion  DATE            DEFAULT SYSDATE,
    fecha_expiracion DATE           NOT NULL,
    utilizado       NUMBER(1)       DEFAULT 0 NOT NULL,
    CONSTRAINT fk_token_pago FOREIGN KEY (id_pago) REFERENCES PAGOS(id_pago),
    CONSTRAINT chk_utilizado CHECK (utilizado IN (0, 1))
);

-- FK diferida: PAGOS → DESCUENTOS
-- (DESCUENTOS se crea después de PAGOS, por eso el FK se agrega al final)
ALTER TABLE PAGOS ADD CONSTRAINT fk_pago_descuento
    FOREIGN KEY (id_descuento) REFERENCES DESCUENTOS(id_descuento);

COMMIT;
