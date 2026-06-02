package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("docentes")
public class Docente extends Model {
    public String getDniPersona() {
        return getString("dni");
    }

    public void setDniPersona(String dniPersona) {
        set("dni", dniPersona);
    }
}

