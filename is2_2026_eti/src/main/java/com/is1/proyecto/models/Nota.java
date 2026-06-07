package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;

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

    // Helper methods
    public Inscripcion getInscripcion() {
        return Inscripcion.findById(get("id_inscripcion"));
    }

    public static List<Nota> findByInscripcion(int idInscripcion) {
        return where("id_inscripcion = ?", idInscripcion).orderBy("tipo_nota");
    }

    public static boolean isValidScore(int valor) {
        return valor >= 0 && valor <= 10;
    }

    public void updateOrInsert(int idInscripcion, int valor, String tipoNota) {
        Nota existing = findFirst("id_inscripcion = ? AND tipo_nota = ?", idInscripcion, tipoNota);
        if (existing != null) {
            existing.setValor(valor);
            existing.saveIt();
        } else {
            Nota nota = new Nota();
            nota.set("id_inscripcion", idInscripcion);
            nota.set("valor", valor);
            nota.set("tipo_nota", tipoNota);
            nota.saveIt();
        }
    }
}