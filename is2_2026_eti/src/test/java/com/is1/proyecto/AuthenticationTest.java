package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;
import org.mindrot.jbcrypt.BCrypt;

public class AuthenticationTest {

    @BeforeEach
    void setUp() {
        Base.open("org.sqlite.JDBC", "jdbc:sqlite:./db/dev.db", "", "");
        Base.openTransaction();
    }

    @AfterEach
    void tearDown() {
        Base.rollbackTransaction();
        Base.close();
    }

    @Test
    void testSuccessfulLogin() {
        // Crear Persona
        Persona persona = new Persona();
        persona.set("dni", 12345690);
        persona.set("nombre", "John");
        persona.set("apellido", "Doe");
        persona.set("email", "john@test.com");
        persona.insert();

        // Crear usuario con contraseña hasheada
        String password = "myPassword123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.set("name", "john_doe");
        user.set("password", hashedPassword);
        user.set("type", "ALUMNO");
        user.set("id_persona", persona.getId());
        user.insert();

        // Simular login: buscar usuario y verificar contraseña
        User foundUser = User.findFirst("name = ?", "john_doe");
        assertNotNull(foundUser, "Usuario debería ser encontrado");
        assertTrue(BCrypt.checkpw(password, foundUser.getString("password")),
                "Contraseña debería coincidir");
    }

    @Test
    void testLoginWithIncorrectPassword() {
        // Crear Persona
        Persona persona = new Persona();
        persona.set("dni", 12345691);
        persona.set("nombre", "Alice");
        persona.set("apellido", "Smith");
        persona.set("email", "alice@test.com");
        persona.insert();

        String correctPassword = "correctPass123";
        String wrongPassword = "wrongPass456";
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt());

        User user = new User();
        user.set("name", "alice");
        user.set("password", hashedPassword);
        user.set("type", "DOCENTE");
        user.set("id_persona", persona.getId());
        user.insert();

        // Buscar usuario
        User foundUser = User.findFirst("name = ?", "alice");
        assertNotNull(foundUser);
        assertFalse(BCrypt.checkpw(wrongPassword, foundUser.getString("password")),
                "Contraseña incorrecta no debería coincidir");
    }

    @Test
    void testLoginWithNonexistentUser() {
        User found = User.findFirst("name = ?", "nonexistent_user");
        assertNull(found, "Usuario inexistente no debería ser encontrado");
    }

    @Test
    void testMultipleLoginAttempts() {
        // Crear Persona
        Persona persona = new Persona();
        persona.set("dni", 12345692);
        persona.set("nombre", "Bob");
        persona.set("apellido", "Johnson");
        persona.set("email", "bob@test.com");
        persona.insert();

        String password = "testPass";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.set("name", "bob");
        user.set("password", hashedPassword);
        user.set("type", "ALUMNO");
        user.set("id_persona", persona.getId());
        user.insert();

        // Múltiples intentos de login exitosos
        for (int i = 0; i < 3; i++) {
            User foundUser = User.findFirst("name = ?", "bob");
            assertTrue(BCrypt.checkpw(password, foundUser.getString("password")));
        }
    }

    @Test
    void testLoginWithEmptyUsername() {
        User found = User.findFirst("name = ?", "");
        // No debería encontrar usuario con nombre vacío
        if (found != null) {
            assertNotEquals("", found.getString("name"));
        }
    }

    @Test
    void testUserRoleAfterLogin() {
        // Crear Personas
        Persona p1 = new Persona();
        p1.set("dni", 12345693);
        p1.set("nombre", "Alumno");
        p1.set("apellido", "Uno");
        p1.set("email", "alumno1@test.com");
        p1.insert();

        Persona p2 = new Persona();
        p2.set("dni", 12345694);
        p2.set("nombre", "Docente");
        p2.set("apellido", "Uno");
        p2.set("email", "docente1@test.com");
        p2.insert();

        Persona p3 = new Persona();
        p3.set("dni", 12345695);
        p3.set("nombre", "Admin");
        p3.set("apellido", "Uno");
        p3.set("email", "admin1@test.com");
        p3.insert();

        User alumno = new User();
        alumno.set("name", "alumno1");
        alumno.set("password", "pass");
        alumno.set("type", "ALUMNO");
        alumno.set("id_persona", p1.getId());
        alumno.insert();

        User docente = new User();
        docente.set("name", "docente1");
        docente.set("password", "pass");
        docente.set("type", "DOCENTE");
        docente.set("id_persona", p2.getId());
        docente.insert();

        User admin = new User();
        admin.set("name", "admin1");
        admin.set("password", "pass");
        admin.set("type", "ADMINISTRADOR");
        admin.set("id_persona", p3.getId());
        admin.insert();

        User foundAlumno = User.findFirst("name = ?", "alumno1");
        User foundDocente = User.findFirst("name = ?", "docente1");
        User foundAdmin = User.findFirst("name = ?", "admin1");

        assertEquals("ALUMNO", foundAlumno.getString("type"));
        assertEquals("DOCENTE", foundDocente.getString("type"));
        assertEquals("ADMINISTRADOR", foundAdmin.getString("type"));
    }
}
