-- CARRERAS

INSERT INTO carrera (nombre_carrera, id_facultad, cant_anios) VALUES
('Ingeniería en Telecomunicaciones', 3, 6),
('Ingeniería Mecánica', 3, 6),
('Ingeniería Eléctrica', 3, 6),
('Ingeniería Química', 3, 6),
('Licenciatura en Ciencias de la Computación', 1, 5),
('Licenciatura en Química', 1, 5),
('Licenciatura en Física', 1, 5),
('Profesorado en Matemática', 1, 4),
('Licenciatura en Matemática', 1, 5),
('Ingeniería Agronómica', 8, 5),
('Medicina Veterinaria', 8, 6),
('Contador Público Nacional', 2, 5),
('Licenciatura en Economía', 2, 5),
('Profesorado en Lengua y Literatura', 2, 4),
('Profesorado en Historia', 2, 4),
('Profesorado en Geografía', 2, 4),
('Analista en Computación', 1, 3);

-- MATERIAS - INGENIERÍA EN TELECOMUNICACIONES

INSERT INTO materia (nombre_materia, codigo, anio_pertenece, cant_horas, periodo) VALUES
('Análisis Matemático I', '01101', 1, 96, '1C'),
('Álgebra y Geometría Analítica', '01102', 1, 96, '2C'),
('Introducción a la Ingeniería', '01103', 1, 48, '1C'),
('Física I', '01104', 1, 80, '2C'),
('Química General', '01105', 1, 64, '1C'),
('Análisis Matemático II', '01201', 2, 96, '1C'),
('Física II', '01202', 2, 80, '2C'),
('Probabilidad y Estadística', '01203', 2, 64, '1C'),
('Circuitos Eléctricos', '01204', 2, 80, '2C'),
('Programación', '01205', 2, 64, '2C'),
('Señales y Sistemas', '01301', 3, 80, '1C'),
('Electrónica Analógica', '01302', 3, 80, '2C'),
('Teoría Electromagnética', '01303', 3, 80, '1C'),
('Sistemas Digitales', '01304', 3, 64, '1C'),
('Ingeniería de Software', '01305', 3, 64, '2C');

-- PLAN DE ESTUDIO - TELECOMUNICACIONES

INSERT INTO plan_estudio (id_carrera, id_materia)
SELECT
    c.id_carrera,
    m.id_materia
FROM carrera c, materia m
WHERE c.nombre_carrera = 'Ingeniería en Telecomunicaciones'
AND m.codigo IN (
    '01101',
    '01102',
    '01103',
    '01104',
    '01105',
    '01201',
    '01202',
    '01203',
    '01204',
    '01205',
    '01301',
    '01302',
    '01303',
    '01304',
    '01305'
);

-- MATERIAS - LIC. EN CIENCIAS DE LA COMPUTACIÓN

INSERT INTO materia (nombre_materia, codigo, anio_pertenece, cant_horas, periodo) VALUES
('Análisis Matemático I', '05101', 1, 96, '1C'),
('Álgebra', '05102', 1, 64, '1C'),
('Introducción a la Programación', '05103', 1, 80, '1C'),
('Física I', '05104', 1, 80, '2C'),
('Lógica y Matemática Discreta', '05105', 1, 64, '2C'),
('Análisis Matemático II', '05201', 2, 96, '1C'),
('Programación Orientada a Objetos', '05202', 2, 80, '1C'),
('Estructura de Datos', '05203', 2, 80, '2C'),
('Arquitectura de Computadoras', '05204', 2, 64, '1C'),
('Probabilidad y Estadística', '05205', 2, 64, '2C'),
('Sistemas Operativos', '05301', 3, 80, '1C'),
('Bases de Datos', '05302', 3, 80, '2C'),
('Algoritmos y Complejidad', '05303', 3, 64, '1C');

-- PLAN DE ESTUDIO - LIC. COMPUTACIÓN

INSERT INTO plan_estudio (id_carrera, id_materia)
SELECT
    c.id_carrera,
    m.id_materia
FROM carrera c, materia m
WHERE c.nombre_carrera = 'Licenciatura en Ciencias de la Computación'
AND m.codigo IN (
    '05101',
    '05102',
    '05103',
    '05104',
    '05105',
    '05201',
    '05202',
    '05203',
    '05204',
    '05205',
    '05301',
    '05302',
    '05303'
);

-- MATERIAS - ANALISTA EN COMPUTACIÓN

INSERT INTO materia (nombre_materia, codigo, anio_pertenece, cant_horas, periodo) VALUES

('INTRODUCCIÓN A LA COMPUTACIÓN Y PROGRAMACIÓN I', '3410', 1, 112, '1C'),
('INTRODUCCIÓN A LA MATEMÁTICA', '3376', 1, 112, '1C'),
('LÓGICA Y RESOLUCIÓN DE PROBLEMAS', '3377', 1, 112, '1C'),

('INTRODUCCIÓN A LA COMPUTACIÓN Y PROGRAMACIÓN II', '3411', 1, 112, '2C'),
('MATEMÁTICA DISCRETA', '3379', 1, 140, '2C'),

('ESTRUCTURA DE DATOS Y ALGORITMOS', '3412', 2, 112, '1C'),
('ORGANIZACIÓN DE COMPUTADORAS', '3381', 2, 112, '1C'),
('COMPUTACIÓN Y SOCIEDAD', '3382', 2, 56, '1C'),
('INGLÉS I', '3402', 2, 56, '1C'),

