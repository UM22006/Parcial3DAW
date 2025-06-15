package com.example.application;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CustomOidcUserService extends OidcUserService {

    private final String namespace = "https://parcial2.um22006/";

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) {
        OidcUser oidcUser = super.loadUser(userRequest);

        // Obtener los roles personalizados desde el claim del token
        List<String> roles = oidcUser.getClaimAsStringList(namespace + "roles");
        if (roles == null) roles = List.of();

        // Convertir los roles a GrantedAuthority
        Collection<GrantedAuthority> authorities = roles.stream()
            .map(role -> "ROLE_" + role.toUpperCase())
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        System.out.println("✅ Roles desde ID Token (OIDC):");
        authorities.forEach(a -> System.out.println(" - " + a.getAuthority()));

        // Crear un nuevo OidcUser con las authorities actualizadas
        return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
