package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserDetailsBuilder {

    private UserDetailsBuilder() {
    }


    public static UserDetails buildUserDetails(User user) {
        var userDetailsRoles = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getPhoneNumber())
                .password(user.getPassword())
                .authorities(userDetailsRoles)
                .build();
    }
}
