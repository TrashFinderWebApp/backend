package org.example.domain.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.member.type.RoleType;
import org.example.global.domain.BaseTimeEntity;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "member")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "email", length = 20)
    private String email;

    @Column(name = "member_password", length = 15)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleType role;

    public Member update(String email, String name, String oauth2Id) {
        this.email = email;
        this.name = name;
        return this;
    }

    public Member(String email, String password, String name, RoleType role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }
}
