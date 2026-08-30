package com.siakad.auth.security;

import com.siakad.auth.entity.UserEntity;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter {@link UserEntity} menuju {@link UserDetails} Spring Security.
 */
public class UserPrincipal implements UserDetails {

    private final UserEntity user;

    public UserPrincipal(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getHashedPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    public Integer getId() {
        return user.getId();
    }

    public Role getRole() {
        return user.getRole();
    }

    public Jenjang getJenjang() {
        return user.getJenjang();
    }

    public UserEntity getUser() {
        return user;
    }
}