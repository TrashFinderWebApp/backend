package org.example.domain.trashcan.exception;

public class TrashcanNotFoundException extends RuntimeException {
    public TrashcanNotFoundException(String message) {
        super(message);
    }
}
