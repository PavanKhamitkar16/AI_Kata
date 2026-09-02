package com.hospital.patientmanagement.security;

import com.hospital.patientmanagement.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter between the JPA {@link User} entity and Spring Security's
 * {@link UserDetails} contract.
 *
 * <p>Keeping them separate prevents the persistence layer from depending on
 * the security layer and makes testing easier — service tests receive a plain
 * {@link User} while security infrastructure receives this wrapper.
 */
public class CustomUserDetails implements UserDetails {

    @Getter
    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Returns a single {@code ROLE_<ROLE_NAME>} authority as required by
     * Spring Security's {@code hasRole()} / {@code hasAuthority()} matchers.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String   getPassword()            { return user.getPassword(); }
    @Override public String   getUsername()            { return user.getUsername(); }
    @Override public boolean  isAccountNonExpired()    { return user.isAccountNonExpired(); }
    @Override public boolean  isAccountNonLocked()     { return user.isAccountNonLocked(); }
    @Override public boolean  isCredentialsNonExpired(){ return user.isCredentialsNonExpired(); }
    @Override public boolean  isEnabled()              { return user.isEnabled(); }
}
