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

    /**
     * Inicializa el diálogo para crear un nuevo fichaje
     */
    public void inicializarNuevo() {
        this.fichajeEditar = null;
        configurarComponentes();

        // Cargar trabajadores con manejo de errores
        cargarTrabajadores();

        // Establecer valores por defecto
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
        }

        if (txtHora != null) {
            txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (cmbTipo != null) {
            cmbTipo.setValue(TipoFichaje.ENTRADA);
        }

        // Intentar obtener clima automáticamente
        obtenerClimaAutomaticamente();
    }

    /**
     * Inicializa el diálogo para editar un fichaje existente
     */
    public void inicializarEditar(Fichaje fichaje) {
        this.fichajeEditar = fichaje;
        configurarComponentes();

        cargarTrabajadores();

        // Cargar datos del fichaje
        if (cmbTrabajador != null) {
            cmbTrabajador.setValue(fichaje.getTrabajador());
            cmbTrabajador.setDisable(true);
        }

        if (dpFecha != null) {
            dpFecha.setValue(fichaje.getFechaHora().toLocalDate());
        }

        if (txtHora != null) {
            txtHora.setText(fichaje.getFechaHora().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (cmbTipo != null) {
            cmbTipo.setValue(fichaje.getTipo());
        }

        if (txtClima != null) {
            txtClima.setText(fichaje.getClima() != null ? fichaje.getClima() : "No disponible");
        }

        if (txtNotas != null) {
            txtNotas.setText(fichaje.getNotas());
        }
    }

    /**
     * Configura los componentes del formulario
     */
    private void configurarComponentes() {
        // Configurar ComboBox de tipo
        if (cmbTipo != null) {
            cmbTipo.getItems().setAll(TipoFichaje.values());
        }

        // Configurar TextField de hora
        if (txtHora != null) {
            txtHora.setPromptText("HH:mm");
        }

        // Configurar TextField de clima como SOLO LECTURA
        if (txtClima != null) {
            txtClima.setEditable(false);
            txtClima.setStyle("-fx-background-color: #f0f0f0; -fx-opacity: 1;");
            txtClima.setText("Cargando...");
        }

        // Configurar TextArea de notas
        if (txtNotas != null) {
            txtNotas.setWrapText(true);
            txtNotas.setTextFormatter(new TextFormatter<>(change -> {
                if (change.getControlNewText().length() <= 500) {
                    return change;
                }
                return null;
            }));
        }
    }

    /**
     * Carga la lista de trabajadores en el ComboBox
     */
    private void cargarTrabajadores() {
        try {
            List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();

            if (cmbTrabajador != null) {
                cmbTrabajador.getItems().setAll(trabajadores);

                // Configurar cómo se muestra el trabajador seleccionado
                cmbTrabajador.setButtonCell(new ListCell<Trabajador>() {
                    @Override
                    protected void updateItem(Trabajador item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNombreCompleto());
                    }
                });

                // Configurar cómo se muestran los trabajadores en la lista
                cmbTrabajador.setCellFactory(param -> new ListCell<Trabajador>() {
                    @Override
                    protected void updateItem(Trabajador item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? "" : item.getNombreCompleto());
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error al cargar trabajadores: " + e.getMessage());
            AlertasUtil.mostrarError("Error", "No se pudieron cargar los trabajadores");
        }
    }

    /**
     * Valida los campos del formulario
     */
    private boolean validarCampos() {
        if (cmbTrabajador == null || cmbTrabajador.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar un trabajador");
            return false;
        }

        if (dpFecha == null || dpFecha.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar una fecha");
            return false;
        }

        if (dpFecha.getValue().isAfter(LocalDate.now())) {
            AlertasUtil.mostrarError("Error", "La fecha no puede ser futura");
            return false;
        }

        if (txtHora == null || txtHora.getText() == null || txtHora.getText().trim().isEmpty()) {
            AlertasUtil.mostrarError("Error", "Debe ingresar una hora");
            return false;
        }

        String hora = txtHora.getText().trim();
        if (!ValidadorUtil.esHoraValida(hora)) {
            AlertasUtil.mostrarError("Error", "Hora inválida (formato HH:mm)");
            return false;
        }

        if (cmbTipo == null || cmbTipo.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar el tipo de fichaje");
            return false;
        }

        // Validar que la fecha y hora no sean futuras
        try {
            LocalDateTime fechaHora = LocalDateTime.of(
                    dpFecha.getValue(),
                    LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"))
            );

            if (fechaHora.isAfter(LocalDateTime.now())) {
                AlertasUtil.mostrarError("Error", "La fecha y hora no pueden ser futuras");
                return false;
            }
        } catch (Exception e) {
            AlertasUtil.mostrarError("Error", "Error al validar fecha y hora");
            return false;
        }

        return true;
    }

    /**
     * Maneja el evento del botón "Obtener Clima"
     */
    @FXML
    private void handleObtenerClima() {
        obtenerClimaAutomaticamente();
    }

    /**
     * Obtiene el clima automáticamente de la API
     */
    private void obtenerClimaAutomaticamente() {
        if (txtClima != null) {
            txtClima.setText("Obteniendo clima...");
        }

        try {
            String clima = climaService.obtenerClima();
            if (txtClima != null) {
                txtClima.setText(clima);
            }
        } catch (Exception e) {
            System.err.println("Error al obtener clima: " + e.getMessage());
            if (txtClima != null) {
                txtClima.setText("No disponible");
            }
        }
    }

    /**
     * Maneja el evento del botón "Guardar"
     */
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

            // El clima siempre es el que está en el campo (de la API o "No disponible")
            String clima = txtClima != null && txtClima.getText() != null
                    ? txtClima.getText().trim()
                    : "No disponible";

            String notas = txtNotas != null && txtNotas.getText() != null
                    ? txtNotas.getText().trim()
                    : "";

            if (fichajeEditar == null) {
                // CREAR NUEVO FICHAJE
                Fichaje nuevo = new Fichaje();
                nuevo.setTrabajador(trabajador);
                nuevo.setFechaHora(fechaHora);
                nuevo.setTipo(tipo);
                nuevo.setClima(clima.equals("No disponible") ? null : clima);
                nuevo.setNotas(notas.isEmpty() ? null : notas);

                if (fichajeDAO.guardar(nuevo)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo guardar el fichaje");
                }
            } else {
                // EDITAR FICHAJE EXISTENTE
                fichajeEditar.setFechaHora(fechaHora);
                fichajeEditar.setTipo(tipo);
                fichajeEditar.setClima(clima.equals("No disponible") ? null : clima);
                fichajeEditar.setNotas(notas.isEmpty() ? null : notas);

                if (fichajeDAO.actualizar(fichajeEditar)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo actualizar el fichaje");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al guardar fichaje: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al guardar: " + e.getMessage());
        }
    }

    /**
     * Maneja el evento del botón "Cancelar"
     */
    @FXML
    private void handleCancelar() {
        guardado = false;
        cerrarDialogo();
    }

    /**
     * Cierra el diálogo
     */
    private void cerrarDialogo() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Indica si se guardó el fichaje correctamente
     */
    public boolean isGuardado() {
        return guardado;
    }
}