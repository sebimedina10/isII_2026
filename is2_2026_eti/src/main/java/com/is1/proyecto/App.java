package com.is1.proyecto; // Define el paquete de la aplicación, debe coincidir con la estructura de carpetas.

// Importaciones necesarias para la aplicación Spark
import java.util.HashMap; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import java.util.Map; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import java.util.List;
import org.javalite.activejdbc.Base; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import org.mindrot.jbcrypt.BCrypt; // Utilidad para hashear y verificar contraseñas de forma segura.

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper; // Representa un modelo de datos y el nombre de la vista a renderizar.
import com.is1.proyecto.config.DBConfigSingleton; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.models.Alumno; // Para crear mapas de datos (modelos para las plantillas).
import com.is1.proyecto.models.Docente;
import com.is1.proyecto.models.Persona;
import com.is1.proyecto.models.User;

import spark.ModelAndView; // Interfaz Map, utilizada para Map.of() o HashMap.
import static spark.Spark.after; // Clase Singleton para la configuración de la base de datos.
import static spark.Spark.before; // Modelo de ActiveJDBC que representa la tabla 'users'.
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;


/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    // Instancia estática y final de ObjectMapper para la serialización/deserialización JSON.
    // Se inicializa una sola vez para ser reutilizada en toda la aplicación.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {
            port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones (por defecto es 4567).

            // Obtener la instancia única del singleton de configuración de la base de datos.
            DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

            // --- Filtro 'before' para gestionar la conexión a la base de datos ---
            // Este filtro se ejecuta antes de cada solicitud HTTP.
            before((req, res) -> {
                try {
                    // Abre una conexión a la base de datos utilizando las credenciales del singleton.
                    if (!Base.hasConnection()) {
                        Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                    }
                    System.out.println("DEBUG URL: " + req.requestMethod() + " " + req.url());
                    //System.out.println("DEBUG SESSION ID: " + req.session().id());
                    //System.out.println("DEBUG USER_ID EN SESION: " + req.session().attribute("userId"));

                } catch (Exception e) {
                    // Si ocurre un error al abrir la conexión, se registra y se detiene la solicitud
                    // con un código de estado 500 (Internal Server Error) y un mensaje JSON.
                    System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                    halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos.\"}" + e.getMessage());
                }
            });

            // --- Filtro 'after' para cerrar la conexión a la base de datos ---
            // Este filtro se ejecuta después de que cada solicitud HTTP ha sido procesada.
            after((req, res) -> {
                try {
                    // Cierra la conexión a la base de datos para liberar recursos.
                        Base.close();
                } catch (Exception e) {
                    // Si ocurre un error al cerrar la conexión, se registra.
                    System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
                }
            });

        // --- Rutas GET para renderizar formularios y páginas HTML ---

        // GET: Muestra el formulario de creación de cuenta.
        // Soporta la visualización de mensajes de éxito o error pasados como query parameters.
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            // Obtener y añadir mensaje de éxito de los query parameters (ej. ?message=Cuenta creada!)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            // Obtener y añadir mensaje de error de los query parameters (ej. ?error=Campos vacíos)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        // Requiere que el usuario esté autenticado.
        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantilla del dashboard.

            // Intenta obtener el nombre de usuario y la bandera de login de la sesión.
            String currentUsername = req.session().attribute("currentUserUsername");
            Boolean loggedIn = req.session().attribute("loggedIn");
            Object userId = req.session().attribute("userId");

            // 1. Verificar si el usuario ha iniciado sesión.
            // Si no hay un nombre de usuario en la sesión, la bandera es nula o falsa,
            // significa que el usuario no está logueado o su sesión expiró.
            if (currentUsername == null || loggedIn == null || !loggedIn || userId == null) {
                System.out.println("DEBUG: Acceso no autorizado a /dashboard. Redirigiendo a /login.");

                // Redirige al login con un mensaje de error.
                res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión", StandardCharsets.UTF_8));
                return null; // Importante retornar null después de una redirección.
            }

            // 2. Si el usuario está logueado, añade el nombre de usuario al modelo para la plantilla.
            model.put("username", currentUsername);

            User user = User.findFirst("name = ?", currentUsername);

            if (user == null) {
                // Caso raro pero importante: usuario no existe
                req.session().invalidate();
                res.redirect("/?error=Usuario no válido.");
                return null;
            }

            String type = user.getString("type");

            // 3. Flags para el frontend (Mustache)

            boolean isAlumno = "ALUMNO".equalsIgnoreCase(type);
            boolean isDocente = "DOCENTE".equalsIgnoreCase(type);
            boolean isAdmin = "ADMINISTRADOR".equalsIgnoreCase(type);

            // 4. Datos para la vista
            model.put("username", currentUsername);
            model.put("isAlumno", isAlumno);
            model.put("isDocente", isDocente);
            model.put("isAdmin", isAdmin);
            model.put("isAlumnoOrDocente", isAlumno || isDocente);

            System.out.println("DEBUG: Usuario=" + currentUsername + " Tipo=" + type);

            // 5. Renderiza la plantilla del dashboard con el nombre de usuario.
            return new ModelAndView(model, "dashboard.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta para cerrar la sesión del usuario.
        get("/logout", (req, res) -> {
            // Invalida completamente la sesión del usuario.
            // Esto elimina todos los atributos guardados en la sesión y la marca como inválida.
            // La cookie JSESSIONID en el navegador también será gestionada para invalidarse.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");

            // Redirige al usuario a la página de login con un mensaje de éxito.
            res.redirect("/");

            return null; // Importante retornar null después de una redirección.
        });

        // GET: Muestra el formulario de inicio de sesión (login).
        // Nota: Esta ruta debería ser capaz de leer también mensajes de error/éxito de los query params
        // si se la usa como destino de redirecciones. (Tu código de /user/create ya lo hace, aplicar similar).
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }
            return new ModelAndView(model, "login.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta de alias para el formulario de creación de cuenta.
        // En una aplicación real, probablemente querrías unificar con '/user/create' para evitar duplicidad.
        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache"); // No pasa un modelo específico, solo el formulario.
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.


        //El objetivo del POST es recibir los datos que el usuario ingresó en el formulario 
        //(el que devolvió el GET) y guardarlos en la base de datos.
        // POST: Maneja el envío del formulario de creación de nueva cuenta.
        //Req contiene toda la información enviada por el cliente en el formulario.
        post("/user/new", (req, res) -> {
            String name = req.queryParams("name");
            String password = req.queryParams("password");
            String dni = req.queryParams("dni");
            String type = req.queryParams("type");

            // Validaciones básicas: campos no pueden ser nulos o vacíos.
            if (name == null || name.isEmpty() || password == null || password.isEmpty() || dni == null || dni.isEmpty()
            || type == null || type.isEmpty()) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formulario de creación con un mensaje de error.
                res.redirect("/user/create?error=Todos los campos son requeridos.");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }


            // Validaciones básicas: chequear repetidos.
            User userExistente = User.findFirst("name = ?", name);
            Persona other =  Persona.findFirst("dni = ?", dni);
            if (userExistente != null || other != null) {        
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formularo de creación con un mensaje de error.
                res.redirect("/user/create?error=Usuario ya registrado");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }

            //si no se respeta el tipo de usuario tenemos error. 
            if (!type.equals("ADMINISTRADOR") && !type.equals("ALUMNO") && !type.equals("DOCENTE")) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formulario de creación con un mensaje de error.
                res.redirect("/user/create?error=Tipo de usuario inválido.");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }
            Persona persona = Persona.findFirst("dni = ?", dni);
            try {
                //Busca si existe Persona 
                if (persona == null) {
                    persona = new Persona();
                    persona.set("dni", dni);
                    persona.saveIt();
                }
                if (type.equals("ALUMNO")) {
                    Alumno alumno = new Alumno();
                    alumno.set("dni", persona.get("dni"));
                    alumno.saveIt();
                } else if (type.equals("DOCENTE")) {
                    Docente docente = new Docente();
                    docente.set("dni", persona.get("dni"));
                    docente.saveIt();
                }       
                // Intenta crear y guardar la nueva cuenta en la base de datos.
                User ac = new User(); // Crea una nueva instancia del modelo User.
                // Hashea la contraseña de forma segura antes de guardarla.
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                ac.set("name", name); // Asigna el nombre de usuario.
                ac.set("password", hashedPassword); // Asigna la contraseña hasheada.
                ac.set("type", type);
                System.out.println("DEBUG REGISTER TYPE: " + type);
                ac.set("id_persona", persona.getId()); 
                ac.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Código de estado HTTP 201 (Created) para una creación exitosa.
                // Redirige al formulario de creación con un mensaje de éxito.
                res.redirect("/user/create?message=Cuenta creada exitosamente para " + name);
                return ""; // Retorna una cadena vacía.

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB (ej. nombre de usuario duplicado),
                // se captura aquí y se redirige con un mensaje de error.
                System.err.println("Error al registrar la cuenta: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Código de estado HTTP 500 (Internal Server Error).
                res.redirect("/user/create?error=Error interno al crear la cuenta. Intente de nuevo.");
                return ""; // Retorna una cadena vacía.
            }
        });


        // POST: Maneja el envío del formulario de inicio de sesión.
        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantilla de login o dashboard.

            String username = req.queryParams("username");
            String plainTextPassword = req.queryParams("password");

            // Validaciones básicas: campos de usuario y contraseña no pueden ser nulos o vacíos.
            if (username == null || username.isEmpty() || plainTextPassword == null || plainTextPassword.isEmpty()) {
                res.status(400); // Bad Request.
                model.put("errorMessage", "El nombre de usuario y la contraseña son requeridos.");
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }

            // Busca la cuenta en la base de datos por el nombre de usuario.
            User ac = User.findFirst("name = ?", username);

            // Si no se encuentra ninguna cuenta con ese nombre de usuario.
            if (ac == null) {
                res.status(401); // Unauthorized.
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos."); // Mensaje genérico por seguridad.
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }

            // Obtiene la contraseña hasheada almacenada en la base de datos.
            String storedHashedPassword = ac.getString("password");

            // Compara la contraseña en texto plano ingresada con la contraseña hasheada almacenada.
            // BCrypt.checkpw hashea la plainTextPassword con el salt de storedHashedPassword y compara.
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                // Autenticación exitosa.
                res.status(200);

                // --- Gestión de Sesión ---
                String userIdVal = ac.getString("id_user");
                req.session(true).attribute("userId", userIdVal); // Guarda el ID de la cuenta en la sesión (útil).
                req.session().attribute("currentUserUsername", username); // Guarda el nombre de usuario en la sesión.
                req.session().attribute("loggedIn", true); // Establece una bandera para indicar que el usuario está logueado.

                System.out.println("DEBUG: Login exitoso para la cuenta: " + username);

                res.redirect("/dashboard");
                return null;
            } else {
                // Contraseña incorrecta.
                res.status(401); // Unauthorized.
                System.out.println("DEBUG: Intento de login fallido para: " + username);
                model.put("errorMessage", "Usuario o contraseña incorrectos."); // Mensaje genérico por seguridad.
                return new ModelAndView(model, "login.mustache"); // Renderiza la plantilla de login con error.
            }
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta POST.


        // POST: Endpoint para añadir usuarios (API que devuelve JSON, no HTML).
        // Advertencia: Esta ruta tiene un propósito diferente a las de formulario HTML.
        post("/add_users", (req, res) -> {
            res.type("application/json"); // Establece el tipo de contenido de la respuesta a JSON.

            // Obtiene los parámetros 'name' y 'password' de la solicitud.
            String name = req.queryParams("name");
            String password = req.queryParams("password");

            // --- Validaciones básicas ---
            if (name == null || name.isEmpty() || password == null || password.isEmpty()) {
                res.status(400); // Bad Request.
                return objectMapper.writeValueAsString(Map.of("error", "Nombre y contraseña son requeridos."));
            }

            try {
                // --- Creación y guardado del usuario usando el modelo ActiveJDBC ---
                User newUser = new User(); // Crea una nueva instancia de tu modelo User.
                // ¡ADVERTENCIA DE SEGURIDAD CRÍTICA!
                // En una aplicación real, las contraseñas DEBEN ser hasheadas (ej. con BCrypt)
                // ANTES de guardarse en la base de datos, NUNCA en texto plano.
                // (Nota: El código original tenía la contraseña en texto plano aquí.
                // Se recomienda usar `BCrypt.hashpw(password, BCrypt.gensalt())` como en la ruta '/user/new').
                newUser.set("name", name); // Asigna el nombre al campo 'name'.
                newUser.set("password", password); // Asigna la contraseña al campo 'password'.
                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Created.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Internal Server Error.
                return objectMapper.writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });
    //Métodos de HTTP protocolo para transferir info entre servidor y cliente.
    
// GET: Muestra el formulario con los campos para completar
        get("/datos", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Intentamos obtener el ID o el Username
            Object sessionUserId = req.session().attribute("userId");
            String sessionUsername = req.session().attribute("currentUserUsername");

            if (sessionUserId == null && sessionUsername == null) {
                System.out.println("DEBUG: No hay sesión en /datos. Redirigiendo...");
                res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión", StandardCharsets.UTF_8));
                return null;
            }

            // Buscamos al usuario (priorizamos ID, si no por nombre)
            User user = null;
            if (sessionUserId != null) {
               user = User.findFirst("id_user = ?", sessionUserId);
            } else {
                user = User.findFirst("name = ?", sessionUsername);
            }

            if (user == null) {
                res.redirect("/?error=Usuario+no+encontrado");
                return null;
            }

            // Flag de tipos para Mustache
            String type = user.getString("type");
            model.put("isAlumno", "ALUMNO".equalsIgnoreCase(type));
            model.put("isDocente", "DOCENTE".equalsIgnoreCase(type));
            model.put("isAdmin", "ADMINISTRADOR".equalsIgnoreCase(type));

            // Cargar datos de Persona vinculada
            Object idPersona = user.get("id_persona");
            if (idPersona != null) {
                Persona p = Persona.findById(idPersona);
                if (p != null) {
                    model.put("dni", p.get("dni"));
                    model.put("nombre", p.get("nombre"));
                    model.put("apellido", p.get("apellido"));
                    model.put("email", p.get("email"));
                    model.put("telefono", p.get("telefono"));

                    if (p.get("fecha_nacimiento") != null) {
                        String fecha = p.getString("fecha_nacimiento"); // Formato SQL: YYYY-MM-DD
                        String[] partes = fecha.split("-");
                        model.put("anio_val", partes[0]);
                        model.put("mes_val", partes[1]);
                        model.put("dia_val", partes[2]);
                    }

                    if ("DOCENTE".equalsIgnoreCase(type)) {
                        Docente d = Docente.findFirst("dni = ?", p.get("dni"));
                        if (d != null) {
                            model.put("titulo", d.get("titulo"));
                            model.put("rol", d.get("rol"));
                            model.put("id_facultad_actual", d.get("id_facultad"));
                        }
                    }
                }
            }

            model.put("facultades", Base.findAll("SELECT id_facultad, nombre FROM facultad"));
            return new ModelAndView(model, "datos.mustache");
        }, new MustacheTemplateEngine());


        post("/datos", (req, res) -> {
            try {
                // 1. Verificación robusta de la sesión
                String sessionUserId = req.session().attribute("userId");

                if (sessionUserId == null) {
                    res.redirect("/?error=" + URLEncoder.encode("Sesión expirada", "UTF-8"));
                    return null;
                }

                // 2. Buscamos al usuario existente
                User user = User.findFirst("id_user = ?", sessionUserId);

                if (user == null) {
                    res.redirect("/?error=Usuario+no+existe");
                    return null;
                }

                String tipo = user.getString("type");

                // 3. Capturar datos del formulario
                String dni = req.queryParams("dni");
                String nombre = req.queryParams("nombre");
                String apellido = req.queryParams("apellido");
                String email = req.queryParams("email");
                String telefono = req.queryParams("telefono");

                // 4. Manejo de la Fecha de Nacimiento
                String dia = req.queryParams("dia");
                String mes = req.queryParams("mes");
                String anio = req.queryParams("anio");
                String fechaNacimientoFull = (dia != null && !dia.isEmpty()) ? anio + "-" + mes + "-" + dia : null;

                // 5. Actualizar o Crear Persona
                Persona p = Persona.findFirst("dni = ?", dni);
                if (p == null) {
                    p = new Persona();
                    p.set("dni", dni);
                }

                p.set("nombre", nombre);
                p.set("apellido", apellido);
                p.set("email", email);
                p.set("telefono", telefono);
                p.set("fecha_nacimiento", fechaNacimientoFull);
                p.saveIt();

                // 6. Asegurar vínculo Usuario -> Persona y GUARDAR
                // Usamos UPDATE directo para que no intente crear un usuario nuevo
                Base.exec("UPDATE users SET id_persona = ? WHERE id_user = ?", p.getId(), sessionUserId);

                // 7. Lógica específica por tipo de usuario
                if ("DOCENTE".equalsIgnoreCase(tipo)) {
                    Docente d = Docente.findFirst("dni = ?", dni);
                    if (d == null) d = new Docente();
                    d.set("dni", dni);
                    d.set("titulo", req.queryParams("titulo"));
                    d.set("rol", req.queryParams("rol"));
                    d.set("id_facultad", req.queryParams("id_facultad"));
                    d.saveIt();
                } else if ("ALUMNO".equalsIgnoreCase(tipo)) {
                    Alumno al = Alumno.findFirst("dni = ?", dni);
                    if (al == null) {
                        al = new Alumno();
                        al.set("dni", dni);
                        al.set("progreso", 0);
                    }
                    al.saveIt();
                }

                res.redirect("/dashboard?message=" + URLEncoder.encode("Datos guardados con éxito", "UTF-8"));
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/datos?error=" + URLEncoder.encode("Error técnico al guardar", "UTF-8"));
                return null;
            }
        });

        get("/admin/carrera", (req, res) -> {

            String userId = req.session().attribute("userId");

            if (userId == null) {
                res.redirect("/");
                return null;
            }

            User user = User.findFirst("id_user = ?", userId);

            if (user == null || !"ADMINISTRADOR".equalsIgnoreCase(user.getString("type"))) {
                halt(403, "No autorizado");
            }

            Map<String, Object> model = new HashMap<>();

            model.put("facultades", Base.findAll("SELECT id_facultad, nombre FROM facultad"));
            model.put("carreras",
                Base.findAll("SELECT nombre_carrera as nombre, cant_anios as anios FROM carrera"));

            String error = req.queryParams("error");
            if (error != null) model.put("error", error);

            return new ModelAndView(model, "admin_carrera.mustache");

        }, new MustacheTemplateEngine());

