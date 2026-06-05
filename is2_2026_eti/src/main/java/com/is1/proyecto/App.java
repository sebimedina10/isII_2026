package com.is1.proyecto; // Define el paquete de la aplicación, debe coincidir con la estructura de carpetas.

// Importaciones necesarias para la aplicación Spark
import java.net.URLEncoder; // Utilidad para serializar/deserializar objetos Java a/desde JSON.
import java.nio.charset.StandardCharsets; // Importa los métodos estáticos principales de Spark (get, post, before, after, etc.).
import java.util.ArrayList;
import java.util.HashMap; // Clase central de ActiveJDBC para gestionar la conexión a la base de datos.
import java.util.List;
import java.util.Map;

import org.javalite.activejdbc.Base; // Utilidad para hashear y verificar contraseñas de forma segura.
import org.mindrot.jbcrypt.BCrypt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.is1.proyecto.config.DBConfigSingleton; // Representa un modelo de datos y el nombre de la vista a renderizar.
import com.is1.proyecto.models.Alumno; // Motor de plantillas Mustache para Spark.
import com.is1.proyecto.models.Docente; // Para crear mapas de datos (modelos para las plantillas).
import com.is1.proyecto.models.Persona;
import com.is1.proyecto.models.User;

import spark.ModelAndView;
import static spark.Spark.after; // Interfaz Map, utilizada para Map.of() o HashMap.
import static spark.Spark.before; // Clase Singleton para la configuración de la base de datos.
import static spark.Spark.get; // Modelo de ActiveJDBC que representa la tabla 'users'.
import static spark.Spark.halt;
import static spark.Spark.port;
import static spark.Spark.post;
import spark.template.mustache.MustacheTemplateEngine;

/**
 * Clase principal de la aplicación Spark.
 * Configura las rutas, filtros y el inicio del servidor web.
 */
public class App {

    // Instancia estática y final de ObjectMapper para la
    // serialización/deserialización JSON.
    // Se inicializa una sola vez para ser reutilizada en toda la aplicación.
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Método principal que se ejecuta al iniciar la aplicación.
     * Aquí se configuran todas las rutas y filtros de Spark.
     */
    public static void main(String[] args) {
        port(8080); // Configura el puerto en el que la aplicación Spark escuchará las peticiones
                    // (por defecto es 4567).

        // Obtener la instancia única del singleton de configuración de la base de
        // datos.
        DBConfigSingleton dbConfig = DBConfigSingleton.getInstance();

        // --- Filtro 'before' para gestionar la conexión a la base de datos ---
        // Este filtro se ejecuta antes de cada solicitud HTTP.
        before((req, res) -> {
            try {
                // Abre una conexión a la base de datos utilizando las credenciales del
                // singleton.
                if (!Base.hasConnection()) {
                    Base.open(dbConfig.getDriver(), dbConfig.getDbUrl(), dbConfig.getUser(), dbConfig.getPass());
                }
                System.out.println("DEBUG URL: " + req.requestMethod() + " " + req.url());
            } catch (Exception e) {
                // Si ocurre un error al abrir la conexión, se registra y se detiene la
                // solicitud
                System.err.println("Error al abrir conexión con ActiveJDBC: " + e.getMessage());
                halt(500, "{\"error\": \"Error interno del servidor: Fallo al conectar a la base de datos. "
                        + e.getMessage() + "\"}");
            }
        });

        // --- Filtro 'before' para gestionar la autorización de acceso centralizada ---
        // Este filtro se ejecuta después de abrir la conexión a la base de datos.
        before((req, res) -> {
            String path = req.pathInfo();

            // Ignorar el filtro para las rutas públicas
            if (path.equals("/") || path.equals("/login") || path.equals("/user/create") || path.equals("/user/new")
                    || path.equals("/add_users") || path.equals("/logout")) {
                return;
            }

            // 1. Verificar si el usuario ha iniciado sesión
            String userId = req.session().attribute("userId");
            if (userId == null) {
                System.out.println("DEBUG AUTH: Acceso denegado a " + path + ". Redirigiendo al login.");
                res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión para acceder.", "UTF-8"));
                halt();
            }

            // 2. Obtener el usuario de la base de datos
            User user = User.findFirst("id_user = ?", userId);
            if (user == null) {
                System.out.println("DEBUG AUTH: Usuario no encontrado en BD. Invalidando sesión.");
                req.session().invalidate();
                res.redirect("/?error=" + URLEncoder.encode("Sesión inválida.", "UTF-8"));
                halt();
            }

            String role = user.getString("type");

            // 3. Control de acceso por Roles (Autorización)
            if (path.startsWith("/admin/")) {
                if (!"ADMINISTRADOR".equalsIgnoreCase(role)) {
                    System.out.println("DEBUG AUTH: Acceso prohibido a " + path + " para el rol " + role);
                    halt(403, "No tienes permisos de Administrador para acceder a esta sección.");
                }
            } else if (path.startsWith("/docente/")) {
                if (!"DOCENTE".equalsIgnoreCase(role)) {
                    System.out.println("DEBUG AUTH: Acceso prohibido a " + path + " para el rol " + role);
                    halt(403, "No tienes permisos de Docente para acceder a esta sección.");
                }
            } else if (path.startsWith("/inscripcion/")) {
                if (!"ALUMNO".equalsIgnoreCase(role)) {
                    System.out.println("DEBUG AUTH: Acceso prohibido a " + path + " para el rol " + role);
                    halt(403, "No tienes permisos de Alumno para acceder a esta sección.");
                }
            } else if (path.equals("/profile")) {
                if (!"ALUMNO".equalsIgnoreCase(role) && !"DOCENTE".equalsIgnoreCase(role)) {
                    halt(403, "No tienes permisos para acceder a esta sección.");
                }
            }
        });

        // --- Filtro 'after' para cerrar la conexión a la base de datos ---
        // Este filtro se ejecuta después de que cada solicitud HTTP ha sido procesada.
        after((req, res) -> {
            try {
                // Cierra la conexión a la base de datos para liberar recursos.
                Base.close();
            } catch (Exception e) {
                System.err.println("Error al cerrar conexión con ActiveJDBC: " + e.getMessage());
            }
        });

        // --- Rutas GET para renderizar formularios y páginas HTML ---

        // GET: Muestra el formulario de creación de cuenta.
        // Soporta la visualización de mensajes de éxito o error pasados como query
        // parameters.
        get("/user/create", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Crea un mapa para pasar datos a la plantilla.

            // Obtener y añadir mensaje de éxito de los query parameters (ej.
            // ?message=Cuenta creada!)
            String successMessage = req.queryParams("message");
            if (successMessage != null && !successMessage.isEmpty()) {
                model.put("successMessage", successMessage);
            }

            // Obtener y añadir mensaje de error de los query parameters (ej. ?error=Campos
            // vacíos)
            String errorMessage = req.queryParams("error");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.put("errorMessage", errorMessage);
            }

            // Renderiza la plantilla 'user_form.mustache' con los datos del modelo.
            return new ModelAndView(model, "user_form.mustache");
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        // Requiere que el usuario esté autenticado.
        // GET: Ruta para mostrar el dashboard (panel de control) del usuario.
        // Requiere que el usuario esté autenticado.
        get("/dashboard", (req, res) -> {
            Map<String, Object> model = new HashMap<>(); // Modelo para la plantilla del dashboard.

            String currentUsername = req.session().attribute("currentUserUsername");
            Object userId = req.session().attribute("userId");

            User user = User.findFirst("id_user = ?", userId);
            // El filtro ya garantiza que user no es nulo y está autenticado.

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
            // Esto elimina todos los atributos guardados en la sesión y la marca como
            // inválida.
            // La cookie JSESSIONID en el navegador también será gestionada para
            // invalidarse.
            req.session().invalidate();

            System.out.println("DEBUG: Sesión cerrada. Redirigiendo a /login.");

            // Redirige al usuario a la página de login con un mensaje de éxito.
            res.redirect("/");

            return null; // Importante retornar null después de una redirección.
        });

        // GET: Muestra el formulario de inicio de sesión (login).
        // Nota: Esta ruta debería ser capaz de leer también mensajes de error/éxito de
        // los query params
        // si se la usa como destino de redirecciones. (Tu código de /user/create ya lo
        // hace, aplicar similar).
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
        // En una aplicación real, probablemente querrías unificar con '/user/create'
        // para evitar duplicidad.
        get("/user/new", (req, res) -> {
            return new ModelAndView(new HashMap<>(), "user_form.mustache"); // No pasa un modelo específico, solo el
                                                                            // formulario.
        }, new MustacheTemplateEngine()); // Especifica el motor de plantillas para esta ruta.

        // El objetivo del POST es recibir los datos que el usuario ingresó en el
        // formulario
        // (el que devolvió el GET) y guardarlos en la base de datos.
        // POST: Maneja el envío del formulario de creación de nueva cuenta.
        // Req contiene toda la información enviada por el cliente en el formulario.
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
            Persona other = Persona.findFirst("dni = ?", dni);
            if (userExistente != null || other != null) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formularo de creación con un mensaje de error.
                res.redirect("/user/create?error=Usuario ya registrado");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }

