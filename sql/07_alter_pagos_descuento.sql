-- ============================================================
-- 07_alter_pagos_descuento.sql
-- Agrega columnas de descuento a la tabla PAGOS
-- Ejecutar UNA SOLA VEZ conectado como usuario: spacework
-- ============================================================

-- Columna para el monto descontado (cuántos soles se aplicó de descuento)
ALTER TABLE PAGOS ADD descuento_aplicado NUMBER(10,2) DEFAULT 0;

-- Columna FK al código de descuento utilizado (nullable: puede no haber descuento)
ALTER TABLE PAGOS ADD id_descuento NUMBER;

-- Constraint FK (opcional, depende de si ya existe la tabla DESCUENTOS)
ALTER TABLE PAGOS ADD CONSTRAINT fk_pago_descuento
    FOREIGN KEY (id_descuento) REFERENCES DESCUENTOS(id_descuento);

COMMIT;
