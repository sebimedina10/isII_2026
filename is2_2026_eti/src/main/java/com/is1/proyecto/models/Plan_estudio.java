package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;

@Table("plan_estudio")
@IdName("id_planEstudio")
public class Plan_estudio extends Model {    //Getters y setters de tables son creados automáticamente .
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
}