package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;

@Table("docente_materia") // name of table
@IdName("id_DocMat")
public class Doc_Materia extends Model {
    // Getters y setters de tables son creados automáticamente .

    // Helper methods para encapsular consultas SQL
    public Docente getDocente() {
        return Docente.findById(get("id_docente"));
    }

    public Materia getMateria() {
        return Materia.findById(get("id_materia"));
    }

    public static List<Doc_Materia> findByDocente(int idDocente) {
        return where("id_docente = ?", idDocente);
    }

    public static void deleteByDocente(int idDocente) {
        delete("id_docente = ?", idDocente);
    }

    public static void deleteById(int id) {
        delete("id_DocMat = ?", id);
    }

    public static boolean assignmentExists(int idDocente, int idMateria) {
        return findFirst("id_docente = ? AND id_materia = ?", idDocente, idMateria) != null;
    }
}