-- =====================================================================
-- SARA - Script 03: consultas SQL principales
-- Son las mismas consultas que usa el sistema (en las clases DAO).
-- Antes de ejecutarlas se eligen los datos del grupo en las variables.
-- Requiere MySQL 8 o MariaDB 10.2 o superior (usa RANK()).
-- =====================================================================
USE sara;
SET NAMES utf8mb4;

-- Variables: cambiar segun el grupo que se quiere consultar
SET @anio       = 2026;
SET @id_grado   = (SELECT id_grado FROM grado WHERE nombre = 'Séptimo grado');
SET @id_seccion = (SELECT id_seccion FROM seccion WHERE id_grado = @id_grado AND nombre = 'A');
SET @id_asig    = (SELECT id_asignatura FROM asignatura WHERE nombre = 'Matemática' AND regimen = 'Secundaria');
SET @id_corte   = (SELECT id_corte FROM corte WHERE nombre = 'I Corte');

-- ---------------------------------------------------------------------
-- 1) Estudiantes activos por grupo (grado y seccion) en un anio
-- ---------------------------------------------------------------------
SELECT g.nombre AS grado, s.nombre AS seccion, COUNT(m.id_matricula) AS estudiantes_activos
FROM seccion s
JOIN grado g ON g.id_grado = s.id_grado
LEFT JOIN matricula m ON m.id_seccion = s.id_seccion
                     AND m.anio_lectivo = @anio AND m.estado = 'ACTIVA'
GROUP BY g.id_grado, g.nombre, s.nombre
HAVING COUNT(m.id_matricula) > 0
ORDER BY g.id_grado, s.nombre;

-- ---------------------------------------------------------------------
-- 2) Actividades de un corte y suma de sus puntos maximos (debe ser 100)
-- ---------------------------------------------------------------------
SELECT a.nombre AS actividad, a.porcentaje, a.fecha
FROM actividad a
WHERE a.id_grado = @id_grado AND a.id_seccion = @id_seccion AND a.id_asignatura = @id_asig
  AND a.id_corte = @id_corte AND a.anio_lectivo = @anio
ORDER BY a.fecha, a.id_actividad;

-- Cortes cuyos puntos maximos NO suman 100 (para revisar configuraciones incompletas)
SELECT g.nombre AS grado, s.nombre AS seccion, asig.nombre AS asignatura, c.nombre AS corte,
       SUM(a.porcentaje) AS suma_porcentajes
FROM actividad a
JOIN grado g       ON g.id_grado = a.id_grado
JOIN seccion s     ON s.id_seccion = a.id_seccion
JOIN asignatura asig ON asig.id_asignatura = a.id_asignatura
JOIN corte c       ON c.id_corte = a.id_corte
WHERE a.anio_lectivo = @anio
GROUP BY a.id_usuario, g.nombre, s.nombre, asig.nombre, c.nombre, a.id_grado, a.id_seccion,
         a.id_asignatura, a.id_corte
HAVING SUM(a.porcentaje) <> 100;

-- ---------------------------------------------------------------------
-- 3) Nota del corte y nivel de logro de cada estudiante
--    nota del corte = suma de los puntos de sus actividades (0 a 100)
--    aporte a la final = nota del corte x 25 / 100
-- ---------------------------------------------------------------------
SELECT e.codigo,
       CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
       ROUND(SUM(c.puntaje), 2) AS nota_del_corte,
       ROUND(SUM(c.puntaje) * 25 / 100, 2) AS aporte_a_la_final,
       CASE
           WHEN SUM(c.puntaje) >= 90 THEN 'AA'
           WHEN SUM(c.puntaje) >= 76 THEN 'AS'
           WHEN SUM(c.puntaje) >= 60 THEN 'AF'
           ELSE 'AI'
       END AS nivel_logro
FROM calificacion c
JOIN actividad a  ON a.id_actividad = c.id_actividad
JOIN matricula m  ON m.id_matricula = c.id_matricula
JOIN estudiante e ON e.id_estudiante = m.id_estudiante
WHERE a.id_grado = @id_grado AND a.id_seccion = @id_seccion AND a.id_asignatura = @id_asig
  AND a.id_corte = @id_corte AND a.anio_lectivo = @anio AND m.estado = 'ACTIVA'
