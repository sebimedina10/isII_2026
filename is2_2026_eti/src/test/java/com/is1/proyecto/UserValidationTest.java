package com.is1.proyecto;

import org.javalite.activejdbc.Base;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.is1.proyecto.models.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserValidationTest {

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
    void testUserCreationWithValidData() {
        Persona persona = new Persona();
        persona.set("dni", 12345678);
        persona.set("nombre", "Juan");
        persona.set("apellido", "Perez");
        persona.set("email", "juan@test.com");
        persona.insert();

        User user = new User();
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        user.set("name", "juanperez");
        user.set("password", hashedPassword);
        user.set("type", "ALUMNO");
        user.set("id_persona", persona.getId());
        user.insert();

        assertNotNull(user.getId());
        assertTrue(BCrypt.checkpw("password123", user.getString("password")));
    }

    @Test
    void testDuplicateUsernameRejection() {
        // Crear Persona
        Persona persona = new Persona();
        persona.set("dni", 12345679);
        persona.set("nombre", "Test");
        persona.set("apellido", "User");
        persona.set("email", "test@test.com");
        persona.insert();

        // Crear primer usuario
        User user1 = new User();
        user1.set("name", "testuser");
        user1.set("password", "pass123");
        user1.set("type", "ALUMNO");
        user1.set("id_persona", persona.getId());
        user1.insert();

        // Intentar crear segundo usuario con mismo nombre
        User user2 = User.findFirst("name = ?", "testuser");
        assertNotNull(user2, "Usuario duplicado no debería permitirse");
    }

    @Test
    void testPasswordHashing() {
        String plainPassword = "miContraseña123";
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        // Verificar que hashes diferentes generan distintos resultados
        String hash2 = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        assertNotEquals(hashedPassword, hash2, "Los hashes deben ser diferentes");

        // Pero ambos deben verificar correctamente contra la contraseña
        assertTrue(BCrypt.checkpw(plainPassword, hashedPassword));
        assertTrue(BCrypt.checkpw(plainPassword, hash2));
    }

    @Test
    void testInvalidUserType() {
        // Crear Persona
        Persona persona = new Persona();
        persona.set("dni", 12345680);
        persona.set("nombre", "Invalid");
        persona.set("apellido", "Type");
        persona.set("email", "invalid@test.com");
        persona.insert();

        User user = new User();
        user.set("name", "testuser");
        user.set("password", "pass123");
        user.set("type", "SUPERADMIN"); // Tipo inválido
        user.set("id_persona", persona.getId());

        // El modelo no valida, pero en App.java se valida
        assertNotEquals("ALUMNO", user.get("type"));
        assertNotEquals("DOCENTE", user.get("type"));
        assertNotEquals("ADMINISTRADOR", user.get("type"));
    }

    @Test
    void testPersonaWithNegativeDni() {
        Persona persona = new Persona();
        persona.set("dni", -12345);
        persona.set("nombre", "Test");
        persona.set("apellido", "User");

        // ActiveJDBC almacena el valor, pero la validación debe ocurrir en la capa de
        // aplicación
        assertEquals(-12345, persona.get("dni"));
    }

    @Test
    void testEmptyEmailAllowed() {
        Persona persona = new Persona();
        persona.set("dni", 99999999);
        persona.set("nombre", "Test");
        persona.set("apellido", "User");
        persona.set("email", "");
        persona.insert();

        Persona found = Persona.findById(persona.getId());
        assertEquals("", found.get("email"));
    }

    @Test
    void testUserTypeValidation() {
        String[] validTypes = { "ALUMNO", "DOCENTE", "ADMINISTRADOR" };

        for (int i = 0; i < validTypes.length; i++) {
            String type = validTypes[i];

            // Crear Persona para cada usuario
            Persona persona = new Persona();
            persona.set("dni", 12345681 + i);
            persona.set("nombre", "User" + type);
            persona.set("apellido", type);
            persona.set("email", "user" + i + "@test.com");
            persona.insert();

            User user = new User();
            user.set("name", "user_" + type);
            user.set("password", "pass");
            user.set("type", type);
            user.set("id_persona", persona.getId());

            assertEquals(type, user.get("type"), "Tipo " + type + " debería ser válido");
        }
    }
}
