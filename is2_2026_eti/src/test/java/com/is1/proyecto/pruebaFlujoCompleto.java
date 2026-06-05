package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;
import java.util.List;
import java.util.Map;

public class pruebaFlujoCompleto {

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
    void testCompleteAcademicWorkflow() {
        // 1. CONFIGURACIÓN DEL PLAN DE ESTUDIOS
        // Crear una nueva Carrera
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Ingeniería Aeroespacial");
        carrera.set("cant_anios", 5);
        carrera.insert();
        int idCarrera = (int) carrera.getId();

        // Crear materias: Física I y Física II
        Materia fisicaI = new Materia();
        fisicaI.set("nombre_materia", "Física I");
        fisicaI.set("codigo", "FIS1");
        fisicaI.set("anio_pertenece", 1);
        fisicaI.set("cant_horas", 4);
        fisicaI.set("periodo", "CUATRIMESTRAL");
        fisicaI.insert();
        int idFisicaI = (int) fisicaI.getId();

        Materia fisicaII = new Materia();
        fisicaII.set("nombre_materia", "Física II");
        fisicaII.set("codigo", "FIS2");
        fisicaII.set("anio_pertenece", 1);
        fisicaII.set("cant_horas", 4);
        fisicaII.set("periodo", "CUATRIMESTRAL");
        fisicaII.insert();
        int idFisicaII = (int) fisicaII.getId();

        // Asociar materias al plan de estudio de la carrera
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)", idCarrera, idFisicaI);
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)", idCarrera, idFisicaII);

        // 2. CONFIGURACIÓN DEL ESTUDIANTE
        // Crear Persona
        Persona persAlumno = new Persona();
        persAlumno.set("dni", 99000111);
        persAlumno.set("nombre", "Carlos");
        persAlumno.set("apellido", "Astron");
        persAlumno.set("email", "carlos.a@aero.com");
        persAlumno.insert();

        // Crear Alumno
        Alumno alumno = new Alumno();
        alumno.set("dni", 99000111);
        alumno.set("progreso", 0.0);
        alumno.set("tipo_alumno", "INGRESANTE");
        alumno.insert();
        int idAlumno = (int) alumno.getId();

        // Inscribir alumno a la carrera
        Base.exec("INSERT INTO alumno_carrera (id_alumno, id_carrera) VALUES (?, ?)", idAlumno, idCarrera);

        // 3. SIMULACIÓN DE INSCRIPCIÓN A MATERIA
        Inscripcion inscFisI = new Inscripcion();
        inscFisI.set("id_alumno", idAlumno);
        inscFisI.set("id_materia", idFisicaI);
        inscFisI.set("estado", "CURSANDO");
        inscFisI.insert();
        int idInscFisI = (int) inscFisI.getId();

        // Verificar que está inscripto correctamente
        Inscripcion checkInscFisI = Inscripcion.findFirst("id_inscripcion = ?", idInscFisI);
        assertNotNull(checkInscFisI);
        assertEquals("CURSANDO", checkInscFisI.get("estado"));

        // 4. SIMULACIÓN DE EXÁMENES Y CARGA DE NOTAS
        // Registrar nota de examen final (Aprobado con 9)
        Nota notaFinal = new Nota();
        notaFinal.set("id_inscripcion", idInscFisI);
        notaFinal.set("valor", 9);
        notaFinal.set("tipo_nota", "FINAL");
        notaFinal.insert();

        // Promover el estado de la inscripción a APROBADA
        inscFisI.set("estado", "APROBADA");
        inscFisI.saveIt();

        // 5. COMPROBAR PROMEDIO ACADÉMICO
        List<Map> notasFinales = Base.findAll(
            "SELECT valor FROM notas WHERE id_inscripcion = ? AND tipo_nota = 'FINAL'", idInscFisI);

        assertEquals(1, notasFinales.size());
        double sum = 0;
        for (Map n : notasFinales) {
            sum += ((Number) n.get("valor")).doubleValue();
        }
        double promedio = sum / notasFinales.size();
        assertEquals(9.0, promedio, 0.01, "El promedio de notas finales debería ser 9.0");
    }

    @Test
    void testTeacherAssignment() {
        // Crear una materia
        Materia mat = new Materia();
        mat.set("nombre_materia", "Materia de Prueba");
        mat.set("codigo", "TEST101");
        mat.set("anio_pertenece", 1);
        mat.set("cant_horas", 4);
        mat.set("periodo", "CUATRIMESTRAL");
        mat.insert();
        int idMateria = (int) mat.getId();

        // Crear un Docente
        Persona persProf = new Persona();
        persProf.set("dni", 99333222);
        persProf.set("nombre", "Dr. Richard");
        persProf.set("apellido", "Feynman");
        persProf.set("email", "feynman@caltech.edu");
        persProf.insert();

        Docente doc = new Docente();
        doc.set("dni", 99333222);
        doc.set("titulo", "Físico");
        doc.set("rol", "RESPONSABLE");
        doc.insert();
        int idDocente = (int) doc.getId();

        // Asignar el docente a la materia
        Base.exec("INSERT INTO docente_materia (id_docente, id_materia) VALUES (?, ?)", idDocente, idMateria);

        // Verificar la asignación
        List<Map> asignaciones = Base.findAll(
            "SELECT * FROM docente_materia WHERE id_docente = ? AND id_materia = ?", idDocente, idMateria);
        
        assertEquals(1, asignaciones.size());
    }
}