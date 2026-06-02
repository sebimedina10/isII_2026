package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("persona")
public class Persona extends Model {
    public String getDni() {
        return getString("dni");
    }

    public void setDni(String dni) {
        set("dni", dni);
    }

    public String getNombre() {
        return getString("nombre");
    }

    public void setNombre(String nombre) {
        set("nombre", nombre);
    }

    public String getApellido() {
        return getString("apellido");
    }

    public void setApellido(String apellido) {
        set("apellido", apellido);
    }

    public String getTelefono() {
        return getString("telefono");
    }

    public void setTelefono(String telefono) {
        set("telefono", telefono);
    }

    public String getEmail() {
        return getString("email");
    }

    public void setEmail(String email) {
        set("email", email);
    }
}