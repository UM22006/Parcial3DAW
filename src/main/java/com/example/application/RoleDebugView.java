package com.example.application;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.stream.Collectors;

@Route(value = "debug-roles")
@PageTitle("Debug Roles")
public class RoleDebugView extends VerticalLayout {

    public RoleDebugView() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String username = (auth != null) ? auth.getName() : "NO AUTENTICADO";
        String roles = (auth != null)
            ? auth.getAuthorities().stream()
                  .map(GrantedAuthority::getAuthority)
                  .collect(Collectors.joining(", "))
            : "NINGUNO";

        add(
            new H1("🔍 Debug de Roles"),
            new Pre("Usuario: " + username),
            new Pre("Roles: " + roles)
        );
    }
}
