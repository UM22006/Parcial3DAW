package com.example.application.views.estudiantes;

import com.example.application.data.ControlHoras;
import com.example.application.data.Estudiante;
import com.example.application.data.services.ControlHorasService;
import com.example.application.data.services.EstudianteService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

@PageTitle("Estudiantes")
@Route(value = "estudiantes", layout = MainLayout.class)
@PreAuthorize("hasRole('ADMIN')")
public class EstudiantesView extends VerticalLayout implements BeforeEnterObserver {

    private final EstudianteService estudianteService;
    private final ControlHorasService horasService;

    private final TextField carnet = new TextField("Carnet");
    private final TextField primerNombre = new TextField("Primer Nombre");
    private final TextField segundoNombre = new TextField("Segundo Nombre");
    private final TextField primerApellido = new TextField("Primer Apellido");
    private final TextField segundoApellido = new TextField("Segundo Apellido");
    private final TextField numeroTelefono = new TextField("Teléfono");
    private final EmailField correo = new EmailField("Correo");
    private final IntegerField edad = new IntegerField("Edad");
    private final TextField direccion = new TextField("Dirección");
    private final TextField carrera = new TextField("Carrera");

    private final Button guardar = new Button("💾 Guardar");
    private final Button limpiar = new Button("🩹 Limpiar");

    private final TextField buscarCarnet = new TextField("Buscar por Carnet");
    private final Button buscarBtn = new Button("🔍 Buscar");

    private final Grid<Estudiante> grid = new Grid<>(Estudiante.class, false);
    private final Grid<ControlHoras> gridHoras = new Grid<>(ControlHoras.class, false);
    private final BeanValidationBinder<Estudiante> binder = new BeanValidationBinder<>(Estudiante.class);

    private final Label resumenHorasLabel = new Label();

    private Estudiante estudianteActual;

