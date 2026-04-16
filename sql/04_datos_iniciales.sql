-- ============================================================
-- 04_datos_iniciales.sql
-- Usuario admin: username=admin / password=admin123
-- SHA-256 de "admin123" = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- ============================================================

INSERT INTO USUARIOS (id_usuario, username, password_hash, nombre, email, rol, estado)
VALUES (SEQ_USUARIOS.NEXTVAL, 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        'Administrador', 'admin@spacework.pe', 'ADMINISTRADOR', 'ACTIVO');

-- Espacios de ejemplo
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Sala Reuniones A', 'SALA_REUNION', 10, 'Piso 2 - Ala Norte', 50.00);

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Coworking Central', 'COWORKING', 30, 'Piso 1', 20.00);

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Oficina Privada 01', 'OFICINA', 4, 'Piso 3 - Suite 301', 80.00);

COMMIT;
