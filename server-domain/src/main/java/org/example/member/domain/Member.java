package org.example.member.domain;

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
import org.example.member.type.AuthType;
import org.example.member.type.RoleType;

@Entity
@Builder
@AllArgsConstructor
@Table(name = "member")
public class Member extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "social_id", unique = true)
    private Long socialId;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "email", length = 20)
    private String email;

    @Column(name = "member_password", length = 15)
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthType authType;

    @Enumerated(EnumType.STRING)
    private RoleType role;

}
