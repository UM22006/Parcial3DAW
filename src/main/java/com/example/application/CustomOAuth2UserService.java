package com.example.application;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;
import java.util.stream.Collectors;

public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final String namespace = "https://parcial2.um22006/";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User user = super.loadUser(userRequest);

        List<String> roles = user.getAttribute(namespace + "roles");
        if (roles == null) roles = List.of();

        var authorities = roles.stream()
            .map(role -> "ROLE_" + role.toUpperCase())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        System.out.println("✅ Roles recibidos desde Auth0:");
        authorities.forEach(a -> System.out.println(" - " + a.getAuthority()));

        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            authorities,
            user.getAttributes(),
            "sub"
        );
    }
}
