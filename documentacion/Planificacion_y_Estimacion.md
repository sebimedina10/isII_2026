# Planificación y Estimación del Proyecto
# ACTIVIDAD 5: Estimación de Tareas del Backlog

## Objetivo

El objetivo de esta actividad fue estimar el esfuerzo necesario para completar cada una de las tareas definidas dentro del backlog del proyecto, permitiendo organizar el desarrollo del sistema de manera progresiva y realista.

Las estimaciones fueron utilizadas posteriormente para la planificación de los sprints y el seguimiento del avance del proyecto.

---

# Técnica de Estimación Utilizada

Para realizar las estimaciones se utilizó la técnica de **Story Points**, debido a que permite medir la complejidad y el esfuerzo relativo de cada tarea sin depender estrictamente de horas exactas.

Los Story Points fueron asignados considerando:

- Complejidad técnica.
- Cantidad de funcionalidades involucradas.
- Dificultad de implementación.
- Tiempo estimado de desarrollo.
- Necesidad de validaciones o integración con otras partes del sistema.

---

# Escala de Story Points Utilizada

## La escala utilizada fue basada en Fibonacci:

| Story Points | Complejidad |
|--------------|-------------|
| 1            | Muy baja    |
| 2            | Baja        |
| 3            | Media       |
| 5            | Alta        |
| 8            | Muy alta    |

---

# Backlog del Proyecto

El backlog fue organizado en función de las principales funcionalidades del sistema académico.

---

## Gestión de Usuarios y Seguridad

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Login y autenticación | 3 | Incluye autenticación de usuarios, validación de credenciales y gestión de sesiones de acceso | Login funcional. Validación correcta. Redirección según rol |
| Gestión de usuarios | 5 | Involucra roles, permisos y persistencia de datos | Registrar usuario con nombre, contraseña y rol. No permitir usuarios duplicados. Modificar información. Eliminar usuario. Validar datos obligatorios |
| Menú dinámico según rol | 3 | Permite mostrar opciones específicas según el tipo de usuario autenticado | Administrador visualiza todas las opciones. Docente visualiza sus funciones. Alumno visualiza su información |

---

## Base de Datos y Configuración

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Configuración de conexión SQLite | 5 | Incluye la configuración y validación de la conexión entre la aplicación y SQLite | Conexión funcionando correctamente. Sin errores |
| Configuración ActiveJDBC | 5 | Comprende la configuración del framework y modelos de persistencia | Modelos funcionando correctamente. Consultas válidas |

---

## Gestión de Alumnos

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Gestionar alumno | 5 | Incluye formularios, validaciones y persistencia | Registrar alumno. Evitar DNI duplicado. Modificar datos. Eliminar alumno. Mostrar mensajes claros |
| Visualizar datos del alumno | 2 | Consulta simple de información | Mostrar datos personales. Mostrar materias y notas. Sin errores |
| Visualizar estado académico del alumno | 8 | Implica consolidar información académica y situación general del alumno | Mostrar materias cursando, aprobadas y desaprobadas. Mostrar notas parciales y finales. Datos correctos |

---

## Gestión de Docentes

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Gestionar docente | 5 | Requiere manejo de datos y asignaciones | Registrar docente. Evitar duplicados por DNI. Modificar datos. No eliminar docentes con materias asignadas |
| Visualizar datos del docente | 2 | Funcionalidad de consulta sencilla | Mostrar datos del docente. Mostrar materias asignadas. Sin errores |
| Visualizar materias del docente | 2 | Requiere relaciones entre tablas y consultas | Mostrar listado de materias asignadas. Mostrar nombre y código. No mostrar materias incorrectas |
| Asignación docente-materia | 5 | Necesita relaciones entre docentes y materias | Asignar docente correctamente. Evitar asignaciones duplicadas |
| Visualizar alumnos inscriptos en una materia | 3 | Consulta y filtrado de datos | Seleccionar materia. Mostrar listado de alumnos. Mostrar nombre y DNI. Sin duplicados |

---

## Gestión Académica

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Gestionar carrera | 5 | Incluye creación, modificación y administración de carreras | Crear carrera. Editar carrera. Eliminar carrera. Validar datos |
| Gestionar materias | 8 | Incluye estructura académica, validaciones y persistencia | Crear materia. Código único. Modificar materia. Eliminar materia |
| Plan de estudio | 8 | Requiere vinculación entre carreras y materias | Vincular materias a carreras. Guardar correctamente. Permitir modificaciones |
| Listado de inscriptos por materia | 3 | Relación entre alumnos y materias | Seleccionar materia. Mostrar alumnos inscriptos. Información correcta |

---

## Inscripciones y Rendimiento Académico

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Inscripción a materias | 13 | Incluye validaciones académicas y persistencia de inscripciones | Alumno puede inscribirse. No permitir duplicados. Guardar en base de datos |
| Carga de notas | 5 | Gestión de calificaciones y actualización académica | Cargar notas correctamente. Guardar información. Actualizar estado académico |
| Visualizar notas del alumno | 3 | Consulta académica simple | Mostrar notas correctamente. Sin errores |

---

## Testing y Organización

| Tarea | Story Points | Justificación | Criterios de aceptación |
|--------|--------------|----------------|--------------------------|
| Testing manual | 2 | Validación funcional manual del sistema | Funcionalidades probadas. Sin errores críticos |
| Testing automatizado | 8 | Requiere análisis e implementación de pruebas automáticas | Herramientas evaluadas. Decisión documentada |
| Organización del equipo | 1 | Incluye coordinación y seguimiento general del proyecto | Tareas asignadas. Seguimiento realizado |

---

