-- ============================================================
-- 03_crear_triggers.sql
-- ============================================================

CREATE OR REPLACE TRIGGER TRG_AUDITORIA_RESERVAS
AFTER UPDATE OF estado ON RESERVAS
FOR EACH ROW
BEGIN
    INSERT INTO AUDITORIA (id_auditoria, id_reserva, estado_anterior, estado_nuevo)
    VALUES (SEQ_AUDITORIA.NEXTVAL, :OLD.id_reserva, :OLD.estado, :NEW.estado);
END;
/
