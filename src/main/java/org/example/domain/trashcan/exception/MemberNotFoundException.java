package org.example.domain.trashcan.exception;

public class MemberNotFoundException extends RuntimeException{
    public MemberNotFoundException(String message) {
        super(message);
    }
}
