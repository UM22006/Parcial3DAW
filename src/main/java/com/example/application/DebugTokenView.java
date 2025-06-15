package com.example.application;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.example.application.views.MainLayout;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@PageTitle("Debug Token")
@Route(value = "debug-token", layout = MainLayout.class)
public class DebugTokenView extends VerticalLayout {

    public DebugTokenView() {
        add(new H2("🔍 JWT Token Debug"));

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof OAuth2AuthenticationToken oauth2Auth) {
            var attributes = oauth2Auth.getPrincipal().getAttributes();
            var authorities = oauth2Auth.getAuthorities();

            add(new Paragraph("✅ Usuario: " + oauth2Auth.getName()));
            add(new Paragraph("📌 Authorities: " + authorities.toString()));
            add(new Paragraph("🔐 Atributos:"));

            attributes.forEach((k, v) -> add(new Paragraph(k + ": " + v)));
        } else {
            add(new Paragraph("❌ No OAuth2 token found in authentication object."));
        }
    }
}
