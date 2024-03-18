package org.example.domain.user.domain;

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
import org.example.domain.user.type.AuthType;
import org.example.domain.user.type.RoleType;

@Entity
@Builder
@Getter
@AllArgsConstructor
@Table(name = "user")
public class User extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "social_id", unique = true)
    private String socialId;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "email", length = 20)
    private String email;

    @Column(name = "user_password", length = 15)
    private String password;

    @Enumerated(EnumType.STRING)
    private AuthType authType;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    public User update(String email, String name, String oauth2Id) {
        this.email = email;
        this.name = name;
        this.socialId = oauth2Id;
        return this;
    }
}
