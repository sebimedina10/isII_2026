package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;

@Table("materia")
@IdName("id_materia")
public class Materia extends Model {
    public int getIdMateria() {
        return getInteger("id_materia");
    }

    public void setIdMateria(int idMateria) {
        set("id_materia", idMateria);
    }

    public String getCodigo() {
        return getString("codigo");
    }

    public void setCodigo(String codigo) {
        set("codigo", codigo);
    }

    public String getNombre() {
        return getString("nombre_materia");
    }

    public void setNombre(String nombre) {
        set("nombre_materia", nombre);
    }

    public String getPeriodo() {
        return getString("periodo");
    }

    public void setPeriodo(String periodo) {
        set("periodo", periodo);
    }
}