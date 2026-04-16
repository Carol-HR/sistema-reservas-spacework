-- ============================================================
-- 05_agregar_roles_y_auditoria.sql
-- Ampliaciones: Roles, Bloqueos de Horario, Auditoría
-- ============================================================

-- Crear tabla de ROLES
CREATE TABLE ROLES (
    id_rol NUMBER PRIMARY KEY,
    nombre VARCHAR2(50) NOT NULL UNIQUE,
    descripcion VARCHAR2(255),
    fecha_creacion DATE DEFAULT SYSDATE
);

CREATE SEQUENCE SEQ_ROLES START WITH 1 INCREMENT BY 1 NOCACHE;

-- Insertar roles
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (SEQ_ROLES.NEXTVAL, 'ADMINISTRADOR', 'Administrador con acceso total');
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (SEQ_ROLES.NEXTVAL, 'GERENTE', 'Gerente con acceso a reportes y cambios de estado');
INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (SEQ_ROLES.NEXTVAL, 'CLIENTE', 'Cliente con acceso limitado a sus reservas');
COMMIT;

-- Agregar columna ROL a USUARIOS (si no existe)
ALTER TABLE USUARIOS ADD (rol VARCHAR2(20) DEFAULT 'CLIENTE');
-- Actualizar al admin como ADMIN
UPDATE USUARIOS SET rol = 'ADMIN' WHERE nombre_usuario = 'admin';
COMMIT;

-- Crear tabla HORARIOS_BLOQUEADOS para no-disponibilidad
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

-- AUDITORIA se registrará desde Java al hacer cambios en reservas
-- La tabla AUDITORIA ya existe, aquí la verificamos
-- ALTER TABLE AUDITORIA ADD (cambios VARCHAR2(2000)) — si no existe

COMMIT;
