package com.ems.service;

import com.ems.dao.UserAuthRecord;
import com.ems.dao.UserDao;
import com.ems.model.User;
import com.ems.util.PasswordUtil;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User login(String username, char[] password) {
        UserAuthRecord r = userDao.findAuthByUsername(username);
        if (r == null) return null;
        if (!"ACTIVE".equalsIgnoreCase(r.status)) return null;

        byte[] computed = PasswordUtil.pbkdf2(password, r.salt, r.iterations);
        if (!PasswordUtil.constantTimeEquals(computed, r.passwordHash)) return null;

        return new User(r.id, r.username, r.role, r.status);
    }
}