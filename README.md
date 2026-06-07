# Sistema de Gestión Universitaria
### Comisión N.º 3 - Ingeniería de Software I

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)![Spark](https://img.shields.io/badge/Spark-000000?style=for-the-badge&logo=apachespark&logoColor=white)![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)![ActiveJDBC](https://img.shields.io/badge/ActiveJDBC-2E8B57?style=for-the-badge)![Mustache](https://img.shields.io/badge/Mustache-000000?style=for-the-badge&logo=mustache&logoColor=white)![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

**Tecnologías utilizadas:** Java · Spark Java · SQLite · ActiveJDBC · Mustache · Maven

Bienvenido al repositorio del **Sistema de Gestión Universitaria**, una aplicación web desarrollada en Java que permite administrar usuarios, carreras, materias, docentes, alumnos e información académica de manera centralizada.

El sistema está orientado a facilitar las tareas administrativas y académicas mediante una interfaz web simple, permitiendo la gestión de inscripciones, planes de estudio, asignación de docentes y seguimiento del rendimiento estudiantil.

---

## 📚 Documentación del Proyecto

Accedé a la documentación detallada de cada etapa del desarrollo:

- 📘 [Análisis de Requerimientos](documentacion/Requirements.md)
- 🎨 [Diseño y Arquitectura](documentacion/Desing.md)
- 🛡️ [Auditoría y Gestión de Riesgos](documentacion/Auditoria.md)
- 📅 [Planificación y Estimación](documentacion/Planificacion_y_Estimacion.md)
- 📄 [Documento Integrador](documentacion/IS2_Proyecto.md)

---

## 📂 Estructura del Proyecto

```text
isII_2026/
│
├── 📂 documentacion/
│
├── 📂 is2_2026_eti/
│   │
│   ├── 📂 db/
│   │   ├── dev.db
│   │   └── prod.db
│   │
│   ├── 📂 src/
│   │   ├── 📂 main/
│   │   │   ├── 📂 java/
│   │   │   │   └── 📂 com/
│   │   │   │       └── 📂 is1/
│   │   │   │           └── 📂 proyecto/
│   │   │   │               ├── 📄 App.java
│   │   │   │               ├── 📂 config/
│   │   │   │               └── 📂 models/
│   │   │   │
│   │   │   └── 📂 resources/
│   │   │       ├── 📂 templates/
│   │   │       └── 📄 scheme.sql
│   │   │
│   │   └── 📂 test/
│   │
│   ├── 📄 pom.xml
│   └── 📄 LICENSE
│
└── 📄 README.md
```

## 🚀 Guía de Ejecución

### Requisitos

* Java 21 o superior
* Maven 3.8 o superior
* SQLite3 instalado en el sistema

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/sebimedina10/isII_2026.git
cd isII_2026/is2_2026_eti
```

---

## 2. Compilar el proyecto

```bash
mvn clean install
```

---

## 3. Inicializar la base de datos

### Crear la estructura de tablas

```bash
sqlite3 db/dev.db < src/main/resources/scheme.sql
```

### Cargar datos iniciales

```bash
sqlite3 db/dev.db < src/main/resources/seed_unrc.sql
```

---

## 4. Ejecutar la aplicación

```bash
mvn exec:java
```

---

## 5. Acceder al sistema

Una vez iniciado el servidor, abrir en el navegador:

```text
http://localhost:8080
```

---


## Arquitectura Utilizada

El sistema sigue una arquitectura basada en el patrón **MVC (Modelo - Vista - Controlador)**:

- **Modelo:** ActiveJDBC + SQLite.
- **Vista:** Mustache + HTML + Tailwind CSS.
- **Controlador:** Rutas y lógica implementadas mediante Spark Java.

Esto permite mantener separadas las responsabilidades de persistencia, lógica de negocio e interfaz de usuario.

---

## Equipo de Desarrollo

| Integrante | Responsabilidades | Github |
|------------|-------------------|--------|
| Leandro Olivero | Base de datos, backend, integración y documentación | [@Leoolivero](https://github.com/Leoolivero) |
| Sebastian Medina | Requerimientos, frontend, pruebas y documentación | [@sebimedina10](https://github.com/sebimedina10) |

---

## 📄 Licencia

Proyecto académico desarrollado para las asignaturas **"Ingeniería de Software I" e "Ingeniería de Software II"**.