package com.is1.proyecto.models;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;
import org.javalite.activejdbc.annotations.IdName;

@Table("inscripcion") //name of table
@IdName("id_inscripcion")
public class Inscripcion extends Model {
    //Getters y setters de tables son creados automáticamente .
}