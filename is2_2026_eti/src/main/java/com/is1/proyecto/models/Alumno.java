package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("alumnos") //name of table 
public class Alumno extends Model {
    public String getDniPersona() {
        return getString("dni");
    }

    public void setDniPersona(String dniPersona) {
        set("dni", dniPersona);
    }
}