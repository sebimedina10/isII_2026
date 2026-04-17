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

DROP TABLE IF EXISTS alumnos;

CREATE TABLE alumnos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dni INTEGER,
    progreso FLOAT,
    fecha_registro DATE,

    FOREIGN KEY (dni) REFERENCES persona(dni)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

DROP TABLE IF EXISTS carrera;

CREATE TABLE carrera (
    id_carrera INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_carrera TEXT NOT NULL,
    facultad TEXT NOT NULL,
    cant_anios INTEGER NOT NULL
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

DROP TABLE IF EXISTS plan_estudio;

CREATE TABLE plan_estudio (
    id_planEstudio INTEGER PRIMARY KEY AUTOINCREMENT,
    id_carrera INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,

    FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera),
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia)
);

DROP TABLE IF EXISTS docente_materia;

CREATE TABLE docente_materia (
    id_DocMat INTEGER PRIMARY KEY AUTOINCREMENT,
    id_docente INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,

    FOREIGN KEY (id_docente) REFERENCES docentes(id),
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia)
);

DROP TABLE IF EXISTS inscripcion;

CREATE TABLE inscripcion (
    id_inscripcion INTEGER PRIMARY KEY AUTOINCREMENT,
    id_alumno INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,
    estado TEXT CHECK(estado IN ('CURSANDO', 'APROBADA', 'DESAPROBADA')),

    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (id_materia) REFERENCES materia(id_materia)
);

DROP TABLE IF EXISTS notas;

CREATE TABLE notas (
    id_notas INTEGER PRIMARY KEY AUTOINCREMENT,
    id_inscripcion INTEGER NOT NULL,
    nota_parcial FLOAT,
    nota_final FLOAT,

    FOREIGN KEY (id_inscripcion) REFERENCES inscripcion(id_inscripcion)
);

DROP TABLE IF EXISTS alumno_carrera;

CREATE TABLE alumno_carrera (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_alumno INTEGER NOT NULL,
    id_carrera INTEGER NOT NULL,

    FOREIGN KEY (id_alumno) REFERENCES alumnos(id),
    FOREIGN KEY (id_carrera) REFERENCES carrera(id_carrera)
);