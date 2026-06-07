package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;
import java.util.List;
import java.util.Map;

@Table("inscripcion") // name of table
@IdName("id_inscripcion")
public class Inscripcion extends Model {
    // Getters y setters de tables son creados automáticamente .

    // Helper methods para encapsular consultas SQL
    public Alumno getAlumno() {
        return Alumno.findById(get("id_alumno"));
    }

    public Materia getMateria() {
        return Materia.findById(get("id_materia"));
    }

    public List<Map> getNotas() {
        return Base.findAll("SELECT id_notas, valor, tipo_nota FROM notas WHERE id_inscripcion = ? ORDER BY tipo_nota",
                getId());
    }

    public void deleteInscription() {
        Base.exec("DELETE FROM inscripcion WHERE id_inscripcion = ?", getId());
    }

    public static boolean isAlreadyEnrolled(int idAlumno, int idMateria) {
        return findFirst("id_alumno = ? AND id_materia = ?", idAlumno, idMateria) != null;
    }
}