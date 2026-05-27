-- Script para PROBAR carga de imagen en la BD
-- Crea una imagen base64 de prueba para un espacio

-- Imagen PNG de 1x1 pixel rojo en base64
UPDATE ESPACIOS 
SET imagen_url = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==' 
WHERE id_espacio = 46;

-- Verificar
SELECT id_espacio, nombre, 
       CASE WHEN imagen_url IS NOT NULL THEN '✓ Tiene imagen' ELSE '✗ Sin imagen' END as estado
FROM ESPACIOS 
WHERE id_espacio = 46;

COMMIT;
