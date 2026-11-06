-- =====================================================================
-- SARA - Script 02: datos de prueba (OPCIONAL)
-- =====================================================================
USE sara;
SET NAMES utf8mb4;

-- Estudiantes registrados por el usuario "docente"
INSERT INTO estudiante (codigo, nombres, apellidos, sexo, fecha_nacimiento, id_usuario)
SELECT v.codigo, v.nombres, v.apellidos, v.sexo, v.fecha, u.id_usuario
FROM usuario u
JOIN (
    SELECT 'E-001' AS codigo, 'Ana'    AS nombres, 'Lopez'    AS apellidos, 'F' AS sexo, '2013-03-14' AS fecha UNION ALL
    SELECT 'E-002', 'Carlos', 'Mendoza',  'M', '2013-07-02' UNION ALL
    SELECT 'E-003', 'Sofía',  'Rivas',    'F', '2012-11-20' UNION ALL
    SELECT 'E-004', 'Luis',   'Castillo', 'M', '2013-01-09' UNION ALL
    SELECT 'E-005', 'María',  'Gómez',    'F', '2013-05-27'
) v
WHERE u.usuario = 'docente';

-- Matricula 2026 en Septimo grado, seccion A
INSERT INTO matricula (id_estudiante, id_grado, id_seccion, anio_lectivo, fecha_matricula)
SELECT e.id_estudiante, g.id_grado, s.id_seccion, 2026, '2026-02-01'
FROM estudiante e
JOIN grado g   ON g.nombre = 'Séptimo grado'
JOIN seccion s ON s.id_grado = g.id_grado AND s.nombre = 'A'
WHERE e.codigo IN ('E-001', 'E-002', 'E-003', 'E-004', 'E-005');

-- Actividades de Matematica (secundaria): las mismas 3 en los 4 cortes
INSERT INTO actividad (id_usuario, id_asignatura, id_grado, id_seccion, id_corte,
                       anio_lectivo, nombre, porcentaje, fecha)
SELECT u.id_usuario, a.id_asignatura, g.id_grado, s.id_seccion, c.id_corte,
       2026, v.nombre, v.porcentaje, v.fecha
FROM usuario u
JOIN asignatura a ON a.nombre = 'Matemática' AND a.regimen = 'Secundaria'
JOIN grado g      ON g.nombre = 'Séptimo grado'
JOIN seccion s    ON s.id_grado = g.id_grado AND s.nombre = 'A'
JOIN corte c
JOIN (
    SELECT 'Quiz 1' AS nombre, 20.00 AS porcentaje, '2026-03-10' AS fecha UNION ALL
    SELECT 'Tarea',  30.00, '2026-03-20' UNION ALL
    SELECT 'Examen', 50.00, '2026-04-05'
) v
WHERE u.usuario = 'docente';

-- Calificaciones: cada estudiante tiene un rendimiento base (fraccion de los
-- puntos maximos) que varia un poco por actividad y por corte.
-- puntaje = maximo de la actividad x fraccion  (nunca pasa del maximo)
INSERT INTO calificacion (id_matricula, id_actividad, puntaje)
SELECT m.id_matricula, a.id_actividad,
       ROUND(a.porcentaje * LEAST(1, GREATEST(0, p.base + v.ajuste_actividad + w.ajuste_corte)), 2)
FROM (
    SELECT 'E-001' AS codigo, 0.95 AS base UNION ALL
    SELECT 'E-002', 0.82 UNION ALL
    SELECT 'E-003', 0.70 UNION ALL
    SELECT 'E-004', 0.52 UNION ALL
    SELECT 'E-005', 0.88
) p
JOIN estudiante e ON e.codigo = p.codigo
JOIN matricula m  ON m.id_estudiante = e.id_estudiante AND m.anio_lectivo = 2026
JOIN actividad a  ON a.id_grado = m.id_grado AND a.id_seccion = m.id_seccion
                 AND a.anio_lectivo = m.anio_lectivo
JOIN asignatura asig ON asig.id_asignatura = a.id_asignatura
                    AND asig.nombre = 'Matemática' AND asig.regimen = 'Secundaria'
JOIN corte c      ON c.id_corte = a.id_corte
JOIN (
    SELECT 'Quiz 1' AS nombre, -0.03 AS ajuste_actividad UNION ALL
    SELECT 'Tarea',   0.02 UNION ALL
    SELECT 'Examen', -0.01
) v ON v.nombre = a.nombre
JOIN (
    SELECT 'I Corte' AS corte, 0.00 AS ajuste_corte UNION ALL
    SELECT 'II Corte',  0.02 UNION ALL
    SELECT 'III Corte', -0.03 UNION ALL
    SELECT 'IV Corte',  0.01
) w ON w.corte = c.nombre
WHERE NOT (p.codigo = 'E-005' AND c.nombre = 'IV Corte');

-- Nota de cada corte = suma de los puntos de sus actividades
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

-- ---------------------------------------------------------------------
-- Consulta de prueba: nota de cada corte y nota final por estudiante
-- nota final = promedio de los 4 cortes (cada corte aporta 25 %)
-- ---------------------------------------------------------------------
SELECT e.codigo,
       CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
       MAX(CASE WHEN c.nombre = 'I Corte'   THEN n.calif_cuantitativa END) AS i_corte,
       MAX(CASE WHEN c.nombre = 'II Corte'  THEN n.calif_cuantitativa END) AS ii_corte,
       MAX(CASE WHEN c.nombre = 'III Corte' THEN n.calif_cuantitativa END) AS iii_corte,
       MAX(CASE WHEN c.nombre = 'IV Corte'  THEN n.calif_cuantitativa END) AS iv_corte,
       CASE WHEN COUNT(*) = 4 THEN ROUND(SUM(n.calif_cuantitativa) / 4, 2) END AS nota_final
FROM nota n
JOIN corte c      ON c.id_corte = n.id_corte
JOIN matricula m  ON m.id_matricula = n.id_matricula
JOIN estudiante e ON e.id_estudiante = m.id_estudiante
WHERE e.codigo LIKE 'E-00_'
GROUP BY e.id_estudiante, e.codigo, e.nombres, e.apellidos
ORDER BY e.codigo;
