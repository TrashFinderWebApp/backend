package org.example.global.security.authentication;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Setter;
import org.example.domain.user.domain.User;
import org.example.domain.user.type.RoleType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal implements UserDetails {

    private Long id;
    private String email;
    private String socialId;
    private Collection<? extends GrantedAuthority> authorities;

    @Setter
    private Map<String, Object> attributes;

    public UserPrincipal(Long id, String email, String socialId, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.socialId = socialId;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(RoleType.ROLE_USER.name()));
        UserPrincipal userDetails = new UserPrincipal(user.getId(), user.getEmail(), user.getSocialId(), authorities);
        userDetails.setAttributes(attributes);
        return userDetails;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
