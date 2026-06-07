package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.annotations.Table;
import java.util.List;

@Table("alumnos") // name of table
public class Alumno extends Model {
    public String getDniPersona() {
        return getString("dni");
    }

    public void setDniPersona(String dniPersona) {
        set("dni", dniPersona);
    }

    // Helper methods para encapsular consultas SQL
    public void enrollInCarrera(int idCarrera) {
        String fechaActual = java.time.LocalDate.now().toString();
        Base.exec("INSERT INTO alumno_carrera (id_alumno, id_carrera, fecha_inscripcion) VALUES (?, ?, ?)", getId(),
                idCarrera, fechaActual);
    }

    public void enrollInMateria(int idMateria) {
        java.util.List<java.util.Map> exists = Base
                .findAll("SELECT * FROM inscripcion WHERE id_alumno = ? AND id_materia = ?", getId(), idMateria);
        if (exists.isEmpty()) {
            Base.exec("INSERT INTO inscripcion (id_alumno, id_materia) VALUES (?, ?)", getId(), idMateria);
        }
    }

    public List<Inscripcion> getInscripciones() {
        return findAll();
    }

    public double calculateProgress(int idCarrera) {
        long totalMaterias = Base.count("plan_estudio", "id_carrera = ?", idCarrera);
        if (totalMaterias == 0)
            return 0.0;

        long materiasAprobadas = ((Number) Base.firstCell(
                "SELECT COUNT(DISTINCT i.id_materia) FROM inscripcion i JOIN plan_estudio pe ON i.id_materia = pe.id_materia WHERE i.id_alumno = ? AND i.estado = 'APROBADA' AND pe.id_carrera = ?",
                getId(), idCarrera)).longValue();

        return ((double) materiasAprobadas / totalMaterias) * 100.0;
    }
}