package com.giftora.service;

import com.giftora.dao.UserDao;
import com.giftora.exception.ValidationException;
import com.giftora.model.Role;
import com.giftora.model.User;
import com.giftora.util.PasswordUtil;
import com.giftora.util.ValidationUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new user. Only BUYER and SELLER roles may self-register; ADMIN is never public.
     */
    public User register(String name, String email, String password, String confirmPassword, String roleValue)
            throws ValidationException, SQLException {
        if (ValidationUtil.isBlank(name) || name.trim().length() < 2) {
            throw new ValidationException("Please enter your full name (at least 2 characters).");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match.");
        }
        Role role = Role.fromString(roleValue);
        if (role == null || role == Role.ADMIN) {
            role = Role.BUYER;
        }
        String normalizedEmail = email.trim().toLowerCase();
        if (userDao.emailExists(normalizedEmail)) {
            throw new ValidationException("An account with this email already exists.");
        }
        User user = new User(name.trim(), normalizedEmail, PasswordUtil.hash(password), role);
        userDao.insert(user);
        return user;
    }

    public Optional<User> authenticate(String email, String password) throws SQLException {
        if (ValidationUtil.isBlank(email) || ValidationUtil.isBlank(password)) {
            return Optional.empty();
        }
        Optional<User> found = userDao.findByEmail(email.trim());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User user = found.get();
        if (!user.isActive()) {
            return Optional.empty();
        }
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
