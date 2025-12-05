package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.TrabajadorDAO;
import org.example.model.Rol;
import org.example.model.Trabajador;

import java.time.LocalDate;
import java.util.Optional;

public class DialogoTrabajadorController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtNumeroTarjeta;
    @FXML private PasswordField txtPin;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<Rol> cbRol;
    @FXML private DatePicker dpFechaAlta;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private Trabajador trabajadorActual; // null = nuevo, no-null = editar
    private boolean guardado = false;
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        // Configurar ComboBox de roles
        cbRol.setItems(FXCollections.observableArrayList(Rol.values()));

        if (trabajador == null) {
            // MODO CREAR
            lblTitulo.setText("➕ Nuevo Trabajador");
            cbRol.setValue(Rol.TRABAJADOR);
            dpFechaAlta.setValue(LocalDate.now());
        } else {
            // MODO EDITAR
            lblTitulo.setText("✏️ Editar Trabajador");
            cargarDatos(trabajador);
        }
    }

    private void cargarDatos(Trabajador trabajador) {
        txtNumeroTarjeta.setText(trabajador.getNumeroTarjeta());
        txtPin.setText(trabajador.getPin());
        txtNombre.setText(trabajador.getNombre());
        txtApellidos.setText(trabajador.getApellidos());
        txtEmail.setText(trabajador.getEmail());
        cbRol.setValue(trabajador.getRol());
        dpFechaAlta.setValue(trabajador.getFechaAlta());

        // Deshabilitar número de tarjeta en edición (es la clave única)
        txtNumeroTarjeta.setDisable(true);
        txtNumeroTarjeta.setStyle("-fx-opacity: 0.6;");
    }

    @FXML
    private void handleGuardar() {
        // Validar campos
        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String pin = txtPin.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellidos = txtApellidos.getText().trim();
        String email = txtEmail.getText().trim();
        Rol rol = cbRol.getValue();
        LocalDate fechaAlta = dpFechaAlta.getValue();

        // Validaciones
        if (numeroTarjeta.isEmpty()) {
            mostrarAlerta("Campo obligatorio", "El número de tarjeta es obligatorio");
            txtNumeroTarjeta.requestFocus();
            return;
        }

        if (pin.isEmpty() || pin.length() < 4) {
            mostrarAlerta("PIN inválido", "El PIN debe tener al menos 4 caracteres");
            txtPin.requestFocus();
            return;
        }

        if (nombre.isEmpty()) {
            mostrarAlerta("Campo obligatorio", "El nombre es obligatorio");
            txtNombre.requestFocus();
            return;
        }

        if (rol == null) {
            mostrarAlerta("Campo obligatorio", "Debes seleccionar un rol");
            cbRol.requestFocus();
            return;
        }

        // Validar email si está presente
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mostrarAlerta("Email inválido", "El formato del email no es válido");
            txtEmail.requestFocus();
            return;
        }

        // Validar número de tarjeta único (solo en modo CREAR)
        if (trabajadorActual == null) {
            Optional<Trabajador> existente = trabajadorDAO.buscarPorNumeroTarjeta(numeroTarjeta);
            if (existente.isPresent()) {
                mostrarAlerta("Tarjeta duplicada", "Ya existe un trabajador con ese número de tarjeta");
                txtNumeroTarjeta.requestFocus();
                return;
            }
        }

        // Guardar
        boolean exito;
        if (trabajadorActual == null) {
            // CREAR NUEVO
            Trabajador nuevo = new Trabajador();
            nuevo.setNumeroTarjeta(numeroTarjeta);
            nuevo.setPin(pin);
            nuevo.setNombre(nombre);
            nuevo.setApellidos(apellidos.isEmpty() ? null : apellidos);
            nuevo.setEmail(email.isEmpty() ? null : email);
            nuevo.setRol(rol);
            nuevo.setFechaAlta(fechaAlta);

            exito = trabajadorDAO.crear(nuevo);
        } else {
            // ACTUALIZAR EXISTENTE
            trabajadorActual.setPin(pin);
            trabajadorActual.setNombre(nombre);
            trabajadorActual.setApellidos(apellidos.isEmpty() ? null : apellidos);
            trabajadorActual.setEmail(email.isEmpty() ? null : email);
            trabajadorActual.setRol(rol);
            trabajadorActual.setFechaAlta(fechaAlta);

            exito = trabajadorDAO.actualizar(trabajadorActual);
        }

        if (exito) {
            guardado = true;

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Éxito");
            info.setHeaderText(null);
            info.setContentText("✅ Trabajador " + (trabajadorActual == null ? "creado" : "actualizado") + " correctamente");
            info.showAndWait();

            cerrarDialogo();
        } else {
            mostrarAlerta("Error", "❌ Error al guardar el trabajador. Inténtalo de nuevo.");
        }
    }

    @FXML
    private void handleCancelar() {
        // Confirmación si hay cambios
        if (hayCambios()) {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar cancelación");
            confirmacion.setHeaderText("Hay cambios sin guardar");
            confirmacion.setContentText("¿Estás seguro de que deseas cancelar?");

            Optional<ButtonType> result = confirmacion.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }

        cerrarDialogo();
    }

    private boolean hayCambios() {
        // Si es nuevo, cualquier texto escrito es un cambio
        if (trabajadorActual == null) {
            return !txtNumeroTarjeta.getText().trim().isEmpty() ||
                    !txtPin.getText().trim().isEmpty() ||
                    !txtNombre.getText().trim().isEmpty() ||
                    !txtApellidos.getText().trim().isEmpty() ||
                    !txtEmail.getText().trim().isEmpty();
        }

        // Si es edición, comparar con valores originales
        return !txtPin.getText().equals(trabajadorActual.getPin()) ||
                !txtNombre.getText().equals(trabajadorActual.getNombre()) ||
                !txtApellidos.getText().equals(trabajadorActual.getApellidos() != null ? trabajadorActual.getApellidos() : "") ||
                !txtEmail.getText().equals(trabajadorActual.getEmail() != null ? trabajadorActual.getEmail() : "") ||
                cbRol.getValue() != trabajadorActual.getRol() ||
                !dpFechaAlta.getValue().equals(trabajadorActual.getFechaAlta());
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public boolean isGuardado() {
        return guardado;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}