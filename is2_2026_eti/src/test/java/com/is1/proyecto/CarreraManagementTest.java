package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;
import java.util.Map;

public class CarreraManagementTest {

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
    void testCreateCarrera() {
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Ingenieria Civil");
        carrera.set("cant_anios", 5);
        carrera.insert();

        assertNotNull(carrera.getId(), "La carrera debería tener ID después de insertarse");
        assertEquals("Ingenieria Civil", carrera.getNombre());
        assertEquals(5, carrera.getDuracionAnios());
    }

    @Test
    void testAddMateriasToCarrera() {
        // 1. Crear carrera
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Ingenieria Informatica");
        carrera.set("cant_anios", 4);
        carrera.insert();

        // 2. Crear materias
        Materia m1 = new Materia();
        m1.set("codigo", "PRG1");
        m1.set("nombre_materia", "Programación I");
        m1.set("anio_pertenece", 1);
        m1.set("cant_horas", 4);
        m1.set("periodo", "CUATRIMESTRAL");
        m1.insert();

        Materia m2 = new Materia();
        m2.set("codigo", "BDD1");
        m2.set("nombre_materia", "Bases de Datos I");
        m2.set("anio_pertenece", 2);
        m2.set("cant_horas", 4);
        m2.set("periodo", "CUATRIMESTRAL");
        m2.insert();

        // 3. Asociar materias a la carrera
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                ((Number) carrera.getId()).intValue(), ((Number) m1.getId()).intValue());
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                ((Number) carrera.getId()).intValue(), ((Number) m2.getId()).intValue());

        // 4. Verificar
        long countMaterias = Base.count("plan_estudio", "id_carrera = ?", ((Number) carrera.getId()).intValue());
        assertEquals(2, countMaterias, "La carrera debería tener 2 materias");
    }

    @Test
    void testGetMateriasByCarrera() {
        // 1. Crear carrera
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Licenciatura en Sistemas");
        carrera.set("cant_anios", 3);
        carrera.insert();

        // 2. Crear materias
        Materia m1 = new Materia();
        m1.set("codigo", "ARQ1");
        m1.set("nombre_materia", "Arquitectura de Computadoras");
        m1.set("anio_pertenece", 1);
        m1.set("cant_horas", 4);
        m1.set("periodo", "CUATRIMESTRAL");
        m1.insert();

        // 3. Asociar
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                ((Number) carrera.getId()).intValue(), ((Number) m1.getId()).intValue());

        // 4. Usar helper method
        java.util.List<Map> materias = carrera.getMaterias();
        assertEquals(1, materias.size(), "Debería retornar 1 materia");
    }

    @Test
    void testCarreraWithMultipleYears() {
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Doctorado en Física");
        carrera.set("cant_anios", 3);
        carrera.insert();

        assertEquals(3, carrera.getDuracionAnios(), "La carrera debería durar 3 años");
    }

    @Test
    void testMateriaInMultipleCarreras() {
        // Una materia puede estar en múltiples carreras
        Materia materia = new Materia();
        materia.set("codigo", "MAT001");
        materia.set("nombre_materia", "Cálculo I");
        materia.set("anio_pertenece", 1);
        materia.set("cant_horas", 5);
        materia.set("periodo", "CUATRIMESTRAL");
        materia.insert();

        Carrera carrera1 = new Carrera();
        carrera1.set("nombre_carrera", "Ingenieria Mecanica");
        carrera1.set("cant_anios", 5);
        carrera1.insert();

        Carrera carrera2 = new Carrera();
        carrera2.set("nombre_carrera", "Ingenieria Quimica");
        carrera2.set("cant_anios", 5);
        carrera2.insert();

        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                ((Number) carrera1.getId()).intValue(), ((Number) materia.getId()).intValue());
        Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                ((Number) carrera2.getId()).intValue(), ((Number) materia.getId()).intValue());

        long count = Base.count("plan_estudio", "id_materia = ?", ((Number) materia.getId()).intValue());
        assertEquals(2, count, "La materia debería estar en 2 carreras");
    }

    @Test
    void testCarreraConsistencyCheck() {
        Carrera carrera = new Carrera();
        carrera.set("nombre_carrera", "Tecnicatura Electrónica");
        carrera.set("cant_anios", 2);
        carrera.insert();

        Carrera found = Carrera.findById(((Number) carrera.getId()).intValue());
        assertNotNull(found, "La carrera debería encontrarse por ID");
        assertEquals("Tecnicatura Electrónica", found.getNombre());
        assertEquals(2, found.getDuracionAnios());
    }
}
