-- ============================================================
-- 06_datos_simulacion.sql
-- Datos de simulación para demostrar el sistema completo
-- Ejecutar DESPUÉS de 04_datos_iniciales.sql
-- ============================================================

-- ---- ESPACIOS ADICIONALES ----
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Auditorio Principal', 'AUDITORIO', 100, 'Piso 1 - Ala Sur', 150.00);

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Sala Innovacion B', 'SALA_REUNION', 8, 'Piso 2 - Ala Sur', 60.00);

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora)
VALUES (SEQ_ESPACIOS.NEXTVAL, 'Oficina Privada 02', 'OFICINA', 6, 'Piso 3 - Suite 302', 90.00);

-- ---- CLIENTES ----
INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado)
VALUES (SEQ_CLIENTES.NEXTVAL, 'Carlos', 'Mendoza', '12345678', 'carlos.mendoza@gmail.com', '987654321', 'ACTIVO');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado)
VALUES (SEQ_CLIENTES.NEXTVAL, 'Ana', 'Torres', '23456789', 'ana.torres@hotmail.com', '976543210', 'ACTIVO');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado)
VALUES (SEQ_CLIENTES.NEXTVAL, 'Luis', 'García', '34567890', 'luis.garcia@empresa.pe', '965432109', 'ACTIVO');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado)
VALUES (SEQ_CLIENTES.NEXTVAL, 'María', 'Rojas', '45678901', 'maria.rojas@gmail.com', '954321098', 'ACTIVO');

INSERT INTO CLIENTES (id_cliente, nombre, apellido, dni, email, telefono, estado)
VALUES (SEQ_CLIENTES.NEXTVAL, 'Jorge', 'Huanca', '56789012', 'jorge.huanca@outlook.com', '943210987', 'ACTIVO');

-- ---- RESERVAS ----
-- Reserva CONFIRMADA (Carlos en Sala A - 3 horas = S/. 150)
INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
VALUES (SEQ_RESERVAS.NEXTVAL,
    (SELECT id_cliente FROM CLIENTES WHERE dni='12345678'),
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Sala Reuniones A'),
    TIMESTAMP '2026-04-10 09:00:00',
    TIMESTAMP '2026-04-10 12:00:00',
    150.00, 'CONFIRMADA');

-- Reserva PENDIENTE (Ana en Coworking - 2 horas = S/. 40)
INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
VALUES (SEQ_RESERVAS.NEXTVAL,
    (SELECT id_cliente FROM CLIENTES WHERE dni='23456789'),
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Coworking Central'),
    TIMESTAMP '2026-04-11 14:00:00',
    TIMESTAMP '2026-04-11 16:00:00',
    40.00, 'PENDIENTE');

-- Reserva COMPLETADA con PAGO PENDIENTE (Luis en Oficina - 4 horas = S/. 320)
INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
VALUES (SEQ_RESERVAS.NEXTVAL,
    (SELECT id_cliente FROM CLIENTES WHERE dni='34567890'),
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Oficina Privada 01'),
    TIMESTAMP '2026-04-08 08:00:00',
    TIMESTAMP '2026-04-08 12:00:00',
    320.00, 'COMPLETADA');

-- Reserva COMPLETADA con PAGO PENDIENTE (María en Auditorio - 2 horas = S/. 300)
INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
VALUES (SEQ_RESERVAS.NEXTVAL,
    (SELECT id_cliente FROM CLIENTES WHERE dni='45678901'),
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Auditorio Principal'),
    TIMESTAMP '2026-04-09 10:00:00',
    TIMESTAMP '2026-04-09 12:00:00',
    300.00, 'COMPLETADA');

-- Reserva CANCELADA (Jorge)
INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado)
VALUES (SEQ_RESERVAS.NEXTVAL,
    (SELECT id_cliente FROM CLIENTES WHERE dni='56789012'),
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Sala Innovacion B'),
    TIMESTAMP '2026-04-12 15:00:00',
    TIMESTAMP '2026-04-12 17:00:00',
    120.00, 'CANCELADA');

