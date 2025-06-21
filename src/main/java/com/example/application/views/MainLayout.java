package com.example.application.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public class MainLayout extends AppLayout {

    private H1 viewTitle;

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addDrawerContent();
        addHeaderContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

        addToNavbar(true, toggle, viewTitle);
    }

    private void addDrawerContent() {
        Span appName = new Span("My App");
        appName.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.FontSize.LARGE);
        Header header = new Header(appName);

        Scroller scroller = new Scroller(createNavigation());

        addToDrawer(header, scroller, createFooter());
    }

    private SideNav createNavigation() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Home", "", new Icon(VaadinIcon.HOME)));
        nav.addItem(new SideNavItem("Estudiantes", "estudiantes", new Icon(VaadinIcon.USER)));
        nav.addItem(new SideNavItem("Control de Horas", "horas", new Icon(VaadinIcon.CLOCK)));

        return nav;
    }

    private Footer createFooter() {
        Footer footer = new Footer();

        // Botón de logout actualizado con logout de Spring y Google
        Button logoutButton = new Button("Cerrar sesión", new Icon(VaadinIcon.SIGN_OUT));
        logoutButton.addClickListener(e -> {
            UI.getCurrent().getPage().executeJs("""
                fetch('/logout', { method: 'POST', credentials: 'same-origin' })
                    .finally(() => {
                        setTimeout(function() {
                            window.location.href = 'https://accounts.google.com/Logout';
                        }, 500);
                    });
            """);
        });

        HorizontalLayout layout = new HorizontalLayout(logoutButton);
        layout.setPadding(true);
        footer.add(layout);

        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }

    private String getCurrentPageTitle() {
        if (getContent() != null && getContent().getClass().isAnnotationPresent(PageTitle.class)) {
            return getContent().getClass().getAnnotation(PageTitle.class).value();
        }
        return "";
    }
}