GROUP BY e.id_estudiante, e.codigo, e.nombres, e.apellidos
ORDER BY e.apellidos, e.nombres;

-- ---------------------------------------------------------------------
-- 4) Ranking del grupo (los empates comparten posicion)
-- ---------------------------------------------------------------------
SELECT RANK() OVER (ORDER BY n.calif_cuantitativa DESC) AS posicion,
       e.codigo,
       CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
       n.calif_cuantitativa AS nota_del_corte,
       n.calif_cualitativa  AS nivel
FROM nota n
JOIN matricula m  ON m.id_matricula = n.id_matricula
JOIN estudiante e ON e.id_estudiante = m.id_estudiante
WHERE m.id_grado = @id_grado AND m.id_seccion = @id_seccion AND m.anio_lectivo = @anio
  AND m.estado = 'ACTIVA' AND n.id_asignatura = @id_asig AND n.id_corte = @id_corte
ORDER BY posicion, e.apellidos;

-- ---------------------------------------------------------------------
-- 5) Alumnos en riesgo academico: nota del corte menor a 60
-- ---------------------------------------------------------------------
SELECT e.codigo, CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
       asig.nombre AS asignatura, n.calif_cuantitativa AS nota_del_corte, n.calif_cualitativa AS nivel
FROM nota n
JOIN matricula m     ON m.id_matricula = n.id_matricula
JOIN estudiante e    ON e.id_estudiante = m.id_estudiante
JOIN asignatura asig ON asig.id_asignatura = n.id_asignatura
WHERE m.id_grado = @id_grado AND m.id_seccion = @id_seccion AND m.anio_lectivo = @anio
  AND m.estado = 'ACTIVA' AND n.id_corte = @id_corte AND n.calif_cuantitativa < 60
ORDER BY e.apellidos, e.nombres, asig.nombre;

-- ---------------------------------------------------------------------
-- 6) Boletin de la seccion: nota de cada corte, acumulado, nota final y letra
--    nota final = promedio de los 4 cortes (25 % cada uno); solo existe si
--    el estudiante ya tiene los 4 cortes con nota. Si no, queda NULL (En proceso).
--    Para un solo estudiante agregar:  AND e.codigo = 'E-001'
-- ---------------------------------------------------------------------
SELECT t.codigo, t.estudiante, t.asignatura,
       t.i_corte, t.ii_corte, t.iii_corte, t.iv_corte,
       t.acumulado, t.nota_final,
       CASE WHEN t.nota_final IS NULL THEN NULL
            WHEN t.nota_final >= 90 THEN 'AA'
            WHEN t.nota_final >= 76 THEN 'AS'
            WHEN t.nota_final >= 60 THEN 'AF'
            ELSE 'AI' END AS nivel,
       CASE WHEN t.nota_final IS NULL THEN NULL
            WHEN t.nota_final >= 60 THEN 'Aprobada'
            ELSE 'Reprobada' END AS resultado
FROM (SELECT e.codigo,
             CONCAT(e.nombres, ' ', e.apellidos) AS estudiante,
             e.apellidos, e.nombres,
             asig.nombre AS asignatura,
             MAX(CASE WHEN c.nombre = 'I Corte'   THEN n.calif_cuantitativa END) AS i_corte,
             MAX(CASE WHEN c.nombre = 'II Corte'  THEN n.calif_cuantitativa END) AS ii_corte,
             MAX(CASE WHEN c.nombre = 'III Corte' THEN n.calif_cuantitativa END) AS iii_corte,
             MAX(CASE WHEN c.nombre = 'IV Corte'  THEN n.calif_cuantitativa END) AS iv_corte,
             ROUND(SUM(n.calif_cuantitativa) / 4, 2) AS acumulado,
             CASE WHEN COUNT(*) = 4 THEN ROUND(SUM(n.calif_cuantitativa) / 4, 2) END AS nota_final
      FROM nota n
      JOIN corte c         ON c.id_corte = n.id_corte
      JOIN matricula m     ON m.id_matricula = n.id_matricula
      JOIN estudiante e    ON e.id_estudiante = m.id_estudiante
      JOIN asignatura asig ON asig.id_asignatura = n.id_asignatura
      WHERE m.id_grado = @id_grado AND m.id_seccion = @id_seccion AND m.anio_lectivo = @anio
        AND m.estado = 'ACTIVA'
      GROUP BY e.id_estudiante, e.codigo, e.nombres, e.apellidos, asig.id_asignatura, asig.nombre) t
