package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;

@Table("notas")
@IdName("id_notas")
public class Nota extends Model {
    public int getIdNota() {
        return getInteger("id_notas");
    }

    public void setIdNota(int idNota) {
        set("id_notas", idNota);
    }

    public int getValor() {
        return getInteger("valor");
    }

    public void setValor(int valor) {
        set("valor", valor);
    }
}