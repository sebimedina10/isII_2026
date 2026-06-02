package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;

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
}