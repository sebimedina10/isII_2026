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
    id_persona INTEGER,
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
    rol TEXT,

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