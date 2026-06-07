package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;

public class GradeManagementTest {

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
    void testValidScoreRange() {
        assertTrue(Nota.isValidScore(0), "0 es una nota válida");
        assertTrue(Nota.isValidScore(5), "5 es una nota válida");
        assertTrue(Nota.isValidScore(10), "10 es una nota válida");
        assertFalse(Nota.isValidScore(-1), "-1 no es una nota válida");
        assertFalse(Nota.isValidScore(11), "11 no es una nota válida");
    }

    @Test
    void testAddGradeToInscription() {
        // 1. Crear Alumno, Materia e Inscripción
        Persona p = new Persona();
        p.set("dni", 66666666);
        p.set("nombre", "Laura");
        p.set("apellido", "Flores");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 66666666);
        alumno.insert();

        Materia materia = new Materia();
        materia.set("codigo", "ALG1");
        materia.set("nombre_materia", "Álgebra I");
        materia.set("anio_pertenece", 1);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.set("id_alumno", alumno.getId());
        inscripcion.set("id_materia", materia.getId());
        inscripcion.insert();

        // 2. Agregar nota
        Nota nota = new Nota();
        nota.set("id_inscripcion", ((Number) inscripcion.getId()).intValue());
        nota.set("valor", 8);
        nota.set("tipo_nota", "FINAL");
        nota.insert();

        // 3. Verificar
        Nota foundNota = Nota.findFirst("id_inscripcion = ? AND tipo_nota = ?",
                ((Number) inscripcion.getId()).intValue(), "FINAL");
        assertNotNull(foundNota, "La nota debería existir");
        assertEquals(8, foundNota.getValor(), "La nota debería ser 8");
    }

    @Test
    void testUpdateExistingGrade() {
        // 1. Crear inscripción y nota inicial
        Persona p = new Persona();
        p.set("dni", 77777777);
        p.set("nombre", "Sofia");
        p.set("apellido", "Mendez");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 77777777);
        alumno.insert();

        Materia materia = new Materia();
        materia.set("codigo", "GEO1");
        materia.set("nombre_materia", "Geometría I");
        materia.set("anio_pertenece", 1);
        materia.set("cant_horas", 3);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.set("id_alumno", ((Number) alumno.getId()).intValue());
        inscripcion.set("id_materia", ((Number) materia.getId()).intValue());
        inscripcion.insert();

        // 2. Agregar nota inicial
        Nota nota = new Nota();
        nota.set("id_inscripcion", ((Number) inscripcion.getId()).intValue());
        nota.set("valor", 6);
        nota.set("tipo_nota", "PARCIAL");
        nota.insert();

        // 3. Actualizar nota
        nota.updateOrInsert(((Number) inscripcion.getId()).intValue(), 9, "PARCIAL");

        // 4. Verificar actualización
        Nota updated = Nota.findFirst("id_inscripcion = ? AND tipo_nota = ?",
                ((Number) inscripcion.getId()).intValue(), "PARCIAL");
        assertEquals(9, updated.getValor(), "La nota debería ser 9");
    }

    @Test
    void testMultipleGradesPerInscription() {
        // 1. Crear inscripción
        Persona p = new Persona();
        p.set("dni", 88888888);
        p.set("nombre", "Marco");
        p.set("apellido", "Ruiz");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 88888888);
        alumno.insert();

        Materia materia = new Materia();
        materia.set("codigo", "EST1");
        materia.set("nombre_materia", "Estadística I");
        materia.set("anio_pertenece", 2);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.set("id_alumno", ((Number) alumno.getId()).intValue());
        inscripcion.set("id_materia", ((Number) materia.getId()).intValue());
        inscripcion.insert();

        // 2. Agregar múltiples notas (parcial, final)
        Nota notaParcial = new Nota();
        notaParcial.set("id_inscripcion", ((Number) inscripcion.getId()).intValue());
        notaParcial.set("valor", 7);
        notaParcial.set("tipo_nota", "PARCIAL");
        notaParcial.insert();

        Nota notaFinal = new Nota();
        notaFinal.set("id_inscripcion", ((Number) inscripcion.getId()).intValue());
        notaFinal.set("valor", 8);
        notaFinal.set("tipo_nota", "FINAL");
        notaFinal.insert();

        // 3. Verificar ambas notas
        java.util.List<Nota> notas = Nota.findByInscripcion(((Number) inscripcion.getId()).intValue());
        assertEquals(2, notas.size(), "Debería haber 2 notas");
    }

    @Test
    void testRejectInvalidGradeValue() {
        Persona p = new Persona();
        p.set("dni", 99999999);
        p.set("nombre", "Patricia");
        p.set("apellido", "Gomez");
        p.insert();

        Alumno alumno = new Alumno();
        alumno.set("dni", 99999999);
        alumno.insert();

        Materia materia = new Materia();
        materia.set("codigo", "LOG1");
        materia.set("nombre_materia", "Lógica I");
        materia.set("anio_pertenece", 1);
        materia.set("cant_horas", 3);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.set("id_alumno", ((Number) alumno.getId()).intValue());
        inscripcion.set("id_materia", ((Number) materia.getId()).intValue());
        inscripcion.insert();

        // Intentar agregar nota con valor inválido (> 10)
        assertFalse(Nota.isValidScore(15), "Nota 15 no es válida");
        assertFalse(Nota.isValidScore(-5), "Nota -5 no es válida");
    }
}
