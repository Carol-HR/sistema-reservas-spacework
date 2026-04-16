-- ============================================================
-- 00_crear_usuario.sql
-- Ejecutar en DBeaver conectado como: SYSTEM / MiContra123
-- Esto crea el esquema "spacework" donde vivirán las tablas
-- ============================================================

-- Crear usuario spacework (ignorar error si ya existe)
CREATE USER spacework IDENTIFIED BY spacework123;

-- Darle permisos completos
GRANT CONNECT, RESOURCE, DBA TO spacework;

-- Cuota ilimitada en todos los tablespaces comunes
ALTER USER spacework QUOTA UNLIMITED ON USERS;
ALTER USER spacework QUOTA UNLIMITED ON SYSTEM;

-- En Oracle XE también puede llamarse SYSAUX
ALTER USER spacework DEFAULT TABLESPACE USERS;

COMMIT;