post("/admin/carrera", (req, res) -> {
            try {
                String nombre = req.queryParams("nombre_carrera");
                String anios = req.queryParams("anios");
                String idFacultad = req.queryParams("id_facultad");

                // Verificamos si la carrera ya existe
                // Usamos Number para evitar el ClassCastException entre Integer y Long
                Number existeCarrera = (Number) Base.firstCell("SELECT count(*) FROM carrera WHERE nombre_carrera = ?", nombre);

                if (existeCarrera.longValue() > 0) {
                    res.redirect("/admin/carrera?error=" + URLEncoder.encode("La carrera ya existe", "UTF-8"));
                    return null;
                }

                if (nombre == null || nombre.isEmpty()) {
                    res.redirect("/admin/carrera?error=" + URLEncoder.encode("El nombre es obligatorio", "UTF-8"));
                    return null;
                }

                // 1. Insertar la nueva carrera
                Base.exec("INSERT INTO carrera (nombre_carrera, cant_anios, id_facultad) VALUES (?, ?, ?)",
                          nombre, anios, idFacultad);

                Object idCarrera = Base.firstCell("SELECT last_insert_rowid()");

                // 3. Procesar los parámetros dinámicos de las materias
                Map<String, String[]> params = req.queryMap().toMap();

                for (String key : params.keySet()) {
                    if (key.startsWith("materia_codigo_")) {
                        String[] partes = key.split("_");
                        String anioPerteneciente = partes[2];
                        String index = partes[3];

                        String codigo = req.queryParams("materia_codigo_" + anioPerteneciente + "_" + index);
                        String nombreMat = req.queryParams("materia_nombre_" + anioPerteneciente + "_" + index);
                        String horas = req.queryParams("materia_horas_" + anioPerteneciente + "_" + index);
                        String cuatri = req.queryParams("materia_cuatri_" + anioPerteneciente + "_" + index);

                        if (nombreMat != null && !nombreMat.isEmpty()) {

                            // VALIDACIÓN DE CÓDIGO DE MATERIA:
                            Number existeMateria = (Number) Base.firstCell("SELECT count(*) FROM materia WHERE codigo = ?", codigo);

                            if (existeMateria.longValue() > 0) {
                                // Si el código ya existe, podemos optar por no insertarla o saltarla
                                // Aquí simplemente la saltamos para no romper todo el proceso, pero podrías avisar al usuario
                                System.out.println("DEBUG: El codigo de materia " + codigo + " ya existe. Saltando...");
                                continue;
                            }

                            // 4. Guardar la materia
                            Base.exec("INSERT INTO materia (codigo, nombre_materia, anio_pertenece, cant_horas, periodo) VALUES (?, ?, ?, ?, ?)",
                                      codigo, nombreMat, anioPerteneciente, horas, cuatri);

                            Object idMateria = Base.firstCell("SELECT last_insert_rowid()");

                            // 5. Relacionar Carrera con Materia
                            Base.exec("INSERT INTO plan_estudio (id_carrera, id_materia) VALUES (?, ?)",
                                      idCarrera, idMateria);
                        }
                    }
                }

                res.redirect("/admin/carrera?success=" + URLEncoder.encode("Carrera guardada con éxito", "UTF-8"));
                return null;

            } catch (Exception e) {
                System.err.println("Error al guardar carrera: " + e.getMessage());
                e.printStackTrace();
                res.redirect("/admin/carrera?error=" + URLEncoder.encode("Error interno: " + e.getMessage(), "UTF-8"));
                return null;
            }
        });

    } // Fin del método main
} // Fin de la clase App