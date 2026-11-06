-- =====================================================================
-- SARA - Cuaderno Digital de Gestion Docente y Automatizacion del
-- Rendimiento Academico
-- Script 01: esquema de la base de datos (MySQL 8 / MariaDB 10.x)
-- La tabla nota queda como el resultado consolidado por corte: la nota del
-- corte es la SUMA de puntos (0 a 100). La nota final del año es el promedio
-- de los 4 cortes (25 % cada uno) y se calcula en la aplicacion y en las
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sara
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sara;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- USUARIO: docentes y administradores del sistema
-- La clave se guarda como hash SHA-256 (64 caracteres hexadecimales)
-- ---------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario  INT AUTO_INCREMENT PRIMARY KEY,
    nombres     VARCHAR(80)  NOT NULL,
    apellidos   VARCHAR(80)  NOT NULL,
    usuario     VARCHAR(30)  NOT NULL UNIQUE,
    clave       CHAR(64)     NOT NULL,
    rol         ENUM('ADMINISTRADOR','DOCENTE') NOT NULL,
    activo      TINYINT(1)   NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- GRADO y SECCION (una seccion pertenece a un grado)
-- ---------------------------------------------------------------------
CREATE TABLE grado (
    id_grado  INT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(40) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE seccion (
    id_seccion  INT AUTO_INCREMENT PRIMARY KEY,
    id_grado    INT NOT NULL,
    nombre      VARCHAR(10) NOT NULL,
    UNIQUE (id_grado, nombre),
    FOREIGN KEY (id_grado) REFERENCES grado(id_grado)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ESTUDIANTE: lo registra un usuario (relacion "registra")
-- ---------------------------------------------------------------------
CREATE TABLE estudiante (
    id_estudiante    INT AUTO_INCREMENT PRIMARY KEY,
    codigo           VARCHAR(20)  NOT NULL UNIQUE,
    nombres          VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(80)  NOT NULL,
    sexo             ENUM('M','F') NOT NULL,
    fecha_nacimiento DATE,
    direccion        VARCHAR(200),
    telefono         VARCHAR(20),
    id_usuario       INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ASIGNATURA y CORTE
-- ---------------------------------------------------------------------
CREATE TABLE asignatura (
    id_asignatura  INT AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(60) NOT NULL,
    regimen        VARCHAR(30) NOT NULL,
    UNIQUE (nombre, regimen)
) ENGINE=InnoDB;

CREATE TABLE corte (
    id_corte     INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(30) NOT NULL UNIQUE,
    descripcion  VARCHAR(100)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- MATRICULA: une al estudiante con un grado y seccion en un anio lectivo
-- Dar de baja a un estudiante = cambiar el estado (no se borra)
-- ---------------------------------------------------------------------
CREATE TABLE matricula (
    id_matricula     INT AUTO_INCREMENT PRIMARY KEY,
    id_estudiante    INT NOT NULL,
    id_grado         INT NOT NULL,
    id_seccion       INT NOT NULL,
    anio_lectivo     SMALLINT NOT NULL,
    modalidad        VARCHAR(30) NOT NULL DEFAULT 'Regular',
    fecha_matricula  DATE NOT NULL,
    estado           ENUM('ACTIVA','RETIRADA') NOT NULL DEFAULT 'ACTIVA',
    UNIQUE (id_estudiante, anio_lectivo),
    FOREIGN KEY (id_estudiante) REFERENCES estudiante(id_estudiante),
    FOREIGN KEY (id_grado)      REFERENCES grado(id_grado),
    FOREIGN KEY (id_seccion)    REFERENCES seccion(id_seccion)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ACTIVIDAD (tabla nueva): actividad evaluativa de un docente para una
-- asignatura, grado, seccion y corte, con su porcentaje y fecha.
-- Los porcentajes de un mismo corte deben sumar 100 (lo valida la app).
-- ---------------------------------------------------------------------
CREATE TABLE actividad (
    id_actividad   INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario     INT NOT NULL,
    id_asignatura  INT NOT NULL,
    id_grado       INT NOT NULL,
    id_seccion     INT NOT NULL,
    id_corte       INT NOT NULL,
    anio_lectivo   SMALLINT NOT NULL,
    nombre         VARCHAR(80) NOT NULL,
    porcentaje     DECIMAL(5,2) NOT NULL,
    fecha          DATE,
    CHECK (porcentaje > 0 AND porcentaje <= 100),
    FOREIGN KEY (id_usuario)    REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_asignatura) REFERENCES asignatura(id_asignatura),
    FOREIGN KEY (id_grado)      REFERENCES grado(id_grado),
    FOREIGN KEY (id_seccion)    REFERENCES seccion(id_seccion),
    FOREIGN KEY (id_corte)      REFERENCES corte(id_corte)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- CALIFICACION (tabla nueva): puntaje de un estudiante en una actividad
-- ---------------------------------------------------------------------
CREATE TABLE calificacion (
    id_calificacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_matricula     INT NOT NULL,
    id_actividad     INT NOT NULL,
    puntaje          DECIMAL(5,2) NOT NULL,
    UNIQUE (id_matricula, id_actividad),
    CHECK (puntaje >= 0 AND puntaje <= 100),
    FOREIGN KEY (id_matricula) REFERENCES matricula(id_matricula),
    FOREIGN KEY (id_actividad) REFERENCES actividad(id_actividad)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- NOTA: resultado consolidado por matricula, asignatura y corte
-- calif_cuantitativa = suma de puntos de las actividades del corte (0 a 100)
-- calif_cualitativa: AA (90-100), AS (76-89), AF (60-75), AI (< 60)
-- ---------------------------------------------------------------------
CREATE TABLE nota (
    id_nota             INT AUTO_INCREMENT PRIMARY KEY,
    id_matricula        INT NOT NULL,
    id_asignatura       INT NOT NULL,
    id_corte            INT NOT NULL,
    calif_cuantitativa  DECIMAL(5,2),
    calif_cualitativa   ENUM('AA','AS','AF','AI'),
    UNIQUE (id_matricula, id_asignatura, id_corte),
    FOREIGN KEY (id_matricula)  REFERENCES matricula(id_matricula),
    FOREIGN KEY (id_asignatura) REFERENCES asignatura(id_asignatura),
    FOREIGN KEY (id_corte)      REFERENCES corte(id_corte)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- ASISTENCIA
-- ---------------------------------------------------------------------
CREATE TABLE asistencia (
    id_asistencia  INT AUTO_INCREMENT PRIMARY KEY,
    id_matricula   INT NOT NULL,
    id_asignatura  INT NOT NULL,
    id_corte       INT NOT NULL,
    fecha          DATE NOT NULL,
    estado         ENUM('PRESENTE','AUSENTE','TARDE','JUSTIFICADA') NOT NULL,
    FOREIGN KEY (id_matricula)  REFERENCES matricula(id_matricula),
    FOREIGN KEY (id_asignatura) REFERENCES asignatura(id_asignatura),
    FOREIGN KEY (id_corte)      REFERENCES corte(id_corte)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- RENDIMIENTO: resumen de rendimiento academico por grado
-- ---------------------------------------------------------------------
CREATE TABLE rendimiento (
    id_rendimiento     INT AUTO_INCREMENT PRIMARY KEY,
    id_grado           INT NOT NULL,
    anio_lectivo       SMALLINT NOT NULL,
    modalidad          VARCHAR(30) NOT NULL DEFAULT 'Regular',
    matricula_inicial  INT NOT NULL DEFAULT 0,
    matricula_actual   INT NOT NULL DEFAULT 0,
    aprobados_finales  INT NOT NULL DEFAULT 0,
    aprobados_1_2      INT NOT NULL DEFAULT 0,
    aprobados_3_mas    INT NOT NULL DEFAULT 0,
    pct_retencion      DECIMAL(5,2) NOT NULL DEFAULT 0,
    UNIQUE (id_grado, anio_lectivo, modalidad),
    FOREIGN KEY (id_grado) REFERENCES grado(id_grado)
) ENGINE=InnoDB;

-- =====================================================================
-- DATOS INICIALES (catalogos y usuarios de arranque)
-- =====================================================================

-- Usuarios de arranque (cambiar las claves antes de usar en serio)
--   admin   / admin123
--   docente / docente123
INSERT INTO usuario (nombres, apellidos, usuario, clave, rol) VALUES
    ('Administrador', 'SARA', 'admin',   SHA2('admin123', 256),   'ADMINISTRADOR'),
    ('Docente', 'Demostración', 'docente', SHA2('docente123', 256), 'DOCENTE');

-- Cortes evaluativos
INSERT INTO corte (nombre, descripcion) VALUES
    ('I Corte',   'Primer corte evaluativo'),
    ('II Corte',  'Segundo corte evaluativo'),
    ('III Corte', 'Tercer corte evaluativo'),
    ('IV Corte',  'Cuarto corte evaluativo');

-- Grados: primaria (1 a 6) y secundaria (7 a 11)
INSERT INTO grado (nombre) VALUES
    ('Primer grado'), ('Segundo grado'), ('Tercer grado'), ('Cuarto grado'),
    ('Quinto grado'), ('Sexto grado'), ('Séptimo grado'), ('Octavo grado'),
    ('Noveno grado'), ('Décimo grado'), ('Undécimo grado');

-- Secciones A y B para cada grado
INSERT INTO seccion (id_grado, nombre)
SELECT g.id_grado, s.letra
FROM grado g
CROSS JOIN (SELECT 'A' AS letra UNION ALL SELECT 'B') s;

-- Asignaturas basicas (editables desde el sistema mas adelante)
INSERT INTO asignatura (nombre, regimen) VALUES
    ('Lengua y Literatura', 'Primaria'),
    ('Matemática',          'Primaria'),
    ('Ciencias Naturales',  'Primaria'),
    ('Estudios Sociales',   'Primaria'),
    ('Educación Física',    'Primaria'),
    ('Lengua y Literatura', 'Secundaria'),
    ('Matemática',          'Secundaria'),
    ('Física',              'Secundaria'),
    ('Química',             'Secundaria'),
    ('Biología',            'Secundaria'),
    ('Inglés',              'Secundaria'),
    ('Educación Física',    'Secundaria');
