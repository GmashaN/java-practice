package ru.gerilovich.models;

public enum Role {
    USER,
    ADMIN;

    public String getRole() {
        return "ROLE_" + name();
    }
}