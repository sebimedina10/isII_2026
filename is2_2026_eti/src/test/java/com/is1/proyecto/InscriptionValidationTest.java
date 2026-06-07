package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;

public class InscriptionValidationTest {

    @BeforeEach
    void setUp() {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:./db/dev.db", "", "");
        Base.openTransaction();
    }

    @AfterEach
    void tearDown() {
        Base.rollbackTransaction();
        Base.close();
    }

    @Test
    void testAlumnoCanEnrollInCarrera() {
        // 1. Crear Persona y Alumno
        Persona p = new Persona();
        p.set("dni", 12345678);
        p.set("nombre", "Carlos");
        p.set("apellido", "Lopez");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 11111111);
        alumno.insert();

        // 2. Crear Carrera
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Ingenieria en Sistemas");
        carrera.set("cant_anios", 4);
        carrera.insert();

        // 3. Inscribir alumno en carrera
        alumno.enrollInCarrera(((Number) carrera.getId()).intValue());

        // 4. Verificar inscripción
        long countInscripciones = Base.count("alumno_carrera",
                "id_alumno = ? AND id_carrera = ?", ((Number) alumno.getId()).intValue(),
                ((Number) carrera.getId()).intValue());
        assertEquals(1, countInscripciones, "Alumno debería estar inscripto en la carrera");
    }

    @Test
    void testAlumnoCannot​BeEnrolledTwiceInSameCarrera() {
        // 1. Crear Persona y Alumno
        Persona p = new Persona();
        p.set("dni", 23456789);
        p.set("nombre", "Maria");
        p.set("apellido", "Garcia");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 22222222);
        alumno.insert();

        // 2. Crear Carrera
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Administracion");
        carrera.set("cant_anios", 3);
        carrera.insert();

        // 3. Inscribir alumno en carrera dos veces
        alumno.enrollInCarrera(((Number) carrera.getId()).intValue());
        alumno.enrollInCarrera(((Number) carrera.getId()).intValue());

        // 4. Debería haber solo una inscripción (si hay validación en BD)
        long countInscripciones = Base.count("alumno_carrera",
                "id_alumno = ? AND id_carrera = ?", ((Number) alumno.getId()).intValue(),
                ((Number) carrera.getId()).intValue());
        assertEquals(1, countInscripciones, "Debería haber solo una inscripción");
    }

    @Test
    void testAlumnoCanEnrollInMateria() {
        // 1. Crear Alumno
        Persona p = new Persona();
        p.set("dni", 34567890);
        p.set("nombre", "Pedro");
        p.set("apellido", "Martinez");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 33333333);
        alumno.insert();

        // 2. Crear Materia
        Materia materia = new Materia();
        materia.set("codigo", "MAT101");
        materia.set("nombre_materia", "Matemática I");
        materia.set("anio_pertenece", 1);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        // 3. Inscribir alumno en materia
        alumno.enrollInMateria(((Number) materia.getId()).intValue());

        // 4. Verificar inscripción
        Inscripcion inscripcion = Inscripcion.findFirst("id_alumno = ? AND id_materia = ?",
                ((Number) alumno.getId()).intValue(), ((Number) materia.getId()).intValue());
        assertNotNull(inscripcion, "La inscripción debería existir");
    }

    @Test
    void testCannotEnrollInNonexistentMateria() {
        // 1. Crear Alumno
        Persona p = new Persona();
        p.set("dni", 45678901);
        p.set("nombre", "Ana");
        p.set("apellido", "Sanchez");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 44444444);
        alumno.insert();

        // 2. Intentar inscribir en materia inexistente
        alumno.enrollInMateria(9999); // ID inexistente

        // 3. No debería haber inscripción
        Inscripcion inscripcion = Inscripcion.findFirst("id_alumno = ? AND id_materia = ?",
                ((Number) alumno.getId()).intValue(), 9999);
        assertNull(inscripcion, "No debería haber inscripción a materia inexistente");
    }

    @Test
    void testGetAlumnoInscripciones() {
        // 1. Crear Alumno
        Persona p = new Persona();
        p.set("dni", 56789012);
        p.set("nombre", "David");
        p.set("apellido", "Torres");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 55555555);
        alumno.insert();

        // 2. Crear y inscribir en múltiples materias
        Materia m1 = new Materia();
        m1.set("codigo", "FIS1");
        m1.set("nombre_materia", "Física I");
        m1.set("anio_pertenece", 1);
        m1.set("cant_horas", 4);
        m1.set("periodo", "CUATRIMESTRAL");
        m1.insert();

        Materia m2 = new Materia();
        m2.set("codigo", "QUI1");
        m2.set("nombre_materia", "Química I");
        m2.set("anio_pertenece", 1);
        m2.set("cant_horas", 3);
        m2.set("periodo", "CUATRIMESTRAL");
        m2.insert();

        alumno.enrollInMateria(((Number) m1.getId()).intValue());
        alumno.enrollInMateria(((Number) m2.getId()).intValue());

        // 3. Obtener inscripciones
        java.util.List<Inscripcion> inscripciones = alumno.getInscripciones();
        assertEquals(2, inscripciones.size(), "El alumno debería tener 2 inscripciones");
    }
}
