-- =====================================================================
-- SARA - Script 04: MIGRACION a la version 5 (OPCIONAL)
-- Para quien YA tiene la base "sara" creada con el script 01 de la v4
-- y no quiere perder sus datos. Si va a crear la base desde cero, NO
-- hace falta: el script 01 nuevo ya trae todo.
--
-- Que hace:
--   1) Agrega el IV Corte (si no existe).
--   2) Convierte los datos de ejemplo de Septimo A (Matematica) de la
--      escala vieja (0 a 100) a puntos, solo si todavia no estan convertidos.
--   3) Recalcula la tabla nota: la nota de cada corte pasa a ser la
--      SUMA de puntos (antes era un promedio ponderado).
--
-- Tus puntajes de Segundo C (8, 13, 22, 20, 15...) ya estan en puntos:
-- no se tocan, solo se recalcula su total.
-- Se puede ejecutar mas de una vez sin danar nada.
-- =====================================================================
USE sara;
SET NAMES utf8mb4;
SET SQL_SAFE_UPDATES = 0;

-- 1) IV Corte
INSERT INTO corte (nombre, descripcion)
SELECT 'IV Corte', 'Cuarto corte evaluativo'
WHERE NOT EXISTS (SELECT 1 FROM corte WHERE nombre = 'IV Corte');

-- 2) Datos de ejemplo viejos (E-001 a E-005, Matematica de Septimo A):
--    tenian notas de 0 a 100 en actividades de 20 / 30 / 50 %.
--    Si alguna pasa del maximo de su actividad, se convierten todas a puntos.
SET @sin_convertir = (
    SELECT COUNT(*)
    FROM calificacion c
    JOIN actividad a     ON a.id_actividad = c.id_actividad
    JOIN asignatura asig ON asig.id_asignatura = a.id_asignatura
    JOIN grado g         ON g.id_grado = a.id_grado
    JOIN seccion s       ON s.id_seccion = a.id_seccion
    JOIN matricula m     ON m.id_matricula = c.id_matricula
    JOIN estudiante e    ON e.id_estudiante = m.id_estudiante
    WHERE asig.nombre = 'Matemática' AND asig.regimen = 'Secundaria'
      AND g.nombre = 'Séptimo grado' AND s.nombre = 'A'
      AND e.codigo IN ('E-001', 'E-002', 'E-003', 'E-004', 'E-005')
      AND a.nombre IN ('Quiz 1', 'Tarea', 'Examen')
      AND c.puntaje > a.porcentaje
);

UPDATE calificacion c
JOIN actividad a     ON a.id_actividad = c.id_actividad
JOIN asignatura asig ON asig.id_asignatura = a.id_asignatura
JOIN grado g         ON g.id_grado = a.id_grado
JOIN seccion s       ON s.id_seccion = a.id_seccion
JOIN matricula m     ON m.id_matricula = c.id_matricula
JOIN estudiante e    ON e.id_estudiante = m.id_estudiante
SET c.puntaje = ROUND(c.puntaje * a.porcentaje / 100, 2)
WHERE @sin_convertir > 0
  AND asig.nombre = 'Matemática' AND asig.regimen = 'Secundaria'
  AND g.nombre = 'Séptimo grado' AND s.nombre = 'A'
  AND e.codigo IN ('E-001', 'E-002', 'E-003', 'E-004', 'E-005')
  AND a.nombre IN ('Quiz 1', 'Tarea', 'Examen');

-- Revision: calificaciones que todavia pasan del maximo de su actividad.
-- Si esta consulta devuelve filas, son notas tuyas que hay que corregir
-- en el registro de notas (no se corrigen solas).
SELECT e.codigo, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
       a.nombre AS actividad, a.porcentaje AS maximo, c.puntaje
FROM calificacion c
JOIN actividad a  ON a.id_actividad = c.id_actividad
JOIN matricula m  ON m.id_matricula = c.id_matricula
JOIN estudiante e ON e.id_estudiante = m.id_estudiante
WHERE c.puntaje > a.porcentaje;

-- 3) Recalcular toda la tabla nota desde las calificaciones
--    (nota del corte = suma de puntos)
DELETE FROM nota;

INSERT INTO nota (id_matricula, id_asignatura, id_corte, calif_cuantitativa, calif_cualitativa)
SELECT t.id_matricula, t.id_asignatura, t.id_corte, t.total,
       CASE WHEN t.total >= 90 THEN 'AA'
            WHEN t.total >= 76 THEN 'AS'
            WHEN t.total >= 60 THEN 'AF'
            ELSE 'AI' END
FROM (SELECT c.id_matricula, a.id_asignatura, a.id_corte, ROUND(SUM(c.puntaje), 2) AS total
      FROM calificacion c
      JOIN actividad a ON a.id_actividad = c.id_actividad
      GROUP BY c.id_matricula, a.id_asignatura, a.id_corte) t;

SET SQL_SAFE_UPDATES = 1;

-- Resultado: cuantas notas quedaron y los cortes disponibles
SELECT (SELECT COUNT(*) FROM nota)  AS notas_recalculadas,
       (SELECT GROUP_CONCAT(nombre ORDER BY id_corte SEPARATOR ', ') FROM corte) AS cortes;
