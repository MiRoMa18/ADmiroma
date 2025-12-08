package org.example.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.model.enums.TipoFichaje;
import org.example.service.ClimaService;
import org.example.util.AlertasUtil;
import org.example.util.ValidadorUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para el diálogo de crear/editar fichaje.
 */
public class DialogoFichajeController {

    @FXML private ComboBox<Trabajador> cmbTrabajador;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<TipoFichaje> cmbTipo;
    @FXML private TextField txtClima;
    @FXML private Button btnObtenerClima;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Fichaje fichajeEditar = null;
    private boolean guardado = false;
    private final FichajeDAO fichajeDAO = new FichajeDAO();
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final ClimaService climaService = new ClimaService();

    public void inicializarNuevo() throws Exception {
        this.fichajeEditar = null;
        System.out.println("➕ Modo: CREAR nuevo fichaje");

        configurarComponentes();
        cargarTrabajadores();

        dpFecha.setValue(LocalDate.now());
        txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        cmbTipo.setValue(TipoFichaje.ENTRADA);

        obtenerClimaActual();
    }

    public void inicializarEditar(Fichaje fichaje) {
        this.fichajeEditar = fichaje;
        System.out.println("✏️ Modo: EDITAR fichaje - ID: " + fichaje.getId());

        configurarComponentes();
        cargarTrabajadores();

        cmbTrabajador.setValue(fichaje.getTrabajador());
        cmbTrabajador.setDisable(true); // No permitir cambiar trabajador

        dpFecha.setValue(fichaje.getFechaHora().toLocalDate());
        txtHora.setText(fichaje.getFechaHora().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        cmbTipo.setValue(fichaje.getTipo());
        txtClima.setText(fichaje.getClima());
        txtNotas.setText(fichaje.getNotas());
    }

    private void configurarComponentes() {
        cmbTipo.getItems().setAll(TipoFichaje.values());

        txtHora.setPromptText("HH:mm");
        txtNotas.setWrapText(true);

        txtNotas.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= 500) {
                return change;
            }
            return null;
        }));
    }

    private void cargarTrabajadores() {
        try {
            List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();
            cmbTrabajador.getItems().setAll(trabajadores);

            cmbTrabajador.setButtonCell(new ListCell<Trabajador>() {
                @Override
                protected void updateItem(Trabajador item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });

            cmbTrabajador.setCellFactory(param -> new ListCell<Trabajador>() {
                @Override
                protected void updateItem(Trabajador item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getNombreCompleto());
                }
            });
        } catch (Exception e) {
            AlertasUtil.mostrarError("Error", "No se pudieron cargar los trabajadores");
        }
    }

    private boolean validarCampos() {
        if (cmbTrabajador.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar un trabajador");
            return false;
        }

        if (dpFecha.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar una fecha");
            return false;
        }

        if (dpFecha.getValue().isAfter(LocalDate.now())) {
            AlertasUtil.mostrarError("Error", "La fecha no puede ser futura");
            return false;
        }

        String hora = txtHora.getText().trim();
        if (!ValidadorUtil.esHoraValida(hora)) {
            AlertasUtil.mostrarError("Error", "Hora inválida (formato HH:mm)");
            return false;
        }

        if (cmbTipo.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar el tipo de fichaje");
            return false;
        }

        // Verificar que la fecha-hora no sea futura
        LocalDateTime fechaHora = LocalDateTime.of(
                dpFecha.getValue(),
                LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"))
        );

        if (fechaHora.isAfter(LocalDateTime.now())) {
            AlertasUtil.mostrarError("Error", "La fecha y hora no pueden ser futuras");
            return false;
        }

        return true;
    }

    @FXML
    private void handleObtenerClima() throws Exception {
        obtenerClimaActual();
    }

    private void obtenerClimaActual() throws Exception {
        String clima = climaService.obtenerClima();
        txtClima.setText(clima);
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            Trabajador trabajador = cmbTrabajador.getValue();
            LocalDate fecha = dpFecha.getValue();
            LocalTime hora = LocalTime.parse(txtHora.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            TipoFichaje tipo = cmbTipo.getValue();
            String clima = txtClima.getText().trim();
            String notas = txtNotas.getText().trim();

            if (fichajeEditar == null) {
                // CREAR
                Fichaje nuevo = new Fichaje();
                nuevo.setTrabajador(trabajador);
                nuevo.setFechaHora(fechaHora);
                nuevo.setTipo(tipo);
                nuevo.setClima(clima.isEmpty() ? null : clima);
                nuevo.setNotas(notas.isEmpty() ? null : notas);

                if (fichajeDAO.guardar(nuevo)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo guardar el fichaje");
                }
            } else {
                // EDITAR
                fichajeEditar.setFechaHora(fechaHora);
                fichajeEditar.setTipo(tipo);
                fichajeEditar.setClima(clima.isEmpty() ? null : clima);
                fichajeEditar.setNotas(notas.isEmpty() ? null : notas);

                if (fichajeDAO.actualizar(fichajeEditar)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo actualizar el fichaje");
                }
            }
        } catch (Exception e) {
            AlertasUtil.mostrarError("Error", "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        guardado = false;
        cerrarDialogo();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public boolean isGuardado() {
        return guardado;
    }
}