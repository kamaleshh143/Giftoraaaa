package com.giftora.model;

import java.io.Serializable;
import java.time.Instant;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;
    private Instant createdAt;

    public User() {
        this.role = Role.BUYER;
        this.active = true;
    }

    public User(String name, String email, String passwordHash, Role role) {
        this();
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Date view for JSTL {@code <fmt:formatDate>}, which cannot format {@link Instant} directly.
     */
    public java.util.Date getCreatedAtDate() {
        return createdAt == null ? null : java.util.Date.from(createdAt);
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isSeller() {
        return role == Role.SELLER;
    }

    public boolean isBuyer() {
        return role == Role.BUYER;
    }
}