# Organización del Backlog

El backlog fue dividido en grupos funcionales para facilitar la planificación del desarrollo y mejorar la organización de tareas.

La agrupación realizada fue:

- Seguridad y autenticación.
- Configuración de persistencia.
- Gestión de alumnos.
- Gestión de docentes.
- Gestión académica.
- Inscripciones y notas.
- Testing y organización.

Esta división permitió priorizar primero las funcionalidades fundamentales del sistema y posteriormente incorporar módulos más complejos.

---

# Criterios generales de aceptación

Para asegurar la calidad del desarrollo y mantener un criterio uniforme dentro del proyecto, cada tarea del backlog se considera finalizada únicamente cuando cumple con las siguientes condiciones:

- La funcionalidad implementada cumple con los requerimientos definidos.
- El código compila correctamente sin errores.
- La funcionalidad puede ejecutarse correctamente desde la aplicación.
- Se validaron manualmente los casos principales de uso.
- No genera errores visibles en la interfaz ni en la consola.
- Los cambios fueron integrados correctamente al repositorio de GitHub.
- La tarea fue revisada por el otro integrante del equipo antes de marcarse como completada.

---

# ACTIVIDAD 6: Roadmap y Planificación del Proyecto

## Objetivo

El objetivo de esta actividad fue organizar temporalmente el desarrollo del sistema utilizando GitHub Projects y una vista Roadmap.

Se definieron distintos sprints para distribuir las tareas del backlog a lo largo del cuatrimestre y facilitar el seguimiento del avance del proyecto.

---

## Herramienta Utilizada

Se utilizó:

- GitHub Projects.
- Vista Kanban para seguimiento de tareas.
- Vista Roadmap para planificación temporal.
- Issues para representar cada funcionalidad del backlog.

---

## Organización en Sprints

El proyecto fue dividido en cinco sprints distribuidos durante el cuatrimestre académico.

La duración de cada sprint fue definida considerando:

- Complejidad de las tareas.
- Tiempo disponible del equipo.
- Dependencias entre funcionalidades.
- Necesidad de pruebas y correcciones.

---

## Planificación Temporal

| Sprint   | Fecha de Inicio | Fecha de Fin |
|----------|-----------------|--------------|
| Sprint 1 | 15/03/2026      | 31/03/2026   |
| Sprint 2 | 01/04/2026      | 17/04/2026   |
| Sprint 3 | 18/04/2026      | 04/05/2026   |
| Sprint 4 | 05/05/2026      | 21/05/2026   |
| Sprint 5 | 22/05/2026      | 11/06/2026   |

---

## Distribución de Tareas por Sprint

### Sprint 1 — Infraestructura y Seguridad

#### Objetivo
Implementar la estructura base del sistema y el mecanismo de autenticación.

#### Tareas
- Configuración SQLite.
- Configuración ActiveJDBC.
- Login y autenticación.
- Gestión de usuarios.
- Menú dinámico según rol.

#### Motivo de la planificación
Estas funcionalidades fueron priorizadas porque representan la base necesaria para el funcionamiento del resto del sistema.

---

### Sprint 2 — Gestión de Personas

#### Objetivo
Implementar la administración de alumnos y docentes.

#### Tareas
- Gestionar alumno.
- Visualizar datos del alumno.
- Gestionar docente.
- Visualizar datos del docente.

#### Motivo de la planificación
Se decidió abordar primero las entidades principales del dominio antes de avanzar con la estructura académica.

---

### Sprint 3 — Gestión Académica

#### Objetivo
Implementar la estructura de carreras y materias.

#### Tareas
- Gestionar carrera.
- Gestionar materias.
- Plan de estudio.
- Asignación docente-materia.

#### Motivo de la planificación
Estas tareas presentan una complejidad mayor debido a las relaciones entre tablas y dependencias académicas.

Por esta razón se asignó un período más amplio de desarrollo y validación.

---

### Sprint 4 — Inscripciones y Consultas

#### Objetivo
Desarrollar funcionalidades relacionadas con cursadas e inscripciones.

#### Tareas
- Inscripción a materias.
- Visualizar materias del docente.
- Visualizar alumnos inscriptos.
- Listado de inscriptos por materia.

#### Motivo de la planificación
Estas funcionalidades dependen directamente de la existencia previa de carreras, materias y usuarios correctamente configurados.

---

### Sprint 5 — Rendimiento Académico y Cierre

#### Objetivo
Finalizar el sistema y realizar pruebas generales.

#### Tareas
- Carga de notas.
- Visualizar notas del alumno.
- Estado académico del alumno.
- Testing manual.
- Testing automatizado.
- Corrección de errores.
- Documentación final.

#### Motivo de la planificación
El último sprint fue destinado a pruebas, correcciones y funcionalidades finales debido a que requieren un sistema previamente estable y funcional.

---

## Justificación de la Duración de los Sprints

La cantidad de días asignada a cada sprint fue definida considerando:

- La disponibilidad horaria de los integrantes.
- La carga académica del cuatrimestre.
- La complejidad técnica de cada módulo.
- El tiempo necesario para pruebas y correcciones.
- La integración progresiva entre frontend, backend y base de datos.

Además, se dejó un margen mayor en el último sprint para resolver errores detectados durante las pruebas finales y completar la documentación requerida.

---

## Uso del Roadmap

El Roadmap permitió:

- Visualizar el progreso general del proyecto.
- Organizar prioridades.
- Distribuir tareas entre los integrantes.
- Mantener una planificación temporal coherente.
- Identificar dependencias entre funcionalidades.

La planificación fue mantenida y actualizada durante el desarrollo del sistema utilizando GitHub Projects.