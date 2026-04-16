-- ============================================================
-- 08_alter_metodo_pago_nullable.sql
-- Hace que metodo_pago sea nullable en PAGOS
-- Necesario para que al "Completar" una reserva se pueda
-- crear un pago sin método aún definido.
-- Ejecutar UNA SOLA VEZ conectado como usuario: JUAN (o tu usuario Oracle)
-- ============================================================

-- 1. Cambiar a default EFECTIVO para evitar NULL
UPDATE PAGOS SET metodo_pago = 'EFECTIVO' WHERE metodo_pago IS NULL;

-- 2. Eliminar el constraint CHECK existente
BEGIN
  EXECUTE IMMEDIATE 'ALTER TABLE PAGOS DROP CONSTRAINT CHK_METODO_PAGO';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;
/

-- 3. Hacer metodo_pago nullable explícitamente
ALTER TABLE PAGOS MODIFY metodo_pago VARCHAR2(30) NULL;

-- 4. Recrear el CHECK permitiendo NULL (pago pendiente sin método - opcional cambiar después)
ALTER TABLE PAGOS ADD CONSTRAINT CHK_METODO_PAGO 
CHECK (metodo_pago IS NULL OR metodo_pago IN ('TARJETA', 'TRANSFERENCIA', 'EFECTIVO', 'CHEQUE'));


ALTER TABLE NOTIFICACIONES DROP CONSTRAINT CHK_TIPO_NOTIFICACION;
ALTER TABLE NOTIFICACIONES ADD CONSTRAINT CHK_TIPO_NOTIFICACION CHECK (tipo IN ('RESERVA', 'PAGO', 'RECORDATORIO', 'PROMOCION', 'SISTEMA', 'EVALUACION'));

COMMIT;
