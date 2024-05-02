package org.example.domain.member.type;

public enum RoleType {
    GUEST("비회원"), USER("회원"), BAN("금지된 회원"), ADMIN("관리자");

    private String name;

    RoleType(String name) {
        this.name = name;
    }
}
