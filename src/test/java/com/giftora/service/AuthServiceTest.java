package com.giftora.service;

import com.giftora.dao.UserDao;
import com.giftora.exception.ValidationException;
import com.giftora.model.Role;
import com.giftora.model.User;
import com.giftora.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserDao userDao;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        authService = new AuthService(userDao);
    }

    @Test
    void registerHashesPasswordAndNormalisesEmail() throws Exception {
        when(userDao.emailExists(anyString())).thenReturn(false);

        User user = authService.register("Ada Lovelace", "ADA@Giftora.Example ", "Password1", "Password1", "seller");

        assertEquals(Role.SELLER, user.getRole());
        assertEquals("ada@giftora.example", user.getEmail());
        assertTrue(PasswordUtil.verify("Password1", user.getPasswordHash()));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(captor.capture());
        assertEquals("ada@giftora.example", captor.getValue().getEmail());
    }

    @Test
    void registerDefaultsUnknownRoleToBuyerAndBlocksAdmin() throws Exception {
        when(userDao.emailExists(anyString())).thenReturn(false);

        assertEquals(Role.BUYER, authService.register("Buyer One", "buyer@x.com", "Password1", "Password1", "unknown").getRole());
        assertEquals(Role.BUYER, authService.register("Admin Wannabe", "admin@x.com", "Password1", "Password1", "ADMIN").getRole());
    }

    @Test
    void registerRejectsMismatchedPasswords() {
        assertThrows(ValidationException.class, () ->
                authService.register("Buyer One", "buyer@x.com", "Password1", "Password2", "BUYER"));
    }

    @Test
    void registerRejectsWeakPassword() {
        assertThrows(ValidationException.class, () ->
                authService.register("Buyer One", "buyer@x.com", "short", "short", "BUYER"));
    }

    @Test
    void registerRejectsDuplicateEmail() throws SQLException {
        when(userDao.emailExists("taken@x.com")).thenReturn(true);
        assertThrows(ValidationException.class, () ->
                authService.register("Buyer One", "taken@x.com", "Password1", "Password1", "BUYER"));
        verify(userDao, never()).insert(any());
    }

    @Test
    void authenticateSucceedsWithCorrectCredentials() throws SQLException {
        User stored = new User("Buyer One", "buyer@x.com", PasswordUtil.hash("Password1"), Role.BUYER);
        stored.setActive(true);
        when(userDao.findByEmail("buyer@x.com")).thenReturn(Optional.of(stored));

        Optional<User> result = authService.authenticate("buyer@x.com", "Password1");
        assertTrue(result.isPresent());
    }

    @Test
    void authenticateFailsWithWrongPassword() throws SQLException {
        User stored = new User("Buyer One", "buyer@x.com", PasswordUtil.hash("Password1"), Role.BUYER);
        stored.setActive(true);
        when(userDao.findByEmail(anyString())).thenReturn(Optional.of(stored));

        assertTrue(authService.authenticate("buyer@x.com", "nope").isEmpty());
    }

    @Test
    void authenticateRejectsDisabledAccount() throws SQLException {
        User stored = new User("Buyer One", "buyer@x.com", PasswordUtil.hash("Password1"), Role.BUYER);
        stored.setActive(false);
        when(userDao.findByEmail("buyer@x.com")).thenReturn(Optional.of(stored));

        assertTrue(authService.authenticate("buyer@x.com", "Password1").isEmpty());
    }

    @Test
    void authenticateReturnsEmptyForBlankInput() throws SQLException {
        assertTrue(authService.authenticate("", "").isEmpty());
        verify(userDao, never()).findByEmail(anyString());
    }
}
