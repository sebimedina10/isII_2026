package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;

@Table("plan_estudio")
@IdName("id_planEstudio")
public class Plan_estudio extends Model { // Getters y setters de tables son creados automáticamente .
    public int getIdPlan() {
        return getInteger("id_planEstudio");
    }

    public void setIdPlan(int idPlan) {
        set("id_planEstudio", idPlan);
    }

    public int getIdCarrera() {
        return getInteger("id_carrera");
    }

    public void setIdCarrera(int idCarrera) {
        set("id_carrera", idCarrera);
    }

    // Helper methods
    public static void addMateriasToCarrera(int idCarrera, List<Integer> materiaIds) {
        for (Integer idMateria : materiaIds) {
            Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)", idCarrera, idMateria);
        }
    }

    public static java.util.List<java.util.Map> getMateriasByCarrera(int idCarrera) {
        return Base.findAll(
                "SELECT m.* FROM materia m JOIN plan_estudio pe ON m.id_materia = pe.id_materia WHERE pe.id_carrera = ?",
                idCarrera);
    }

    public static long countMateriasByCarrera(int idCarrera) {
        return Base.count("plan_estudio", "id_carrera = ?", idCarrera);
    }
}