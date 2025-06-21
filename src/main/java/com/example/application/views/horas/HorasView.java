package com.example.application.views.horas;

import com.example.application.data.ControlHoras;
import com.example.application.data.Estudiante;
import com.example.application.data.services.ControlHorasService;
import com.example.application.data.services.EstudianteService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;

@PageTitle("Control de Horas")
@Route(value = "horas", layout = MainLayout.class)
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public class HorasView extends VerticalLayout implements BeforeEnterObserver {

    private final EstudianteService estudianteService;
    private final ControlHorasService controlHorasService;

    private final ComboBox<String> buscarCarnet = new ComboBox<>("Buscar por Carnet");
    private final TextField nombreCompleto = new TextField("Nombre del Estudiante");

    private final TextField carnet = new TextField("Carnet");
    private final DateTimePicker fechaHoraIngreso = new DateTimePicker("Fecha y Hora de Ingreso");
    private final DateTimePicker fechaHoraSalida = new DateTimePicker("Fecha y Hora de Salida");
    private final TextArea actividades = new TextArea("Actividades Realizadas");
    private final TextField horasTrabajadas = new TextField("Horas Trabajadas");

    private final Button guardar = new Button("💾 Guardar");
    private final Button limpiar = new Button("🩹 Limpiar");

    private final Grid<ControlHoras> grid = new Grid<>(ControlHoras.class, false);
    private final ProgressBar progresoHoras = new ProgressBar();

    private final BeanValidationBinder<ControlHoras> binder = new BeanValidationBinder<>(ControlHoras.class);

    private ControlHoras registroActual;

    public HorasView(EstudianteService estudianteService, ControlHorasService controlHorasService) {
        this.estudianteService = estudianteService;
        this.controlHorasService = controlHorasService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H2 titulo = new H2("🕒 Registro de Horas de Servicio Social");

        configurarBuscador();
        configurarFormulario();
        configurarBotones();
        configurarGrid();
        configurarProgressBar();

        HorizontalLayout acciones = new HorizontalLayout(guardar, limpiar);
        add(titulo, buscarCarnet, nombreCompleto, crearFormulario(), acciones, progresoHoras, grid);

        actualizarGrid();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        boolean tieneAcceso = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ROLE_USER"));

        if (!tieneAcceso) {
            event.rerouteTo("inicio");
        }
    }

    private void configurarBuscador() {
        buscarCarnet.setItems(estudianteService.findAll().stream().map(Estudiante::getCarnet).toList());
        buscarCarnet.addValueChangeListener(e -> {
            String carnet = e.getValue();
            Optional<Estudiante> estudianteOpt = estudianteService.findByCarnet(carnet);
            estudianteOpt.ifPresent(est -> {
                nombreCompleto.setValue(est.getPrimerNombre() + " " + est.getPrimerApellido());
                grid.setItems(controlHorasService.findByCarnet(carnet));
                actualizarProgreso(carnet);
            });
        });
        nombreCompleto.setReadOnly(true);
    }

    private void configurarFormulario() {
        carnet.setClearButtonVisible(true);
        fechaHoraIngreso.addValueChangeListener(e -> calcularHorasTrabajadas());
        fechaHoraSalida.addValueChangeListener(e -> calcularHorasTrabajadas());
        horasTrabajadas.setReadOnly(true);
        binder.bindInstanceFields(this);
        // Enlace manual para actividades porque el nombre no coincide
        binder.forField(actividades)
            .bind(ControlHoras::getActividadesRealizadas, ControlHoras::setActividadesRealizadas);
    }

    private FormLayout crearFormulario() {
        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );
        form.add(carnet, fechaHoraIngreso, fechaHoraSalida, horasTrabajadas, actividades);
        form.setColspan(actividades, 2);
        return form;
    }

    private void configurarBotones() {
        guardar.addClickListener(e -> guardarRegistro());
        limpiar.addClickListener(e -> limpiarFormulario());
    }

    private void configurarGrid() {
        grid.addColumn(r -> r.getFechaHoraIngreso().toLocalDate()).setHeader("Ingreso");
        grid.addColumn(r -> r.getFechaHoraSalida().toLocalDate()).setHeader("Salida");
        grid.addColumn(ControlHoras::getHorasTrabajadas).setHeader("Horas");
        grid.addColumn(ControlHoras::getActividadesRealizadas).setHeader("Actividades");
        grid.addColumn(r -> r.getEstado() != null ? r.getEstado() : "Pendiente").setHeader("Estado");

        grid.addComponentColumn(registro -> {
            Button editarBtn = new Button("✏️ Editar", e -> editarRegistro(registro));
            Button eliminarBtn = new Button("🗑️ Eliminar", e -> confirmarEliminar(registro));
            return new HorizontalLayout(editarBtn, eliminarBtn);
        }).setHeader("Acciones");
    }

    private void confirmarEliminar(ControlHoras registro) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Deseas eliminar este registro de horas?");
        dialog.setConfirmText("Eliminar");
        dialog.setCancelText("Cancelar");
        dialog.addConfirmListener(e -> {
            controlHorasService.delete(registro);
            Notification.show("Registro eliminado");
            actualizarGrid();
            if (!buscarCarnet.isEmpty()) {
                actualizarProgreso(buscarCarnet.getValue());
            }
        });
        dialog.open();
    }

    private void guardarRegistro() {
        ControlHoras nuevo = (registroActual != null) ? registroActual : new ControlHoras();
        if (binder.writeBeanIfValid(nuevo)) {
            Estudiante est = estudianteService.findByCarnet(carnet.getValue()).orElse(null);
            if (est != null) {
                nuevo.setEstudiante(est);
                controlHorasService.save(nuevo);
                Notification.show("✅ Registro guardado");
                limpiarFormulario();
                actualizarGrid();
                actualizarProgreso(est.getCarnet());
            } else {
                Notification.show("❌ Carnet no válido");
            }
        } else {
            Notification.show("❌ Corrige los errores del formulario");
        }
    }

    private void editarRegistro(ControlHoras registro) {
        this.registroActual = registro;
        binder.readBean(registro);
        carnet.setValue(registro.getEstudiante().getCarnet());
    }

    private void limpiarFormulario() {
        registroActual = null;
        binder.readBean(null);
    }

    private void actualizarGrid() {
        grid.setItems(controlHorasService.findAll());
    }

    private void calcularHorasTrabajadas() {
        if (fechaHoraIngreso.getValue() != null && fechaHoraSalida.getValue() != null) {
            Duration duracion = Duration.between(fechaHoraIngreso.getValue(), fechaHoraSalida.getValue());
            BigDecimal horas = BigDecimal.valueOf(duracion.toMinutes() / 60.0).setScale(2, RoundingMode.HALF_UP);
            horasTrabajadas.setValue(horas.toString());
        }
    }

    private void configurarProgressBar() {
        progresoHoras.setMin(0);
        progresoHoras.setMax(500);
        progresoHoras.setValue(0);
    }

    private void actualizarProgreso(String carnet) {
        BigDecimal total = controlHorasService.findByCarnet(carnet).stream()
                .filter(h -> "Aprobada".equalsIgnoreCase(h.getEstado()))
                .map(ControlHoras::getHorasTrabajadas)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        progresoHoras.setValue(Math.min(total.floatValue(), 500));
    }
}