    public EstudiantesView(EstudianteService estudianteService, ControlHorasService horasService) {
        this.estudianteService = estudianteService;
        this.horasService = horasService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 titulo = new H2("🎓 Registro de Estudiantes");

        FormLayout formLayout = crearFormulario();
        configurarBotones();
        configurarGrid();
        configurarGridHoras();
        actualizarGrid();

        HorizontalLayout acciones = new HorizontalLayout(guardar, limpiar);
        HorizontalLayout buscarLayout = new HorizontalLayout(buscarCarnet, buscarBtn);

        buscarBtn.addClickListener(e -> buscarEstudiantePorCarnet());

        resumenHorasLabel.getStyle().set("font-weight", "bold").set("margin", "10px 0");

        add(titulo, formLayout, acciones, buscarLayout, grid, resumenHorasLabel, gridHoras);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            event.rerouteTo("inicio");
        }
    }

    private FormLayout crearFormulario() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.add(
                carnet, primerNombre, segundoNombre,
                primerApellido, segundoApellido,
                numeroTelefono, correo, edad,
                direccion, carrera
        );

        formLayout.setColspan(direccion, 2);
        formLayout.setColspan(carrera, 2);

        binder.bindInstanceFields(this);
        return formLayout;
    }

    private void configurarBotones() {
        guardar.addClickListener(e -> guardarEstudiante());
        limpiar.addClickListener(e -> limpiarFormulario());
    }

    private void guardarEstudiante() {
        Estudiante nuevo = (estudianteActual != null) ? estudianteActual : new Estudiante();

        if (binder.writeBeanIfValid(nuevo)) {
            estudianteService.save(nuevo);
            Notification.show("✅ Estudiante guardado con éxito");
            estudianteActual = null;
            binder.readBean(null);
            actualizarGrid();
        } else {
            Notification.show("❌ Corrige los errores del formulario");
        }
    }

    private void limpiarFormulario() {
        estudianteActual = null;
        binder.readBean(null);
    }

    private void configurarGrid() {
        grid.removeAllColumns();
        grid.addColumn(Estudiante::getCarnet).setHeader("Carnet");
        grid.addColumn(Estudiante::getPrimerNombre).setHeader("Primer Nombre");
        grid.addColumn(Estudiante::getSegundoNombre).setHeader("Segundo Nombre");
        grid.addColumn(Estudiante::getPrimerApellido).setHeader("Primer Apellido");
        grid.addColumn(Estudiante::getSegundoApellido).setHeader("Segundo Apellido");
        grid.addColumn(Estudiante::getNumeroTelefono).setHeader("Teléfono");
        grid.addColumn(Estudiante::getCorreo).setHeader("Correo");
        grid.addColumn(Estudiante::getEdad).setHeader("Edad");
        grid.addColumn(Estudiante::getDireccion).setHeader("Dirección");
        grid.addColumn(Estudiante::getCarrera).setHeader("Carrera");

        grid.addComponentColumn(estudiante -> {
            Button editarBtn = new Button("✏️ Editar");
            Button eliminarBtn = new Button("🗑️ Eliminar");

            editarBtn.addClickListener(e -> editarEstudiante(estudiante));
            eliminarBtn.addClickListener(e -> mostrarConfirmacionEliminar(estudiante));

            return new HorizontalLayout(editarBtn, eliminarBtn);
        }).setHeader("Acciones");
    }

    private void mostrarConfirmacionEliminar(Estudiante estudiante) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar Eliminación");
        dialog.setText("¿Estás seguro de eliminar al estudiante " + estudiante.getCarnet() + "?");
        dialog.setConfirmText("Eliminar");
        dialog.setCancelText("Cancelar");
        dialog.addConfirmListener(e -> {
            estudianteService.delete(estudiante);
            Notification.show("Estudiante eliminado");
            actualizarGrid();
        });
        dialog.open();
    }

    private void configurarGridHoras() {
        gridHoras.removeAllColumns();
        gridHoras.addColumn(h -> h.getFechaHoraIngreso().toLocalDate()).setHeader("Entrada");
        gridHoras.addColumn(h -> h.getFechaHoraSalida().toLocalDate()).setHeader("Salida");
        gridHoras.addColumn(ControlHoras::getHorasTrabajadas).setHeader("Horas");
        gridHoras.addColumn(ControlHoras::getActividadesRealizadas).setHeader("Actividades");
        gridHoras.addColumn(h -> h.getEstado() != null ? h.getEstado() : "Pendiente").setHeader("Estado");

        gridHoras.addComponentColumn(hora -> {
            Button aprobar = new Button("✅");
            Button rechazar = new Button("❌");

            aprobar.addClickListener(e -> {
                hora.setEstado("Aprobada");
                horasService.save(hora);
                Notification.show("Hora aprobada");
                buscarEstudiantePorCarnet();
            });

            rechazar.addClickListener(e -> {
                hora.setEstado("Rechazada");
                horasService.save(hora);
                Notification.show("Hora rechazada");
                buscarEstudiantePorCarnet();
            });

            return new HorizontalLayout(aprobar, rechazar);
        }).setHeader("Acciones");
    }

    private void buscarEstudiantePorCarnet() {
        String carnetValor = buscarCarnet.getValue();
        estudianteService.findByCarnet(carnetValor).ifPresentOrElse(est -> {
            grid.setItems(est);
            List<ControlHoras> horas = horasService.findByCarnet(carnetValor);
            gridHoras.setItems(horas);

            long aprobadas = horas.stream().filter(h -> "Aprobada".equalsIgnoreCase(h.getEstado())).count();
            long rechazadas = horas.stream().filter(h -> "Rechazada".equalsIgnoreCase(h.getEstado())).count();
            long pendientes = horas.stream().filter(h -> h.getEstado() == null || h.getEstado().isBlank() || "Pendiente".equalsIgnoreCase(h.getEstado())).count();

            String resumenTexto = String.format("Resumen: ✅ Aprobadas: %d | ❌ Rechazadas: %d | ⏳ Pendientes: %d", aprobadas, rechazadas, pendientes);
            resumenHorasLabel.setText(resumenTexto);

        }, () -> {
            Notification.show("Estudiante no encontrado");
            grid.setItems();
            gridHoras.setItems();
            resumenHorasLabel.setText("");
        });
    }

    private void editarEstudiante(Estudiante estudiante) {
        this.estudianteActual = estudiante;
        binder.readBean(estudiante);
    }

    private void actualizarGrid() {
        grid.setItems(estudianteService.findAll());
    }
}
