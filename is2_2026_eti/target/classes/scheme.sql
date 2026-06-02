DROP TABLE IF EXISTS persona;

CREATE TABLE persona (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER NOT NULL UNIQUE,
    apellido TEXT,
    nombre TEXT,
    email TEXT UNIQUE,
    telefono TEXT UNIQUE,
    fecha_nacimiento DATE,
    CHECK(dni > 0)
);

DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id_user INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    id_persona INTEGER NOT NULL,
    type TEXT NOT NULL CHECK(type IN ('ADMINISTRADOR', 'DOCENTE', 'ALUMNO')),

    FOREIGN KEY (id_persona) REFERENCES persona(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS docentes;

CREATE TABLE docentes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    titulo TEXT,
    rol TEXT CHECK(rol IN ('RESPONSABLE', 'JTP', 'AYUDANTE')),

    FOREIGN KEY (dni) REFERENCES persona(dni)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS docente_facultad;

CREATE TABLE docente_facultad (
    id_docente_facultad INTEGER PRIMARY KEY AUTOINCREMENT,
    id_docente INTEGER NOT NULL,
    id_facultad INTEGER NOT NULL,

    FOREIGN KEY (id_docente) REFERENCES docentes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_facultad) REFERENCES facultad (id_facultad) ON DELETE CASCADE,
    UNIQUE(id_docente, id_facultad)
);

DROP TABLE IF EXISTS alumnos;

CREATE TABLE alumnos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    progreso FLOAT,
    fecha_registro DATE,
    tipo_alumno VARCHAR(20) DEFAULT 'INGRESANTE',

    FOREIGN KEY (dni) REFERENCES persona(dni)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (tipo_alumno) REFERENCES TipoAlumno(tipo)
        ON DELETE SET DEFAULT
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS TipoAlumno;

CREATE TABLE TipoAlumno (
    tipo VARCHAR(20) PRIMARY KEY
);
INSERT INTO TipoAlumno (tipo) VALUES ('INGRESANTE'), ('AVANZADO');


DROP TABLE IF EXISTS carrera;

CREATE TABLE carrera (
    id_carrera INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_carrera TEXT NOT NULL,
    id_facultad INTEGER,
    cant_anios INTEGER NOT NULL,

    FOREIGN KEY (id_facultad) REFERENCES facultad (id_facultad) ON DELETE CASCADE
);

DROP TABLE IF EXISTS materia;

CREATE TABLE materia (
    id_materia INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_materia TEXT NOT NULL,
    codigo TEXT NOT NULL UNIQUE,
    anio_pertenece INTEGER NOT NULL,
    cant_horas INTEGER,
    periodo TEXT
);

DROP TABLE IF EXISTS EstadoInscripcion;

CREATE TABLE EstadoInscripcion (
    estado VARCHAR(20) PRIMARY KEY
);
INSERT INTO EstadoInscripcion (estado) VALUES ('CURSANDO'), ('REGULAR'), ('APROBADA'), ('LIBRE'), ('DESAPROBADA');


DROP TABLE IF EXISTS plan_estudio;

CREATE TABLE plan_estudio (
    id_planEstudio INTEGER PRIMARY KEY AUTOINCREMENT,
    id_carrera INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,

    FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia) ON DELETE CASCADE
);

DROP TABLE IF EXISTS docente_materia;

CREATE TABLE docente_materia (
    id_DocMat INTEGER PRIMARY KEY AUTOINCREMENT,
    id_docente INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,

    FOREIGN KEY (id_docente) REFERENCES docentes(id) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia) ON DELETE CASCADE
);

DROP TABLE IF EXISTS inscripcion;

CREATE TABLE inscripcion (
    id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_alumno INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'CURSANDO',

    FOREIGN KEY (id_alumno) REFERENCES alumnos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia) ON DELETE CASCADE,
    FOREIGN KEY (estado) REFERENCES EstadoInscripcion(estado) ON UPDATE CASCADE
);

DROP TABLE IF EXISTS notas;

CREATE TABLE notas (
    id_notas INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inscripcion INTEGER NOT NULL,
    valor INTEGER,
    tipo_nota VARCHAR(20) NOT NULL,

    FOREIGN KEY (id_inscripcion) REFERENCES inscripcion(id_inscripcion) ON DELETE CASCADE,
    FOREIGN KEY (tipo_nota) REFERENCES TipoNota(tipo) ON UPDATE CASCADE
);

DROP TABLE IF EXISTS TipoNota;

CREATE TABLE TipoNota (
    tipo VARCHAR(20) PRIMARY KEY
);
INSERT INTO TipoNota (tipo) VALUES ('PARCIAL'), ('FINAL'), ('TP');

DROP TABLE IF EXISTS alumno_carrera;

CREATE TABLE alumno_carrera (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_alumno INTEGER NOT NULL,
    id_carrera INTEGER NOT NULL,

    FOREIGN KEY (id_alumno) REFERENCES alumnos(id) ON DELETE CASCADE,
    FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera) ON DELETE CASCADE
);

DROP TABLE IF EXISTS facultad;

CREATE TABLE facultad (
    id_facultad INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE
);

-- Datos iniciales
INSERT INTO facultad (nombre) VALUES
('Ciencias Exactas'),
('Ciencias Humanas'),
('Ingeniería'),
('Ciencias Económicas'),
('Ciencias de la Salud'),
('Arquitectura'),
('Derecho'),
('Agronomía'),
('Veterinaria'),
('Ciencias Sociales');