## ACTIVIDAD 2: (Auditoría) Análisis de riesgos con IA

### a) Riesgos identificados por la IA

| Tipo de Riesgo | Descripción | Probabilidad | Impacto |
|----------------|-------------|--------------|---------|
| Técnico | Problemas en el diseño de la base de datos debido a relaciones complejas entre entidades académicas (planes de estudio, correlatividades e inscripciones). | Alta | Crítico |
| Técnico | Errores en la implementación de reglas de negocio y validaciones académicas. | Media | Alto |
| Técnico | Integración incorrecta entre el backend desarrollado en Spark Java y la base de datos SQLite. | Media | Alto |
| Técnico | Limitaciones de escalabilidad derivadas del uso de SQLite ante un crecimiento significativo de usuarios y datos. | Baja | Medio |
| Organizacional | Falta de comunicación entre los integrantes del equipo. | Media | Alto |
| Organizacional | Distribución desigual de tareas durante el desarrollo. | Media | Medio |
| Planificación | Subestimación del tiempo requerido para implementar funcionalidades complejas. | Alta | Crítico |
| Planificación | Modificaciones de requerimientos durante el desarrollo del proyecto. | Alta | Crítico |
| Planificación | Definición insuficiente de prioridades dentro del backlog de trabajo. | Media | Medio |
| Humano | Falta de experiencia previa en algunas tecnologías utilizadas. | Media | Alto |
| Humano | Desmotivación o baja participación de algún integrante del equipo. | Baja | Medio |
| Humano | Errores humanos durante la carga o modificación de información académica. | Alta | Medio |

---

### b) Riesgos identificados por el equipo

| Tipo de Riesgo | Descripción | Probabilidad | Impacto |
|----------------|-------------|--------------|---------|
| Técnico | Errores en la conexión entre la aplicación y la base de datos SQLite. | Alta | Alto |
| Técnico | Fallos en las validaciones de formularios y persistencia de datos. | Media | Medio |
| Técnico | Ausencia de pruebas automatizadas que permitan detectar errores tempranamente. | Alta | Alto |
| Organizacional | Problemas de comunicación entre los integrantes durante determinadas etapas del proyecto. | Baja | Medio |
| Organizacional | Falta de reuniones periódicas que pueda generar descoordinación en las tareas asignadas. | Media | Medio |
| Organizacional | Conflictos o errores en la gestión del repositorio GitHub y control de versiones. | Media | Medio |
| Planificación | Riesgo de incumplimiento de fechas de entrega establecidas para cada actividad. | Baja | Alto |
| Humano | Dificultad para resolver problemas técnicos complejos debido a la limitada experiencia previa. | Alta | Crítico |
| Humano | Sobrecarga de responsabilidades por la reducida cantidad de integrantes del equipo. | Alta | Bajo |
| Humano | Dificultad para detectar errores utilizando únicamente pruebas manuales. | Alta | Alto |

---

### c) Comparación de análisis

#### Riesgos identificados por la IA y no por el equipo

- Limitaciones de escalabilidad derivadas del uso de SQLite.
- Posible desmotivación o baja participación de integrantes.
- Cambios de requerimientos durante el desarrollo.
- Problemas relacionados con la priorización del backlog.
- Complejidad de implementación de correlatividades y reglas académicas.

#### Riesgos identificados por el equipo y no por la IA

- Errores de conexión entre la aplicación y la base de datos.
- Dificultades en el uso de GitHub y el control de versiones.
- Ausencia de pruebas automatizadas.
- Problemas de sincronización entre ActiveJDBC y la estructura real de la base de datos.

#### Evaluación de la calidad del análisis

El análisis realizado mediante IA permitió identificar riesgos generales relacionados con aspectos técnicos, organizacionales y de planificación que suelen aparecer en proyectos de desarrollo de software similares. Gracias a ello se obtuvieron perspectivas adicionales sobre escalabilidad, gestión del backlog, cambios de requerimientos y complejidad de reglas de negocio.

Por su parte, el análisis realizado por el equipo se enfocó principalmente en problemas concretos observados durante el desarrollo del proyecto, como errores de configuración de la base de datos, inconsistencias en el esquema de persistencia, dificultades en las validaciones y problemas asociados al uso de herramientas de control de versiones.

La combinación de ambos enfoques resultó complementaria y permitió obtener una visión más completa de los riesgos potenciales del proyecto. Mientras que la IA aportó una perspectiva amplia basada en buenas prácticas y experiencias generales de la industria, el equipo contribuyó con situaciones reales derivadas de la implementación efectiva del sistema.