ORDER BY t.apellidos, t.nombres, t.asignatura;

-- ---------------------------------------------------------------------
-- 7) Promedio del grupo (de las notas de corte) por asignatura y corte
-- ---------------------------------------------------------------------
SELECT asig.nombre AS asignatura, c.nombre AS corte,
       ROUND(AVG(n.calif_cuantitativa), 2) AS promedio_del_grupo,
       COUNT(*) AS estudiantes_con_nota
FROM nota n
JOIN matricula m     ON m.id_matricula = n.id_matricula
JOIN asignatura asig ON asig.id_asignatura = n.id_asignatura
JOIN corte c         ON c.id_corte = n.id_corte
WHERE m.id_grado = @id_grado AND m.id_seccion = @id_seccion AND m.anio_lectivo = @anio
  AND m.estado = 'ACTIVA'
GROUP BY asig.id_asignatura, asig.nombre, c.id_corte, c.nombre
ORDER BY asig.nombre, c.id_corte;

-- ---------------------------------------------------------------------
-- 8) Cantidad de estudiantes por nivel de logro (distribucion del grupo)
-- ---------------------------------------------------------------------
SELECT n.calif_cualitativa AS nivel, COUNT(*) AS estudiantes
FROM nota n
JOIN matricula m ON m.id_matricula = n.id_matricula
WHERE m.id_grado = @id_grado AND m.id_seccion = @id_seccion AND m.anio_lectivo = @anio
  AND m.estado = 'ACTIVA' AND n.id_asignatura = @id_asig AND n.id_corte = @id_corte
GROUP BY n.calif_cualitativa
ORDER BY FIELD(n.calif_cualitativa, 'AA', 'AS', 'AF', 'AI');

-- ---------------------------------------------------------------------
-- 9) Rendimiento del grado: asignaturas reprobadas por estudiante
--    Una asignatura se reprueba si su nota final (promedio de los 4 cortes)
--    es menor a 60. Solo cuentan las asignaturas con los 4 cortes ya calificados.
-- ---------------------------------------------------------------------
SELECT m.modalidad,
       COUNT(*) AS estudiantes_con_notas,
       SUM(CASE WHEN r.reprobadas = 0 THEN 1 ELSE 0 END)               AS aprobaron_todas,
       SUM(CASE WHEN r.reprobadas BETWEEN 1 AND 2 THEN 1 ELSE 0 END)   AS reprobaron_1_o_2,
       SUM(CASE WHEN r.reprobadas >= 3 THEN 1 ELSE 0 END)              AS reprobaron_3_o_mas
FROM matricula m
JOIN (SELECT x.id_matricula,
             SUM(CASE WHEN x.nota_final < 60 THEN 1 ELSE 0 END) AS reprobadas
      FROM (SELECT id_matricula, id_asignatura,
                   ROUND(SUM(calif_cuantitativa) / 4, 2) AS nota_final
            FROM nota GROUP BY id_matricula, id_asignatura
            HAVING COUNT(*) = 4) x
      GROUP BY x.id_matricula) r ON r.id_matricula = m.id_matricula
WHERE m.id_grado = @id_grado AND m.anio_lectivo = @anio AND m.estado = 'ACTIVA'
GROUP BY m.modalidad;

-- ---------------------------------------------------------------------
-- 10) Retencion: matricula inicial vs matricula actual por grado
-- ---------------------------------------------------------------------
SELECT g.nombre AS grado,
       COUNT(*) AS matricula_inicial,
       SUM(CASE WHEN m.estado = 'ACTIVA' THEN 1 ELSE 0 END) AS matricula_actual,
       ROUND(SUM(CASE WHEN m.estado = 'ACTIVA' THEN 1 ELSE 0 END) * 100 / COUNT(*), 2) AS pct_retencion
FROM matricula m
JOIN grado g ON g.id_grado = m.id_grado
WHERE m.anio_lectivo = @anio
GROUP BY g.id_grado, g.nombre
ORDER BY g.id_grado;
