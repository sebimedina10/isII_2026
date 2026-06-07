package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;

@Table("facultad")
@IdName("id_facultad")
public class Facultad extends Model {
    public int getIdFacultad() {
        return getInteger("id_facultad");
    }

    public void setIdFacultad(int idFacultad) {
        set("id_facultad", idFacultad);
    }

    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    // Helper methods
    public java.util.List<java.util.Map> getCarreras() {
        return Base.findAll("SELECT c.* FROM carrera c WHERE c.id_facultad = ?", getId());
    }

    public static java.util.List<Facultad> getAll() {
        return findAll();
    }
}