-- ---- PAGOS PENDIENTES (para reservas COMPLETADAS) ----
INSERT INTO PAGOS (id_pago, id_reserva, monto, metodo_pago, estado_pago, fecha_creacion)
VALUES (SEQ_PAGOS.NEXTVAL,
    (SELECT id_reserva FROM RESERVAS WHERE id_cliente=(SELECT id_cliente FROM CLIENTES WHERE dni='34567890') AND ROWNUM=1),
    320.00, 'EFECTIVO', 'PENDIENTE', SYSDATE);

INSERT INTO PAGOS (id_pago, id_reserva, monto, metodo_pago, estado_pago, fecha_creacion)
VALUES (SEQ_PAGOS.NEXTVAL,
    (SELECT id_reserva FROM RESERVAS WHERE id_cliente=(SELECT id_cliente FROM CLIENTES WHERE dni='45678901') AND ROWNUM=1),
    300.00, 'EFECTIVO', 'PENDIENTE', SYSDATE);

-- ---- CÓDIGOS DE DESCUENTO ----
INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado)
VALUES (SEQ_DESCUENTOS.NEXTVAL, 'BIENVENIDO10', 'Descuento bienvenida 10%', 10, 0, SYSDATE, SYSDATE + 60, 50, 0, 'ACTIVO');

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado)
VALUES (SEQ_DESCUENTOS.NEXTVAL, 'PROMO25', 'Promoción especial 25% (mín S/. 100)', 25, 100, SYSDATE, SYSDATE + 30, 20, 0, 'ACTIVO');

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado)
VALUES (SEQ_DESCUENTOS.NEXTVAL, 'CORPORATE15', 'Descuento empresas 15%', 15, 200, SYSDATE, SYSDATE + 90, 100, 0, 'ACTIVO');

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado)
VALUES (SEQ_DESCUENTOS.NEXTVAL, 'VENCIDO5', 'Código vencido (no válido)', 5, 0, SYSDATE - 60, SYSDATE - 1, 10, 0, 'INACTIVO');

-- ---- EVALUACIONES ----
INSERT INTO EVALUACIONES (id_evaluacion, id_reserva, id_cliente, calificacion, comentario, fecha_evaluacion)
VALUES (SEQ_EVALUACIONES.NEXTVAL,
    (SELECT id_reserva FROM RESERVAS WHERE id_cliente=(SELECT id_cliente FROM CLIENTES WHERE dni='34567890') AND ROWNUM=1),
    (SELECT id_cliente FROM CLIENTES WHERE dni='34567890'),
    5, 'Excelente espacio, muy bien equipado y cómodo.', SYSDATE - 1);

INSERT INTO EVALUACIONES (id_evaluacion, id_reserva, id_cliente, calificacion, comentario, fecha_evaluacion)
VALUES (SEQ_EVALUACIONES.NEXTVAL,
    (SELECT id_reserva FROM RESERVAS WHERE id_cliente=(SELECT id_cliente FROM CLIENTES WHERE dni='45678901') AND ROWNUM=1),
    (SELECT id_cliente FROM CLIENTES WHERE dni='45678901'),
    4, 'Buen auditorio, el proyector funcionó perfecto.', SYSDATE);

-- ---- HORARIO BLOQUEADO (mantenimiento) ----
INSERT INTO HORARIOS_BLOQUEADOS (id_bloqueo, id_espacio, fecha_inicio, fecha_fin, razon, bloqueado_por)
VALUES (SEQ_HORARIOS.NEXTVAL,
    (SELECT id_espacio FROM ESPACIOS WHERE nombre='Sala Reuniones A'),
    TIMESTAMP '2026-04-15 07:00:00',
    TIMESTAMP '2026-04-15 09:00:00',
    'Mantenimiento preventivo mensual', 'admin');

COMMIT;
