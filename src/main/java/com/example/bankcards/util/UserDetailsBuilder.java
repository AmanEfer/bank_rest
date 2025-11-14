package com.example.bankcards.util;

import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsBuilder {

    private UserDetailsBuilder() {
    }


    public static CustomUserDetails buildUserDetails(User user) {
        var authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(
                user.getId(),
                user.getPhoneNumber(),
                user.getPassword(),
                authorities
        );
    }
}
