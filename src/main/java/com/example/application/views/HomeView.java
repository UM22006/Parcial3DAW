package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "inicio", layout = MainLayout.class) // Página raíz (http://localhost:8080/)
@PageTitle("Inicio")
@PermitAll
public class HomeView extends VerticalLayout {

    public HomeView() {
        add(new H1("¡Bienvenido! Has iniciado sesión correctamente."));
    }
}
