package com.softserve.itacademy.config.security;

import com.softserve.itacademy.model.User;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Data
public class WebAuthenticationToken implements Authentication {

    private final User user;
    private boolean isAuthenticated;

    public WebAuthenticationToken(User user) {
        this.user = user;
        isAuthenticated = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public Object getCredentials() {
        return user.getPassword();
    }

    @Override
    public Object getDetails() {
        return user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user.getUsername();
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

}