            // si no se respeta el tipo de usuario tenemos error.
            if (!type.equals("ADMINISTRADOR") && !type.equals("ALUMNO") && !type.equals("DOCENTE")) {
                res.status(400); // Código de estado HTTP 400 (Bad Request).
                // Redirige al formulario de creación con un mensaje de error.
                res.redirect("/user/create?error=Tipo de usuario inválido.");
                return ""; // Retorna una cadena vacía ya que la respuesta ya fue redirigida.
            }
            Persona persona = Persona.findFirst("dni = ?", dni);
            try {
                // Busca si existe Persona
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
                // Si ocurre cualquier error durante la operación de DB (ej. nombre de usuario
                // duplicado),
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

            // Validaciones básicas: campos de usuario y contraseña no pueden ser nulos o
            // vacíos.
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

            // Compara la contraseña en texto plano ingresada con la contraseña hasheada
            // almacenada.
            // BCrypt.checkpw hashea la plainTextPassword con el salt de
            // storedHashedPassword y compara.
            if (BCrypt.checkpw(plainTextPassword, storedHashedPassword)) {
                // Autenticación exitosa.
                res.status(200);

                // --- Gestión de Sesión ---
                String userIdVal = ac.getString("id_user");
                req.session(true).attribute("userId", userIdVal); // Guarda el ID de la cuenta en la sesión (útil).
                req.session().attribute("currentUserUsername", username); // Guarda el nombre de usuario en la sesión.
                req.session().attribute("loggedIn", true); // Establece una bandera para indicar que el usuario está
                                                           // logueado.

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
                // Se recomienda usar `BCrypt.hashpw(password, BCrypt.gensalt())` como en la
                // ruta '/user/new').
                newUser.set("name", name); // Asigna el nombre al campo 'name'.
                newUser.set("password", password); // Asigna la contraseña al campo 'password'.
                newUser.saveIt(); // Guarda el nuevo usuario en la tabla 'users'.

                res.status(201); // Created.
                // Devuelve una respuesta JSON con el mensaje y el ID del nuevo usuario.
                return objectMapper.writeValueAsString(
                        Map.of("message", "Usuario '" + name + "' registrado con éxito.", "id", newUser.getId()));

            } catch (Exception e) {
                // Si ocurre cualquier error durante la operación de DB, se captura aquí.
                System.err.println("Error al registrar usuario: " + e.getMessage());
                e.printStackTrace(); // Imprime el stack trace para depuración.
                res.status(500); // Internal Server Error.
                return objectMapper
                        .writeValueAsString(Map.of("error", "Error interno al registrar usuario: " + e.getMessage()));
            }
        });
        // Métodos de HTTP protocolo para transferir info entre servidor y cliente.

        // GET: Muestra el formulario con los campos para completar
        get("/datos", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            Object sessionUserId = req.session().attribute("userId");
            User user = User.findFirst("id_user = ?", sessionUserId);

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
                        if (partes.length == 3) {
                            model.put("anio_val", partes[0]);
                            model.put("mes_val", partes[1]);
                            model.put("dia_val", partes[2]);
                        }
                    }

                    if ("DOCENTE".equalsIgnoreCase(type)) {
                        Docente d = Docente.findFirst("dni = ?", p.get("dni"));
                        if (d != null) {
                            model.put("titulo", d.get("titulo"));
                            model.put("rol", d.get("rol"));
                            List<Object> selectedIds = Base.firstColumn(
                                    "SELECT id_facultad FROM docente_facultad WHERE id_docente = ?", d.getId());
                            model.put("selectedFacultades", selectedIds);
                        }
                    }
                }
            }

            List<Map> facultades = Base.findAll("SELECT id_facultad, nombre FROM facultad");
            if (model.containsKey("selectedFacultades")) {
                List<Object> selectedIds = (List<Object>) model.get("selectedFacultades");
                for (Map fac : facultades) {
                    Object idFac = fac.get("id_facultad");
                    boolean match = false;
                    for (Object selId : selectedIds) {
                        if (String.valueOf(selId).equals(String.valueOf(idFac))) {
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        fac.put("seleccionada", true);
                    }
                }
            }
            model.put("facultades", facultades);
            return new ModelAndView(model, "datos.mustache");
        }, new MustacheTemplateEngine());

        post("/datos", (req, res) -> {
            try {
                // 1. Verificación de la sesión (el filtro ya valida userId != null, pero
                // obtenemos la variable)
                String sessionUserId = req.session().attribute("userId");
                User user = User.findFirst("id_user = ?", sessionUserId);
                String tipo = user.getString("type");

                // 2. Capturar datos del formulario
                String dni = req.queryParams("dni");
                String nombre = req.queryParams("nombre");
                String apellido = req.queryParams("apellido");
                String email = req.queryParams("email");
                String telefono = req.queryParams("telefono");

                // 3. Manejo de la Fecha de Nacimiento
                String dia = req.queryParams("dia");
                String mes = req.queryParams("mes");
                String anio = req.queryParams("anio");
                String fechaNacimientoFull = (dia != null && !dia.isEmpty()) ? anio + "-" + mes + "-" + dia : null;

                // 4. Validación defensiva de DNI positivo
                try {
                    int dniInt = Integer.parseInt(dni);
                    if (dniInt <= 0)
                        throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    res.redirect(
                            "/datos?error=" + URLEncoder.encode("El DNI debe ser un número entero positivo.", "UTF-8"));
                    return null;
                }

                // 5. Actualizar o Crear Persona con validación de DNI ocupado
                Object idPersona = user.get("id_persona");
                Persona p = null;

                if (idPersona != null) {
                    p = Persona.findById(idPersona);
                }

                Persona personaConDni = Persona.findFirst("dni = ?", dni);

                if (p != null) {
                    // Si cambia su DNI anterior, verificamos si el nuevo DNI ya le pertenece a otro
                    // usuario
                    if (!String.valueOf(p.get("dni")).equals(dni)) {
                        if (personaConDni != null) {
                            User userAsociado = User.findFirst("id_persona = ?", personaConDni.getId());
                            if (userAsociado != null
                                    && !String.valueOf(userAsociado.get("id_user")).equals(sessionUserId)) {
                                res.redirect("/datos?error=" + URLEncoder
                                        .encode("El DNI ingresado ya está asociado a otra cuenta.", "UTF-8"));
                                return null;
                            }
                            // Si existe y no tiene usuario, lo reutilizamos
                            p = personaConDni;
                        } else {
                            p.set("dni", dni);
                        }
                    }
                } else {
                    // Si el usuario no tenía Persona asociada aún, chequeamos si el DNI ingresado
                    // ya está ocupado
                    if (personaConDni != null) {
                        User userAsociado = User.findFirst("id_persona = ?", personaConDni.getId());
                        if (userAsociado != null
                                && !String.valueOf(userAsociado.get("id_user")).equals(sessionUserId)) {
                            res.redirect("/datos?error="
                                    + URLEncoder.encode("El DNI ingresado ya está asociado a otra cuenta.", "UTF-8"));
                            return null;
                        }
                        p = personaConDni;
                    } else {
                        p = new Persona();
                        p.set("dni", dni);
                    }
                }

                p.set("nombre", nombre);
                p.set("apellido", apellido);
                p.set("email", email);
                p.set("telefono", telefono);
                p.set("fecha_nacimiento", fechaNacimientoFull);
                p.saveIt();

                // 6. Asegurar vínculo Usuario -> Persona y GUARDAR
                user.set("id_persona", p.getId());
                user.saveIt();

                // 7. Lógica específica por tipo de usuario
                if ("DOCENTE".equalsIgnoreCase(tipo)) {
                    Docente d = Docente.findFirst("dni = ?", dni);
                    if (d == null) {
                        d = new Docente();
                    }
                    d.set("dni", dni);
                    d.set("titulo", req.queryParams("titulo"));
                    d.set("rol", req.queryParams("rol"));
                    d.saveIt();

                    // Guardar relación de facultades múltiples (N a N)
                    Base.exec("DELETE FROM docente_facultad WHERE id_docente = ?", d.getId());
                    String[] selectedFacs = req.queryParamsValues("id_facultad");
                    if (selectedFacs != null) {
                        for (String idFacStr : selectedFacs) {
                            if (idFacStr != null && !idFacStr.isEmpty()) {
                                Base.exec("INSERT INTO docente_facultad (id_docente, id_facultad) VALUES (?, ?)",
                                        d.getId(), Integer.parseInt(idFacStr));
                            }
                        }
                    }
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
            Map<String, Object> model = new HashMap<>();

            model.put("facultades", Base.findAll("SELECT id_facultad, nombre FROM facultad"));
            model.put("carreras",
                    Base.findAll("SELECT nombre_carrera as nombre, cant_anios as anios FROM carrera"));

            String error = req.queryParams("error");
            if (error != null)
                model.put("error", error);

            return new ModelAndView(model, "admin_carrera.mustache");

        }, new MustacheTemplateEngine());

        post("/admin/carrera", (req, res) -> {
            try {
                String nombre = req.queryParams("nombre_carrera");
                String anios = req.queryParams("anios");
                String idFacultad = req.queryParams("id_facultad");

                // Verificamos si la carrera ya existe
                // Usamos Number para evitar el ClassCastException entre Integer y Long
                Number existeCarrera = (Number) Base.firstCell("SELECT count(*) FROM carrera WHERE nombre_carrera = ?",
                        nombre);

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
                            Number existeMateria = (Number) Base
                                    .firstCell("SELECT count(*) FROM materia WHERE codigo = ?", codigo);

                            if (existeMateria.longValue() > 0) {
                                // Si el código ya existe, podemos optar por no insertarla o saltarla
                                // Aquí simplemente la saltamos para no romper todo el proceso, pero podrías
                                // avisar al usuario
                                System.out.println("DEBUG: El codigo de materia " + codigo + " ya existe. Saltando...");
                                continue;
                            }

                            // 4. Guardar la materia
                            Base.exec(
                                    "INSERT INTO materia (codigo, nombre_materia, anio_pertenece, cant_horas, periodo) VALUES (?, ?, ?, ?, ?)",
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

        // GET: Muestra el formulario para asignar docentes a materias
        get("/admin/asignar-docente", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            // Cargar facultades
            List<Map> facultades = Base.findAll("SELECT id_facultad, nombre FROM facultad");
            String idFacultadStr = req.queryParams("id_facultad");

            if (idFacultadStr != null && !idFacultadStr.isEmpty()) {
                int idFacultad = Integer.parseInt(idFacultadStr);

                // Marcar facultad seleccionada
                for (Map fac : facultades) {
                    if (String.valueOf(fac.get("id_facultad")).equals(idFacultadStr)) {
                        fac.put("selected", true);
                    }
                }

                // Cargar docentes de esta facultad (N a N a través de docente_facultad)
                List<Map> docentes = Base.findAll(
                        "SELECT d.id, p.apellido, p.nombre FROM docentes d " +
                                "JOIN persona p ON d.dni = p.dni " +
                                "JOIN docente_facultad df ON d.id = df.id_docente " +
                                "WHERE df.id_facultad = ?",
                        idFacultad);

                // Cargar materias de esta facultad (materias que pertenecen a carreras
                // radicadas en la facultad)
                List<Map> materias = Base.findAll(
                        "SELECT m.id_materia, m.nombre_materia, m.codigo, c.nombre_carrera FROM materia m " +
                                "JOIN plan_estudio pe ON m.id_materia = pe.id_materia " +
                                "JOIN carrera c ON pe.id_carrera = c.id_carrera " +
                                "WHERE c.id_facultad = ?",
                        idFacultad);

                model.put("facultadSeleccionada", true);
                model.put("facultadId", idFacultad);
                model.put("docentes", docentes);
                model.put("materias", materias);
            }

            // Cargar asignaciones actuales
            List<Map> asignaciones = Base.findAll(
                    "SELECT dm.id_DocMat, (p.apellido || ', ' || p.nombre) as docente, m.nombre_materia as materia, c.nombre_carrera as carrera "
                            +
                            "FROM docente_materia dm " +
                            "JOIN docentes d ON dm.id_docente = d.id " +
                            "JOIN persona p ON d.dni = p.dni " +
                            "JOIN materia m ON dm.id_materia = m.id_materia " +
                            "JOIN plan_estudio pe ON m.id_materia = pe.id_materia " +
                            "JOIN carrera c ON pe.id_carrera = c.id_carrera");

            model.put("facultades", facultades);
            model.put("asignaciones", asignaciones);

            String error = req.queryParams("error");
            if (error != null)
                model.put("errorMessage", error);
            String success = req.queryParams("success");
            if (success != null)
                model.put("successMessage", success);

            return new ModelAndView(model, "admin_asignar.mustache");
        }, new MustacheTemplateEngine());

        // POST: Procesa la asignación de un docente a una materia
        post("/admin/asignar-docente", (req, res) -> {
            String idDocente = req.queryParams("id_docente");
            String idMateria = req.queryParams("id_materia");
            String idFacultad = req.queryParams("id_facultad");

            if (idDocente == null || idDocente.isEmpty() || idMateria == null || idMateria.isEmpty()) {
                res.redirect("/admin/asignar-docente?id_facultad=" + idFacultad + "&error="
                        + URLEncoder.encode("Campos obligatorios faltantes", "UTF-8"));
                return null;
            }

            try {
                // Verificar duplicados
                Number existe = (Number) Base.firstCell(
                        "SELECT COUNT(*) FROM docente_materia WHERE id_docente = ? AND id_materia = ?",
                        Integer.parseInt(idDocente), Integer.parseInt(idMateria));

                if (existe.longValue() > 0) {
                    res.redirect("/admin/asignar-docente?id_facultad=" + idFacultad + "&error="
                            + URLEncoder.encode("El docente ya está asignado a esa materia.", "UTF-8"));
                    return null;
                }

                Base.exec("INSERT INTO docente_materia (id_docente, id_materia) VALUES (?, ?)",
                        Integer.parseInt(idDocente), Integer.parseInt(idMateria));

                res.redirect("/admin/asignar-docente?id_facultad=" + idFacultad + "&success="
                        + URLEncoder.encode("Asignación realizada con éxito", "UTF-8"));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/admin/asignar-docente?id_facultad=" + idFacultad + "&error="
                        + URLEncoder.encode("Error al realizar asignación: " + e.getMessage(), "UTF-8"));
                return null;
            }
        });

        // POST: Elimina una asignación existente
        post("/admin/asignar-docente/delete", (req, res) -> {
            String idDocMat = req.queryParams("id_doc_mat");
            if (idDocMat == null || idDocMat.isEmpty()) {
                res.redirect(
                        "/admin/asignar-docente?error=" + URLEncoder.encode("ID de asignación no válido", "UTF-8"));
                return null;
            }
            try {
                Base.exec("DELETE FROM docente_materia WHERE id_DocMat = ?", Integer.parseInt(idDocMat));
                res.redirect("/admin/asignar-docente?success="
                        + URLEncoder.encode("Asignación eliminada con éxito", "UTF-8"));
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/admin/asignar-docente?error="
                        + URLEncoder.encode("Error al eliminar asignación: " + e.getMessage(), "UTF-8"));
                return null;
            }
        });

        // GET: Muestra el perfil del alumno logueado
        get("/profile", (req, res) -> {
            Map<String, Object> model = new HashMap<>();

            String sessionUserId = req.session().attribute("userId");
            User user = User.findFirst("id_user = ?", sessionUserId);

            Object idPersona = user.get("id_persona");
            if (idPersona == null) {
                res.redirect("/datos?error=" + URLEncoder.encode("Primero completá tus datos personales.", "UTF-8"));
                return null;
            }

            Persona p = Persona.findById(idPersona);
            if (p == null) {
                res.redirect("/datos?error=" + URLEncoder.encode("No se encontraron datos."));
                return null;
            }

            // Datos personales
            model.put("nombre", p.get("nombre"));
            model.put("apellido", p.get("apellido"));
            model.put("dni", p.get("dni"));
            model.put("email", p.get("email"));
            model.put("telefono", p.get("telefono"));
            model.put("username", user.get("name"));

            // Fecha de nacimiento formateada
            if (p.get("fecha_nacimiento") != null) {
                String fecha = p.getString("fecha_nacimiento"); // YYYY-MM-DD
                String[] partes = fecha.split("-");
                if (partes.length == 3) {
                    model.put("fecha_nacimiento", partes[2] + "/" + partes[1] + "/" + partes[0]);
                }
            }

            String tipo = user.getString("type");
            model.put("isAlumno", "ALUMNO".equalsIgnoreCase(tipo));
            model.put("isDocente", "DOCENTE".equalsIgnoreCase(tipo));

            if ("ALUMNO".equalsIgnoreCase(tipo)) {
                Alumno al = Alumno.findFirst("dni = ?", p.get("dni"));
                if (al != null) {
                    model.put("progreso", al.get("progreso"));
                }
            } else if ("DOCENTE".equalsIgnoreCase(tipo)) {
                Docente d = Docente.findFirst("dni = ?", p.get("dni"));
                if (d != null) {
                    model.put("titulo", d.get("titulo"));
                    model.put("rol", d.get("rol"));
                }
            }
            return new ModelAndView(model, "profile.mustache");
        }, new MustacheTemplateEngine());

        // GET: Panel de inscripciones
        get("/inscripcion/carrera", (req, res) -> {

            Map<String, Object> model = new HashMap<>();

            Object sessionUserId = req.session().attribute("userId");

            if (sessionUserId == null) {
                res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión", StandardCharsets.UTF_8));
                return null;
            }

            User user = User.findFirst("id_user = ?", sessionUserId);

            if (user == null || !"ALUMNO".equalsIgnoreCase(user.getString("type"))) {
                res.redirect("/dashboard?error="
                        + URLEncoder.encode("Acceso exclusivo para alumnos", StandardCharsets.UTF_8));
                return null;
            }

            Persona persona = Persona.findById(user.get("id_persona"));

            if (persona == null) {
                res.redirect("/dashboard?error="
                        + URLEncoder.encode("No se encontró la persona asociada.", StandardCharsets.UTF_8));
                return null;
            }

            Alumno alumno = Alumno.findFirst("dni = ?", persona.get("dni"));

            if (alumno == null) {
                res.redirect("/datos?error="
                        + URLEncoder.encode("Debes completar tus datos personales primero.", StandardCharsets.UTF_8));
                return null;
            }

            Object idAlumnoReal = alumno.getId();

            List<Map> carrerasDisponibles = Base.findAll(
                    "SELECT id_carrera, nombre_carrera " +
                            "FROM carrera " +
                            "WHERE id_carrera NOT IN (" +
                            "SELECT id_carrera FROM alumno_carrera WHERE id_alumno = ?" +
                            ")",
                    idAlumnoReal);

            List<Map> misCarreras = Base.findAll(
                    "SELECT c.id_carrera, c.nombre_carrera " +
                            "FROM carrera c " +
                            "JOIN alumno_carrera ac ON c.id_carrera = ac.id_carrera " +
                            "WHERE ac.id_alumno = ?",
                    idAlumnoReal);

            String carreraSeleccionada = req.queryParams("id_carrera");
            String anioBusqueda = req.queryParams("anio_pertenece");
            String cuatriBusqueda = req.queryParams("periodo");

            // Marcar la carrera seleccionada dentro de la lista `misCarreras` para que
            // la plantilla pueda renderizarla con el atributo `selected`.
            if (misCarreras != null && carreraSeleccionada != null && !carreraSeleccionada.isEmpty()) {
                for (Map m : misCarreras) {
                    Object idCarr = m.get("id_carrera");
                    if (idCarr != null && String.valueOf(idCarr).equals(carreraSeleccionada)) {
                        m.put("selected", true);
                    }
                }
            }

            List<Map> materiasDisponibles = new ArrayList<>();
            List<Map> materiasInscriptas = new ArrayList<>();

            if (carreraSeleccionada != null && !carreraSeleccionada.isEmpty()) {

                String sql = "SELECT DISTINCT " +
                        "m.id_materia, m.nombre_materia, m.anio_pertenece, m.periodo, c.nombre_carrera " +
                        "FROM materia m " +
                        "JOIN plan_estudio pe ON m.id_materia = pe.id_materia " +
                        "JOIN carrera c ON pe.id_carrera = c.id_carrera " +
                        "WHERE pe.id_carrera = ? " +
                        "AND m.id_materia NOT IN (" +
                        "SELECT id_materia FROM inscripcion WHERE id_alumno = ?" +
                        ") ";

                List<Object> params = new ArrayList<>();

                params.add(carreraSeleccionada);
                params.add(idAlumnoReal);

                if (anioBusqueda != null && !anioBusqueda.isEmpty()) {
                    sql += "AND m.anio_pertenece = ? ";
                    params.add(anioBusqueda);
                }

                if (cuatriBusqueda != null && !cuatriBusqueda.isEmpty()) {
                    sql += "AND (m.periodo = ? OR m.periodo = 'ANUAL') ";
                    params.add(cuatriBusqueda);
                }

                sql += "ORDER BY m.anio_pertenece, m.nombre_materia";

                materiasDisponibles = Base.findAll(sql, params.toArray());

                String sqlInscriptas = "SELECT DISTINCT " +
                        "m.id_materia, m.nombre_materia, m.anio_pertenece, m.periodo, c.nombre_carrera " +
                        "FROM materia m " +
                        "JOIN plan_estudio pe ON m.id_materia = pe.id_materia " +
                        "JOIN carrera c ON pe.id_carrera = c.id_carrera " +
                        "JOIN inscripcion i ON i.id_materia = m.id_materia " +
                        "WHERE pe.id_carrera = ? " +
                        "AND i.id_alumno = ? ";

                List<Object> paramsInscriptas = new ArrayList<>();
                paramsInscriptas.add(carreraSeleccionada);
                paramsInscriptas.add(idAlumnoReal);

                if (anioBusqueda != null && !anioBusqueda.isEmpty()) {
                    sqlInscriptas += "AND m.anio_pertenece = ? ";
                    paramsInscriptas.add(anioBusqueda);
                }

                if (cuatriBusqueda != null && !cuatriBusqueda.isEmpty()) {
                    sqlInscriptas += "AND (m.periodo = ? OR m.periodo = 'ANUAL') ";
                    paramsInscriptas.add(cuatriBusqueda);
                }

                sqlInscriptas += "ORDER BY m.anio_pertenece, m.nombre_materia";
                materiasInscriptas = Base.findAll(sqlInscriptas, paramsInscriptas.toArray());
            }

            model.put("carrerasDisponibles", carrerasDisponibles);
            model.put("misCarreras", misCarreras);
            model.put("materiasDisponibles", materiasDisponibles);
            model.put("materiasInscriptas", materiasInscriptas);
            model.put("hayMaterias", !materiasDisponibles.isEmpty());
            model.put("hayMateriasInscriptas", !materiasInscriptas.isEmpty());

            model.put("carreraSeleccionada", carreraSeleccionada);
            model.put("anioBusqueda", anioBusqueda);
            model.put("cuatriBusqueda", cuatriBusqueda);

            // Flags para que Mustache pueda renderizar las opciones "selected"
            model.put("anio1", "1".equals(anioBusqueda));
            model.put("anio2", "2".equals(anioBusqueda));
            model.put("anio3", "3".equals(anioBusqueda));

            model.put("periodo1C", "1C".equals(cuatriBusqueda));
            model.put("periodo2C", "2C".equals(cuatriBusqueda));

            if (carreraSeleccionada != null && !carreraSeleccionada.isEmpty() && materiasDisponibles.isEmpty()) {
                model.put("mensajeNoHayMaterias",
                        "No hay materias disponibles para los filtros seleccionados. Es posible que ya estés inscripto en todas las materias de ese año/cuatrimestre.");
            }

            if (req.queryParams("error") != null) {
                model.put("error", req.queryParams("error"));
            }

            if (req.queryParams("success") != null) {
                model.put("success", req.queryParams("success"));
            }

            return new ModelAndView(model, "alumno_inscripciones.mustache");

        }, new MustacheTemplateEngine());

        // POST: Inscripción a carrera
        post("/alumno/inscribir-carrera", (req, res) -> {
            try {
                Object sessionUserId = req.session().attribute("userId");
                if (sessionUserId == null) {
                    res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión.", StandardCharsets.UTF_8));
                    return null;
                }

                String idCarrera = req.queryParams("id_carrera");
                if (idCarrera == null || idCarrera.isEmpty()) {
                    res.redirect("/inscripcion/carrera?error="
                            + URLEncoder.encode("Debes seleccionar una carrera.", StandardCharsets.UTF_8));
                    return null;
                }

                User user = User.findFirst("id_user = ?", sessionUserId);
                Persona persona = Persona.findById(user.get("id_persona"));
                Alumno alumno = Alumno.findFirst("dni = ?", persona.get("dni"));

                Object idAlumnoReal = alumno.getId();
                String fechaActual = java.time.LocalDate.now().toString();
                Base.exec(
                        "INSERT INTO alumno_carrera (id_alumno, id_carrera, fecha_inscripcion) VALUES (?, ?, ?)",
                        idAlumnoReal,
                        idCarrera,
                        fechaActual);
                res.redirect("/inscripcion/carrera?success="
                        + URLEncoder.encode("Inscripción a carrera realizada correctamente.", StandardCharsets.UTF_8));
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/inscripcion/carrera?error="
                        + URLEncoder.encode("Error al inscribirse a la carrera.", StandardCharsets.UTF_8));
                return null;
            }

        });

        // POST: Inscripción a materia
        post("/alumno/inscribir-materias", (req, res) -> {
            try {
                Object sessionUserId = req.session().attribute("userId");
                if (sessionUserId == null) {
                    res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión.", StandardCharsets.UTF_8));
                    return null;
                }

                String[] materias = req.queryParamsValues("materias");
                String carrera = req.queryParams("id_carrera");
                String anio = req.queryParams("anio_pertenece");
                String periodo = req.queryParams("periodo");

                if (materias == null || materias.length == 0) {
                    res.redirect("/inscripcion/carrera?id_carrera=" + carrera +
                            "&anio_pertenece=" + anio +
                            "&periodo=" + periodo +
                            "&error="
                            + URLEncoder.encode("Debes seleccionar al menos una materia.", StandardCharsets.UTF_8));
                    return null;
                }
                User user = User.findFirst("id_user = ?", sessionUserId);
                Persona persona = Persona.findById(user.get("id_persona"));
                Alumno alumno = Alumno.findFirst("dni = ?", persona.get("dni"));

                Object idAlumnoReal = alumno.getId();

                for (String idMateria : materias) {
                    List<Map> existe = Base.findAll(
                            "SELECT * FROM inscripcion WHERE id_alumno = ? AND id_materia = ?",
                            idAlumnoReal,
                            idMateria);

                    if (existe.isEmpty()) {
                        Base.exec(
                                "INSERT INTO inscripcion (id_alumno, id_materia) VALUES (?, ?)",
                                idAlumnoReal,
                                idMateria);
                    }
                }
                res.redirect("/inscripcion/carrera?id_carrera=" + carrera +
                        "&anio_pertenece=" + anio +
                        "&periodo=" + periodo +
                        "&success=" + URLEncoder.encode("Materias inscriptas correctamente.", StandardCharsets.UTF_8));
                return null;

            } catch (Exception e) {
                e.printStackTrace();
                res.redirect("/inscripcion/carrera?error=" +
                        URLEncoder.encode("Error al procesar la inscripción.", StandardCharsets.UTF_8));
                return null;
            }
        });

        // GET: Materias asignadas al docente que ingresó
    get("/docente/materias", (req, res) -> {
        try {
        Object sessionUserId = req.session().attribute("userId");
        if (sessionUserId == null) {
            res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión.", StandardCharsets.UTF_8));
            return null;
        }

        User user = User.findFirst("id_user = ?", sessionUserId);
        Persona persona = Persona.findById(user.get("id_persona"));
        Docente docente = Docente.findFirst("dni = ?", persona.get("dni"));

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.get("name"));
        model.put("isDocente", true);

        if (docente == null) {
            model.put("error", "No se encontró el perfil de docente.");
            model.put("hayMaterias", false);
            return new ModelAndView(model, "docente_materias.mustache");
        }

        List<Map> materias = Base.findAll(
            "SELECT m.id_materia, m.nombre_materia, m.codigo, m.anio_pertenece, m.periodo, m.cant_horas, " +
            "c.nombre_carrera " +
            "FROM materia m " +
            "JOIN docente_materia dm ON m.id_materia = dm.id_materia " +
            "JOIN plan_estudio pe ON m.id_materia = pe.id_materia " +
            "JOIN carrera c ON pe.id_carrera = c.id_carrera " +
            "WHERE dm.id_docente = ? " +
            "ORDER BY m.anio_pertenece, m.nombre_materia",
            docente.getId());

        model.put("materias", materias);
        model.put("hayMaterias", !materias.isEmpty());

        if (req.queryParams("error") != null)
            model.put("error", req.queryParams("error"));

        return new ModelAndView(model, "docente_materias.mustache");

        } catch (Exception e) {
            e.printStackTrace();
            res.redirect("/dashboard?error=" + URLEncoder.encode("Error al obtener materias.", StandardCharsets.UTF_8));
            return null;
        }
    }, new MustacheTemplateEngine());
    
// GET: Cargar notas
get("/docente/notas", (req, res) -> {
    try {
        Object sessionUserId = req.session().attribute("userId");
        if (sessionUserId == null) {
            res.redirect("/?error=" + URLEncoder.encode("Debes iniciar sesión.", StandardCharsets.UTF_8));
            return null;
        }

        User user = User.findFirst("id_user = ?", sessionUserId);
        Persona persona = Persona.findById(user.get("id_persona"));
        Docente docente = Docente.findFirst("dni = ?", persona.get("dni"));

        Map<String, Object> model = new HashMap<>();
        model.put("userName", user.get("name"));
        model.put("isDocente", true);

        if (docente == null) {
            model.put("error", "No se encontró el perfil de docente.");
            return new ModelAndView(model, "docente_notas.mustache");
        }

        String idMateriaParam = req.queryParams("id_materia");

        // Materias asignadas al docente
        List<Map> materias = Base.findAll(
            "SELECT m.id_materia, m.nombre_materia " +
            "FROM materia m " +
            "JOIN docente_materia dm ON m.id_materia = dm.id_materia " +
            "WHERE dm.id_docente = ? " +
            "ORDER BY m.nombre_materia",
            docente.getId());

        for (Map m : materias) {
            if (idMateriaParam != null && String.valueOf(m.get("id_materia")).equals(idMateriaParam)) {
                m.put("selected", true);
            }
        }

        model.put("materias", materias);
        model.put("hayMaterias", !materias.isEmpty());
        model.put("materiaSeleccionada", idMateriaParam);

        // Si hay materia seleccionada, traer alumnos inscriptos con sus notas
        if (idMateriaParam != null && !idMateriaParam.isEmpty()) {
            List<Map> alumnos = Base.findAll(
                "SELECT i.id_inscripcion, p.apellido, p.nombre, p.dni, i.estado " +
                "FROM inscripcion i " +
                "JOIN alumnos a ON i.id_alumno = a.id " +
                "JOIN persona p ON a.dni = p.dni " +
                "WHERE i.id_materia = ? " +
                "ORDER BY p.apellido, p.nombre",
                idMateriaParam);

            for (Map alumno : alumnos) {
                List<Map> notas = Base.findAll(
                    "SELECT id_notas, valor, tipo_nota FROM notas WHERE id_inscripcion = ? ORDER BY tipo_nota",
                    alumno.get("id_inscripcion"));
                alumno.put("notas", notas);
                alumno.put("hayNotas", !notas.isEmpty());
            }

            model.put("alumnos", alumnos);
            model.put("hayAlumnos", !alumnos.isEmpty());
        }

        if (req.queryParams("error") != null)
            model.put("error", req.queryParams("error"));
        if (req.queryParams("success") != null)
            model.put("success", req.queryParams("success"));

        return new ModelAndView(model, "docente_notas.mustache");

    } catch (Exception e) {
        e.printStackTrace();
        res.redirect("/dashboard?error=" + URLEncoder.encode("Error al cargar notas.", StandardCharsets.UTF_8));
        return null;
    }
}, new MustacheTemplateEngine());

// POST: Guardar nota
post("/docente/notas/cargar", (req, res) -> {
    String idInscripcion = req.queryParams("id_inscripcion");
    String valor         = req.queryParams("valor");
    String tipoNota      = req.queryParams("tipo_nota");
    String idMateria     = req.queryParams("id_materia");
    String redirectBase  = "/docente/notas?id_materia=" + (idMateria != null ? idMateria : "");

    try {
        if (idInscripcion == null || valor == null || tipoNota == null ||
            idInscripcion.isEmpty() || valor.isEmpty() || tipoNota.isEmpty()) {
            res.redirect(redirectBase + "&error=" +
                URLEncoder.encode("Todos los campos son obligatorios.", StandardCharsets.UTF_8));
            return null;
        }

        int valorInt = Integer.parseInt(valor);
        if (valorInt < 0 || valorInt > 10) {
            res.redirect(redirectBase + "&error=" +
                URLEncoder.encode("La nota debe estar entre 0 y 10.", StandardCharsets.UTF_8));
            return null;
        }

        // Si ya existe nota del mismo tipo para esa inscripción, actualizar
        List<Map> existe = Base.findAll(
            "SELECT id_notas FROM notas WHERE id_inscripcion = ? AND tipo_nota = ?",
            idInscripcion, tipoNota);

        if (!existe.isEmpty()) {
            Base.exec("UPDATE notas SET valor = ? WHERE id_inscripcion = ? AND tipo_nota = ?",
                valorInt, idInscripcion, tipoNota);
        } else {
            Base.exec("INSERT INTO notas (id_inscripcion, valor, tipo_nota) VALUES (?, ?, ?)",
                idInscripcion, valorInt, tipoNota);
        }

        res.redirect(redirectBase + "&success=" +
            URLEncoder.encode("Nota guardada correctamente.", StandardCharsets.UTF_8));
        return null;

    } catch (NumberFormatException e) {
        res.redirect(redirectBase + "&error=" +
            URLEncoder.encode("La nota debe ser un número.", StandardCharsets.UTF_8));
        return null;
    } catch (Exception e) {
        e.printStackTrace();
        res.redirect(redirectBase + "&error=" +
            URLEncoder.encode("Error al guardar la nota.", StandardCharsets.UTF_8));
        return null;
    }
});

// GET: Muestra las materias y notas del alumno logueado
get("/alumno/materias", (req, res) -> {
    Map<String, Object> model = new HashMap<>();

    String sessionUserId = req.session().attribute("userId");
    User user = User.findFirst("id_user = ?", sessionUserId);

    Object idPersona = user.get("id_persona");
    if (idPersona == null) {
        res.redirect("/datos?error=" + URLEncoder.encode("Primero completá tus datos personales.", "UTF-8"));
        return null;
    }

    Persona p = Persona.findById(idPersona);
    Alumno al = Alumno.findFirst("dni = ?", p.get("dni"));

    if (al == null) {
        res.redirect("/datos?error=" + URLEncoder.encode("No se encontró el alumno.", "UTF-8"));
        return null;
    }

    // Materias inscriptas con su estado
    List<Map> materias = Base.findAll(
        "SELECT m.nombre_materia, m.codigo, m.anio_pertenece, m.periodo, " +
        "i.estado, i.id_inscripcion " +
        "FROM inscripcion i " +
        "JOIN materia m ON i.id_materia = m.id_materia " +
        "WHERE i.id_alumno = ? " +
        "ORDER BY m.anio_pertenece, m.nombre_materia", al.getId());

    // Para cada materia, cargar sus notas
    for (Map materia : materias) {
        List<Map> notas = Base.findAll(
            "SELECT valor, tipo_nota FROM notas WHERE id_inscripcion = ?",
            materia.get("id_inscripcion"));
        materia.put("notas", notas);
        materia.put("tieneNotas", !notas.isEmpty());
    }

    model.put("username", user.get("name"));
    model.put("materias", materias);
    model.put("tieneMaterias", !materias.isEmpty());

    return new ModelAndView(model, "alumno_materias.mustache");
}, new MustacheTemplateEngine());












} // Fin del método main




} // Fin de la clase App