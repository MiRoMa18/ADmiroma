package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.model.Fichaje;
import org.example.model.TipoFichaje;
import org.example.model.Trabajador;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class DialogoFichajeController {

    @FXML private ComboBox<Trabajador> cbEmpleado;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<String> cbTipo;
    @FXML private TextField txtClima;
    @FXML private TextArea taNotas;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblTitulo;

    private FichajeDAO fichajeDAO = new FichajeDAO();
    private Integer fichajeIdEditar = null; // null = crear nuevo, not null = editar
    private Fichaje fichajeActual = null;

    /**
     * Inicializar el diálogo
     * @param fichajeId ID del fichaje a editar (null para crear nuevo)
     * @param trabajadores Lista de todos los trabajadores
     */
    public void inicializar(Integer fichajeId, List<Trabajador> trabajadores) {
        this.fichajeIdEditar = fichajeId;

        // Configurar ComboBox de empleados
        cbEmpleado.setItems(FXCollections.observableArrayList(trabajadores));
        cbEmpleado.setConverter(new javafx.util.StringConverter<Trabajador>() {
            @Override
            public String toString(Trabajador trabajador) {
                return trabajador == null ? "" : trabajador.getNombreCompleto() + " (" + trabajador.getNumeroTarjeta() + ")";
            }

            @Override
            public Trabajador fromString(String string) {
                return null;
            }
        });

        // Configurar ComboBox de tipo
        cbTipo.setItems(FXCollections.observableArrayList("ENTRADA", "SALIDA"));

        // Placeholder para hora
        txtHora.setPromptText("HH:mm (ej: 08:30)");

        if (fichajeId == null) {
            // MODO CREAR
            lblTitulo.setText("➕ Crear Nuevo Fichaje");
            dpFecha.setValue(LocalDate.now());
            txtHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            // MODO EDITAR
            lblTitulo.setText("✏️ Editar Fichaje");
            cargarFichaje(fichajeId);
        }
    }

    private void cargarFichaje(Integer fichajeId) {
        Optional<Fichaje> fichajeOpt = fichajeDAO.buscarPorId(fichajeId);

        if (fichajeOpt.isEmpty()) {
            mostrarError("Error", "No se encontró el fichaje con ID " + fichajeId);
            cerrarDialogo();
            return;
        }

        fichajeActual = fichajeOpt.get();

        // Rellenar campos con datos del fichaje
        cbEmpleado.setValue(fichajeActual.getTrabajador());
        dpFecha.setValue(fichajeActual.getFechaHora().toLocalDate());
        txtHora.setText(fichajeActual.getFechaHora().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        cbTipo.setValue(fichajeActual.getTipo().name());
        txtClima.setText(fichajeActual.getClima() != null ? fichajeActual.getClima() : "");
        taNotas.setText(fichajeActual.getNotas() != null ? fichajeActual.getNotas() : "");
    }

    @FXML
    private void handleGuardar() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }

        try {
            // Obtener valores
            Trabajador trabajador = cbEmpleado.getValue();
            LocalDate fecha = dpFecha.getValue();
            LocalTime hora = LocalTime.parse(txtHora.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            TipoFichaje tipo = TipoFichaje.valueOf(cbTipo.getValue());
            String clima = txtClima.getText().trim().isEmpty() ? null : txtClima.getText().trim();
            String notas = taNotas.getText().trim().isEmpty() ? null : taNotas.getText().trim();

            // Validar que no sea fecha futura
            if (fechaHora.isAfter(LocalDateTime.now())) {
                mostrarAlerta("Fecha inválida", "No se puede crear un fichaje con fecha futura");
                return;
            }

            // Verificar duplicados
            Integer idExcluir = fichajeIdEditar; // Para edición, excluir el propio ID
            if (fichajeDAO.existeDuplicado(trabajador.getId(), fechaHora, idExcluir)) {
                mostrarAlerta("Fichaje duplicado",
                        "Ya existe un fichaje para este empleado en la misma fecha y hora exacta");
                return;
            }

            boolean exito;

            if (fichajeIdEditar == null) {
                // CREAR NUEVO
                Fichaje nuevoFichaje = new Fichaje();
                nuevoFichaje.setTrabajador(trabajador);
                nuevoFichaje.setFechaHora(fechaHora);
                nuevoFichaje.setTipo(tipo);
                nuevoFichaje.setClima(clima);
                nuevoFichaje.setNotas(notas);

                exito = fichajeDAO.guardar(nuevoFichaje);

            } else {
                // ACTUALIZAR EXISTENTE
                fichajeActual.setTrabajador(trabajador);
                fichajeActual.setFechaHora(fechaHora);
                fichajeActual.setTipo(tipo);
                fichajeActual.setClima(clima);
                fichajeActual.setNotas(notas);

                exito = fichajeDAO.actualizar(fichajeActual);
            }

            if (exito) {
                mostrarInformacion("Éxito",
                        fichajeIdEditar == null ? "Fichaje creado correctamente" : "Fichaje actualizado correctamente");
                cerrarDialogo();
            } else {
                mostrarError("Error", "No se pudo guardar el fichaje");
            }

        } catch (DateTimeParseException e) {
            mostrarAlerta("Hora inválida", "El formato de hora debe ser HH:mm (ej: 08:30)");
        } catch (Exception e) {
            System.err.println("Error al guardar fichaje: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error", "Ocurrió un error al guardar: " + e.getMessage());
        }
    }

    private boolean validarCampos() {
        // Validar empleado
        if (cbEmpleado.getValue() == null) {
            mostrarAlerta("Campo requerido", "Debes seleccionar un empleado");
            cbEmpleado.requestFocus();
            return false;
        }

        // Validar fecha
        if (dpFecha.getValue() == null) {
            mostrarAlerta("Campo requerido", "Debes seleccionar una fecha");
            dpFecha.requestFocus();
            return false;
        }

        // Validar hora
        if (txtHora.getText().trim().isEmpty()) {
            mostrarAlerta("Campo requerido", "Debes ingresar una hora");
            txtHora.requestFocus();
            return false;
        }

        // Validar formato de hora
        try {
            LocalTime.parse(txtHora.getText(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            mostrarAlerta("Formato inválido", "El formato de hora debe ser HH:mm (ej: 08:30)");
            txtHora.requestFocus();
            return false;
        }

        // Validar tipo
        if (cbTipo.getValue() == null) {
            mostrarAlerta("Campo requerido", "Debes seleccionar un tipo de fichaje");
            cbTipo.requestFocus();
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        cerrarDialogo();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}