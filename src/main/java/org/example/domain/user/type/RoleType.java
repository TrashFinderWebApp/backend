package org.example.domain.user.type;

public enum RoleType {
    ROLE_GUEST("비회원"), ROLE_USER("회원");

    private String name;

    RoleType(String name) {
        this.name = name;
    }
}
