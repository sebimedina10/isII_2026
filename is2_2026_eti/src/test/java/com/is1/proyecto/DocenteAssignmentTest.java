package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;

public class DocenteAssignmentTest {

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
    void testAssignMateriaToDocente() {
        // 1. Crear Docente
        Persona p = new Persona();
        p.set("dni", 11223344);
        p.set("nombre", "Roberto");
        p.set("apellido", "Vilchez");
        p.insert();

        Docente docente = new Docente();
        docente.set("dni", 11223344);
        docente.set("titulo", "Ingeniero");
        docente.set("rol", "RESPONSABLE");
        docente.insert();

        // 2. Crear Materia
        Materia materia = new Materia();
        materia.set("codigo", "POO1");
        materia.set("nombre_materia", "Programación Orientada a Objetos");
        materia.set("anio_pertenece", 2);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        // 3. Asignar Docente a Materia
        Doc_Materia asignacion = new Doc_Materia();
        asignacion.set("id_docente", ((Number) docente.getId()).intValue());
        asignacion.set("id_materia", ((Number) materia.getId()).intValue());
        asignacion.insert();

        // 4. Verificar
        Doc_Materia found = Doc_Materia.findFirst("id_docente = ? AND id_materia = ?",
                ((Number) docente.getId()).intValue(), ((Number) materia.getId()).intValue());
        assertNotNull(found, "La asignación debería existir");
    }

    @Test
    void testPreventDuplicateAssignment() {
        // 1. Crear Docente y Materia
        Persona p = new Persona();
        p.set("dni", 22334455);
        p.set("nombre", "Carmen");
        p.set("apellido", "Diaz");
        p.insert();

        Docente docente = new Docente();
        docente.set("dni", 22334455);
        docente.set("titulo", "Doctora");
        docente.set("rol", "RESPONSABLE");
        docente.insert();

        Materia materia = new Materia();
        materia.set("codigo", "BD1");
        materia.set("nombre_materia", "Bases de Datos");
        materia.set("anio_pertenece", 2);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        // 2. Verificar que no existe asignación
        assertFalse(
                Doc_Materia.assignmentExists(((Number) docente.getId()).intValue(),
                        ((Number) materia.getId()).intValue()),
                "La asignación no debería existir aún");

        // 3. Crear asignación
        Doc_Materia asignacion = new Doc_Materia();
        asignacion.set("id_docente", ((Number) docente.getId()).intValue());
        asignacion.set("id_materia", ((Number) materia.getId()).intValue());
        asignacion.insert();

        // 4. Verificar que ahora existe
        assertTrue(
                Doc_Materia.assignmentExists(((Number) docente.getId()).intValue(),
                        ((Number) materia.getId()).intValue()),
                "La asignación debería existir");
    }

    @Test
    void testGetMateriasByDocente() {
        // 1. Crear Docente
        Persona p = new Persona();
        p.set("dni", 33445566);
        p.set("nombre", "Fernando");
        p.set("apellido", "Soto");
        p.insert();

        Docente docente = new Docente();
        docente.set("dni", 33445566);
        docente.set("titulo", "Licenciado");
        docente.set("rol", "JTP");
        docente.insert();

        // 2. Crear múltiples Materias
        Materia m1 = new Materia();
        m1.set("codigo", "WEB1");
        m1.set("nombre_materia", "Desarrollo Web");
        m1.set("anio_pertenece", 3);
        m1.set("cant_horas", 3);
        m1.set("periodo", "CUATRIMESTRAL");
        m1.insert();

        Materia m2 = new Materia();
        m2.set("codigo", "MOV1");
        m2.set("nombre_materia", "Desarrollo Móvil");
        m2.set("anio_pertenece", 3);
        m2.set("cant_horas", 3);
        m2.set("periodo", "CUATRIMESTRAL");
        m2.insert();

        // 3. Asignar ambas materias
        Doc_Materia a1 = new Doc_Materia();
        a1.set("id_docente", ((Number) docente.getId()).intValue());
        a1.set("id_materia", ((Number) m1.getId()).intValue());
        a1.insert();

        Doc_Materia a2 = new Doc_Materia();
        a2.set("id_docente", ((Number) docente.getId()).intValue());
        a2.set("id_materia", ((Number) m2.getId()).intValue());
        a2.insert();

        // 4. Verificar
        java.util.List<Doc_Materia> asignaciones = Doc_Materia.findByDocente(((Number) docente.getId()).intValue());
        assertEquals(2, asignaciones.size(), "El docente debería tener 2 asignaciones");
    }

    @Test
    void testRemoveAssignment() {
        // 1. Crear Docente y Materia
        Persona p = new Persona();
        p.set("dni", 44556677);
        p.set("nombre", "Gabriela");
        p.set("apellido", "Lopez");
        p.insert();

        Docente docente = new Docente();
        docente.set("dni", 44556677);
        docente.set("titulo", "Ingeniera");
        docente.set("rol", "AYUDANTE");
        docente.insert();

        Materia materia = new Materia();
        materia.set("codigo", "RED1");
        materia.set("nombre_materia", "Redes");
        materia.set("anio_pertenece", 3);
        materia.set("cant_horas", 4);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        // 2. Crear asignación
        Doc_Materia asignacion = new Doc_Materia();
        asignacion.set("id_docente", ((Number) docente.getId()).intValue());
        asignacion.set("id_materia", ((Number) materia.getId()).intValue());
        asignacion.insert();

        // 3. Eliminar asignación
        Doc_Materia.deleteById(((Number) asignacion.getId()).intValue());

        // 4. Verificar que no existe
        Doc_Materia found = Doc_Materia.findById(((Number) asignacion.getId()).intValue());
        assertNull(found, "La asignación debería haber sido eliminada");
    }

    @Test
    void testOneDocenteMultipleMaterias() {
        // Verificar que un docente puede tener múltiples materias
        Persona p = new Persona();
        p.set("dni", 55667788);
        p.set("nombre", "Henry");
        p.set("apellido", "Martinez");
        p.insert();

        Docente docente = new Docente();
        docente.set("dni", 55667788);
        docente.set("titulo", "Magister");
        docente.set("rol", "RESPONSABLE");
        docente.insert();

        // Crear 3 materias
        for (int i = 1; i <= 3; i++) {
            Materia materia = new Materia();
            materia.set("codigo", "MAT" + i);
            materia.set("nombre_materia", "Materia " + i);
            materia.set("anio_pertenece", 1);
            materia.set("cant_horas", 4);
            materia.set("periodo", "CUATRIMESTRAL");
            materia.insert();

            Doc_Materia asignacion = new Doc_Materia();
            asignacion.set("id_docente", ((Number) docente.getId()).intValue());
            asignacion.set("id_materia", ((Number) materia.getId()).intValue());
            asignacion.insert();
        }

        long count = Base.count("docente_materia", "id_docente = ?", ((Number) docente.getId()).intValue());
        assertEquals(3, count, "El docente debería tener 3 materias");
    }
}
