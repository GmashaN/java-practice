package ru.gerilovich.services.exceptions;

public class DuplicateOwnerException extends RuntimeException {
    public DuplicateOwnerException(String message) {
        super(message);
    }
}