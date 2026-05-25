package org.momo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.momo.model.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private AuthService auth;

    @BeforeEach
    public void setup() {
        auth = new AuthService();
    }

    @Test
    public void registerAndLoginSuccessful() {
        boolean reg = auth.register("momo", "password");
        assertTrue(reg, "register should succeed for new user");

        boolean login = auth.login("momo", "password");
        assertTrue(login, "login should succeed with correct credentials");

        Optional<User> current = auth.getCurrentUser();
        assertTrue(current.isPresent(), "current user should be present after login");
        assertEquals("momo", current.get().getUsername());
    }

    @Test
    public void duplicateRegisterFails() {
        assertTrue(auth.register("bob", "p1"));
        assertFalse(auth.register("bob", "p2"), "register should fail for duplicate username");
    }

    @Test
    public void registerInvalidInputsFail() {
        assertFalse(auth.register(null, "x"), "null username should fail");
        assertFalse(auth.register("", "x"), "empty username should fail");
        assertFalse(auth.register("  ", "x"), "blank username should fail");
        assertFalse(auth.register("u", null), "null password should fail");
        assertFalse(auth.register("u", ""), "empty password should fail");
    }

    @Test
    public void loginWithWrongPasswordFails() {
        assertTrue(auth.register("carol", "secret"));
        assertFalse(auth.login("carol", "wrong"), "login should fail with incorrect password");
        assertFalse(auth.getCurrentUser().isPresent(), "no user should be logged in after failed login");
    }

    @Test
    public void loginNonExistentUserFails() {
        assertFalse(auth.login("doesnotexist", "x"), "login should fail for non-existent user");
    }

    @Test
    public void logoutClearsCurrentUser() {
        assertTrue(auth.register("dave", "pw"));
        assertTrue(auth.login("dave", "pw"));
        assertTrue(auth.getCurrentUser().isPresent());

        auth.logout();
        assertFalse(auth.getCurrentUser().isPresent(), "logout should clear current user");
    }
}