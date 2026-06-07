package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;

@Table("carrera")
@IdName("id_carrera")
public class Carrera extends Model {
    public int getIdCarrera() {
        return getInteger("id_carrera");
    }

    public void setIdCarrera(int idCarrera) {
        set("id_carrera", idCarrera);
    }

    public String getNombre() {
        return getString("nombre_carrera");
    }

    public void setNombre(String nombre) {
        set("nombre_carrera", nombre);
    }

    public int getDuracionAnios() {
        return getInteger("cant_anios");
    }

    public void setDuracionAnios(int duracionAnios) {
        set("cant_anios", duracionAnios);
    }

    // Helper methods para encapsular consultas SQL
    public static boolean exists(String nombre) {
        return findFirst("nombre_carrera = ?", nombre) != null;
    }

    public java.util.List<java.util.Map> getMaterias() {
        return Base.findAll(
                "SELECT m.* FROM materia m JOIN plan_estudio pe ON m.id_materia = pe.id_materia WHERE pe.id_carrera = ?",
                getId());
    }
}