package org.momo.service;

import org.momo.model.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {
    private final Map<String, User> users = Collections.synchronizedMap(new HashMap<>());
    private volatile User currentUser = null;

    public boolean register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) return false;
        synchronized (users) {
            if (users.containsKey(username)) return false;
            users.put(username, new User(username, password));
            return true;
        }
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        User u = users.get(username);
        if (u == null) return false;
        if (!u.getPassword().equals(password)) return false;
        currentUser = u;
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }
}