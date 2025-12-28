package com.molla.service.impl;

import com.molla.model.User;
import com.molla.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserImplementation implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // 1. Fetch user from database using email
        User user = userRepository.findByEmail(username);

        // 2. If user not found → throw exception
        if (user == null) {
            throw new UsernameNotFoundException(
                    "User not found with email: " + username
            );
        }

        // 3. Convert role to GrantedAuthority
        GrantedAuthority authority =
                new SimpleGrantedAuthority(user.getRole().toString());

        Collection<GrantedAuthority> authorities =
                Collections.singletonList(authority);

        // 4. Return Spring Security User object
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}