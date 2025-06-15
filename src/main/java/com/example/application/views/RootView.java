package com.example.application.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("") // ← vista en raíz, necesaria para index.html
public class RootView extends VerticalLayout {
    public RootView() {
        add(new H1("Vista raíz generada para producción"));
    }
}
