package com.szusta.meduva.service;

import com.szusta.meduva.model.Role;
import com.szusta.meduva.model.User;

import java.util.List;

public interface UserService {
    User getUserById(Long id);
    User getUserByLogin(String login);
    User saveUser(User user);
    Role saveRole(Role role);
    void addRoleToUser(String login, String roleName);
    List<User> getUsers();
}