('ANÁLISIS Y DISEÑO DE ALGORITMOS I', '3383', 2, 112, '2C'),
('BASES DE DATOS', '3384', 2, 112, '2C'),
('INGENIERÍA DE SOFTWARE I', '3385', 2, 112, '2C'),
('INGLÉS II', '3403', 2, 56, '2C'),

('PARADIGMAS Y LENGUAJES DE PROGRAMACIÓN', '3386', 3, 112, '1C'),
('INGENIERÍA DE SOFTWARE II', '3387', 3, 112, '1C'),
('SISTEMAS OPERATIVOS Y REDES', '3388', 3, 112, '1C'),

('SISTEMAS DISTRIBUIDOS', '3390', 3, 112, '2C'),
('SEMINARIO DE REDACCIÓN INFORMATIVA', '3389', 3, 56, '2C'),
('TESTING DE SOFTWARE', '3347', 3, 112, '2C');

-- PLAN DE ESTUDIO - ANALISTA EN COMPUTACIÓN

INSERT INTO plan_estudio (id_carrera, id_materia)
SELECT
    c.id_carrera,
    m.id_materia
FROM carrera c, materia m
WHERE c.nombre_carrera = 'Analista en Computación'
AND m.codigo IN (
    '3410',
    '3376',
    '3377',
    '3411',
    '3379',
    '3412',
    '3381',
    '3382',
    '3402',
    '3383',
    '3384',
    '3385',
    '3403',
    '3386',
    '3387',
    '3388',
    '3390',
    '3389',
    '3347'
);

-- CONSULTA DE VERIFICACIÓN

SELECT
    c.nombre_carrera,
    m.nombre_materia,
    m.codigo,
    m.anio_pertenece,
    m.periodo
FROM plan_estudio pe
JOIN carrera c
    ON c.id_carrera = pe.id_carrera
JOIN materia m
    ON m.id_materia = pe.id_materia
ORDER BY c.nombre_carrera, m.anio_pertenece;


-- =========================================================================
-- SEED DE USUARIOS, DOCENTES, ALUMNOS Y RELACIONES ACADÉMICAS
-- Contraseña para todos los usuarios: 123456
-- (Hash BCrypt: $2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny)
-- =========================================================================

-- 1. PERSONAS
INSERT INTO persona (dni, apellido, nombre, email, telefono, fecha_nacimiento) VALUES
(11111111, 'General', 'Admin', 'admin@unrc.edu.ar', '3584000101', '1985-05-15'),
(22222222, 'Feynman', 'Richard', 'feynman@unrc.edu.ar', '3584000102', '1918-05-11'),
(33333333, 'Curie', 'Marie', 'curie@unrc.edu.ar', '3584000103', '1867-11-07'),
(44444444, 'Olivero', 'Leandro', 'leandro@unrc.edu.ar', '3584000104', '2001-09-20'),
(55555555, 'Perez', 'Juan', 'juan@unrc.edu.ar', '3584000105', '2002-01-15');

-- 2. USUARIOS (Vínculo con Persona y Roles)
INSERT INTO users (name, password, id_persona, type) VALUES
('admin', '$2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny', 1, 'ADMINISTRADOR'),
('richard', '$2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny', 2, 'DOCENTE'),
('marie', '$2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny', 3, 'DOCENTE'),
('leandro', '$2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny', 4, 'ALUMNO'),
('juan', '$2a$10$abcdefghijklmnopqrstuOUA0wxOWsBQRGEiQVXQvtvZeQCKZP7Ny', 5, 'ALUMNO');

-- 3. DOCENTES
INSERT INTO docentes (dni, titulo, rol) VALUES
(22222222, 'Licenciado en Física', 'RESPONSABLE'),
(33333333, 'Doctora en Química', 'JTP');

-- Asignar facultades iniciales a los docentes
-- Richard en Ciencias Exactas (id_facultad = 1) e Ingeniería (id_facultad = 3)
INSERT INTO docente_facultad (id_docente, id_facultad) VALUES
(1, 1),
(1, 3),
(2, 1);

-- 4. ALUMNOS
INSERT INTO alumnos (dni, progreso, fecha_registro, tipo_alumno) VALUES
(44444444, 0.0, '2026-03-01', 'INGRESANTE'),
(55555555, 0.0, '2026-03-02', 'INGRESANTE');

-- 5. INSCRIPCIÓN DE ALUMNOS A CARRERA
-- Ambos anotados a Analista en Computación
INSERT INTO alumno_carrera (id_alumno, id_carrera)
SELECT al.id, c.id_carrera 
FROM alumnos al, carrera c 
WHERE al.dni IN (44444444, 55555555) 
AND c.nombre_carrera = 'Analista en Computación';

-- 6. ASIGNACIÓN DOCENTE A MATERIA
-- Richard Feynman asignado a Ingeniería de Software II (Código: 3387)
INSERT INTO docente_materia (id_docente, id_materia)
SELECT d.id, m.id_materia 
FROM docentes d, materia m 
WHERE d.dni = 22222222 
AND m.codigo = '3387';

-- 7. INSCRIPCIÓN DE ALUMNO A MATERIA
-- Leandro inscripto a Ingeniería de Software II (Código: 3387) en estado CURSANDO
INSERT INTO inscripcion (id_alumno, id_materia, estado)
SELECT al.id, m.id_materia, 'CURSANDO'
FROM alumnos al, materia m
WHERE al.dni = 44444444
AND m.codigo = '3387';


