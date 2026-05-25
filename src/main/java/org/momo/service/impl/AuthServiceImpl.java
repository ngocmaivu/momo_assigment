package org.momo.service.impl;

public interface AuthServiceImpl {
    boolean login(String username, String password);
    void logout();
}
