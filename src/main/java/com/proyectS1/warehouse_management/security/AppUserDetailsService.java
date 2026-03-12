package com.proyectS1.warehouse_management.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.repositories.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email " + username));

        return User.withUsername(user.getEmail())
            .password(user.getHashPassword())
            .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            .disabled(!user.getEnabled())
            .build();
    }
}
