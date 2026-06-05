package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;

public class modelTest {

    @BeforeEach
    void setUp() {
        // Abrir conexión a la base de datos de desarrollo
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:./db/dev.db", "", "");
        // Iniciar una transacción para revertir cualquier cambio al final de cada test
        Base.openTransaction();
    }

    @AfterEach
    void tearDown() {
        // Deshacer todos los cambios realizados en el test para dejar la base de datos limpia
        Base.rollbackTransaction();
        Base.close();
    }

    @Test
    void testModelCrud() {
        // 1. Probar Modelo Persona
        Persona p = new Persona();
        p.set("dni", 12345678);
        p.set("nombre", "Leandro");
        p.set("apellido", "Olivero");
        p.set("email", "leandro@example.com");
        p.insert();

        Persona foundPersona = Persona.findFirst("dni = ?", 12345678);
        assertNotNull(foundPersona, "La persona debería existir en la BD");
        assertEquals("Leandro", foundPersona.get("nombre"));
        assertEquals("Olivero", foundPersona.get("apellido"));

        // 2. Probar Modelo Carrera
        Carrera c = new Carrera();
        c.set("nombre_carrera", "Ingeniería en Sistemas");
        c.set("cant_anios", 5);
        c.insert();

        Carrera foundCarrera = Carrera.findFirst("nombre_carrera = ?", "Ingeniería en Sistemas");
        assertNotNull(foundCarrera, "La carrera debería existir en la BD");
        assertEquals(5, foundCarrera.getInteger("cant_anios"));

        // 3. Probar Modelo Materia
        Materia m = new Materia();
        m.set("nombre_materia", "Matemática I");
        m.set("codigo", "MAT101");
        m.set("anio_pertenece", 1);
        m.set("cant_horas", 6);
        m.set("periodo", "CUATRIMESTRAL");
        m.insert();

        Materia foundMateria = Materia.findFirst("codigo = ?", "MAT101");
        assertNotNull(foundMateria);
        assertEquals("Matemática I", foundMateria.get("nombre_materia"));
    }

    @Test
    void testAlumnoCreation() {
        // 1. Crear Persona base para el alumno
        Persona p = new Persona();
        p.set("dni", 87654321);
        p.set("nombre", "Juan");
        p.set("apellido", "Perez");
        p.set("email", "juan@example.com");
        p.insert();

        // 2. Crear Alumno asociado
        Alumno alu = new Alumno();
        alu.set("dni", 87654321);
        alu.set("progreso", 0.0);
        alu.set("tipo_alumno", "INGRESANTE");
        alu.insert();

        // 3. Validar recuperación de datos
        Alumno foundAlu = Alumno.findFirst("dni = ?", 87654321);
        assertNotNull(foundAlu, "El alumno debería estar en la base de datos");
        assertEquals("INGRESANTE", foundAlu.get("tipo_alumno"));
    }

    @Test
    void testDocenteCreation() {
        // 1. Crear Persona base para el docente
        Persona p = new Persona();
        p.set("dni", 22223333);
        p.set("nombre", "Marcelo");
        p.set("apellido", "Gomez");
        p.set("email", "marcelo@example.com");
        p.insert();

        // 2. Crear Docente asociado
        Docente doc = new Docente();
        doc.set("dni", 22223333);
        doc.set("titulo", "Ingeniero");
        doc.set("rol", "RESPONSABLE");
        doc.insert();

        // 3. Validar recuperación de datos
        Docente foundDoc = Docente.findFirst("dni = ?", 22223333);
        assertNotNull(foundDoc, "El docente debería estar en la base de datos");
        assertEquals("Ingeniero", foundDoc.get("titulo"));
    }
}