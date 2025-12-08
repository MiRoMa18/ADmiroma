package org.example.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.TrabajadorDAO;
import org.example.model.entity.Trabajador;
import org.example.model.enums.Rol;
import org.example.util.AlertasUtil;
import org.example.util.ValidadorUtil;

import java.time.LocalDate;

public class DialogoTrabajadorController {

    @FXML private TextField txtNumeroTarjeta;
    @FXML private PasswordField txtPin;
    @FXML private PasswordField txtPinConfirmar;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtEmail;
    @FXML private ComboBox<Rol> cmbRol;
    @FXML private DatePicker dpFechaAlta;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Label lblPinInfo;

    private Trabajador trabajadorEditar = null;
    private boolean guardado = false;
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    public void inicializarNuevo() {
        this.trabajadorEditar = null;
        configurarComponentes();

        if (dpFechaAlta != null) {
            dpFechaAlta.setValue(LocalDate.now());
        }

        if (cmbRol != null) {
            cmbRol.setValue(Rol.TRABAJADOR);
        }

        if (lblPinInfo != null) {
            lblPinInfo.setText("* PIN requerido (4-10 dígitos)");
            lblPinInfo.setStyle("-fx-text-fill: red;");
        }
    }

    public void inicializarEditar(Trabajador trabajador) {
        this.trabajadorEditar = trabajador;

        configurarComponentes();

        if (txtNumeroTarjeta != null) {
            txtNumeroTarjeta.setText(trabajador.getNumeroTarjeta());
        }

        if (txtNombre != null) {
            txtNombre.setText(trabajador.getNombre());
        }

        if (txtApellidos != null) {
            txtApellidos.setText(trabajador.getApellidos());
        }

        if (txtEmail != null) {
            txtEmail.setText(trabajador.getEmail() != null ? trabajador.getEmail() : "");
        }

        if (cmbRol != null) {
            cmbRol.setValue(trabajador.getRol());
        }

        if (dpFechaAlta != null) {
            dpFechaAlta.setValue(trabajador.getFechaAlta());
        }

        if (lblPinInfo != null) {
            lblPinInfo.setText("Dejar vacío para mantener el PIN actual");
            lblPinInfo.setStyle("-fx-text-fill: gray;");
        }
    }

    private void configurarComponentes() {
        if (cmbRol != null) {
            cmbRol.getItems().setAll(Rol.values());
        }

        if (dpFechaAlta != null) {
            dpFechaAlta.setValue(LocalDate.now());
        }

        if (txtNumeroTarjeta != null) {
            txtNumeroTarjeta.setTextFormatter(crearLimitador(20));
        }

        if (txtPin != null) {
            txtPin.setTextFormatter(crearLimitador(10));
        }

        if (txtPinConfirmar != null) {
            txtPinConfirmar.setTextFormatter(crearLimitador(10));
        }

        if (txtNombre != null) {
            txtNombre.setTextFormatter(crearLimitador(50));
        }

        if (txtApellidos != null) {
            txtApellidos.setTextFormatter(crearLimitador(100));
        }

        if (txtEmail != null) {
            txtEmail.setTextFormatter(crearLimitador(100));
        }
    }

    private TextFormatter<String> crearLimitador(int maxLength) {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= maxLength) {
                return change;
            }
            return null;
        });
    }

    /**
     * Obtiene el texto de un TextField de forma segura
     */
    private String obtenerTexto(TextField campo) {
        if (campo == null || campo.getText() == null) {
            return "";
        }
        return campo.getText().trim();
    }

    /**
     * Obtiene el texto de un PasswordField de forma segura
     */
    private String obtenerTexto(PasswordField campo) {
        if (campo == null || campo.getText() == null) {
            return "";
        }
        return campo.getText().trim();
    }

    private boolean validarCampos() {
        // Validar número de tarjeta
        String numeroTarjeta = obtenerTexto(txtNumeroTarjeta);
        if (!ValidadorUtil.esNumeroTarjetaValido(numeroTarjeta)) {
            AlertasUtil.mostrarError("Error", "Número de tarjeta inválido (4-20 dígitos)");
            return false;
        }

        // Verificar si el número de tarjeta ya existe
        boolean tarjetaDuplicada = trabajadorEditar == null
                ? trabajadorDAO.existeNumeroTarjeta(numeroTarjeta)
                : trabajadorDAO.existeNumeroTarjetaExcluyendo(numeroTarjeta, trabajadorEditar.getId());

        if (tarjetaDuplicada) {
            AlertasUtil.mostrarError("Error", "Ya existe un trabajador con ese número de tarjeta");
            return false;
        }

        // Validar PIN
        String pin = obtenerTexto(txtPin);
        String pinConfirmar = obtenerTexto(txtPinConfirmar);
        boolean pinRequerido = trabajadorEditar == null;

        if (pinRequerido && pin.isEmpty()) {
            AlertasUtil.mostrarError("Error", "Debe ingresar un PIN");
            return false;
        }

        if (!pin.isEmpty()) {
            if (!ValidadorUtil.esPinValido(pin)) {
                AlertasUtil.mostrarError("Error", "PIN inválido (4-10 dígitos)");
                return false;
            }
            if (!pin.equals(pinConfirmar)) {
                AlertasUtil.mostrarError("Error", "Los PINs no coinciden");
                return false;
            }
        }

        // Validar nombre
        String nombre = obtenerTexto(txtNombre);
        if (!ValidadorUtil.esNombreValido(nombre)) {
            AlertasUtil.mostrarError("Error", "Nombre inválido");
            return false;
        }

        // Validar apellidos
        String apellidos = obtenerTexto(txtApellidos);
        if (!ValidadorUtil.esNombreValido(apellidos)) {
            AlertasUtil.mostrarError("Error", "Apellidos inválidos");
            return false;
        }

        // Validar email (opcional)
        String email = obtenerTexto(txtEmail);
        if (!email.isEmpty() && !ValidadorUtil.esEmailValido(email)) {
            AlertasUtil.mostrarError("Error", "Email inválido");
            return false;
        }

        // Validar rol
        if (cmbRol == null || cmbRol.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar un rol");
            return false;
        }

        // Validar fecha de alta
        if (dpFechaAlta == null || dpFechaAlta.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar la fecha de alta");
            return false;
        }

        if (dpFechaAlta.getValue().isAfter(LocalDate.now())) {
            AlertasUtil.mostrarError("Error", "La fecha de alta no puede ser futura");
            return false;
        }

        return true;
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        try {
            String pin = obtenerTexto(txtPin);

            if (trabajadorEditar == null) {
                // CREAR NUEVO TRABAJADOR
                Trabajador nuevo = new Trabajador();
                nuevo.setNumeroTarjeta(obtenerTexto(txtNumeroTarjeta));
                nuevo.setPin(pin); // Guardar PIN en texto plano
                nuevo.setNombre(obtenerTexto(txtNombre));
                nuevo.setApellidos(obtenerTexto(txtApellidos));

                String email = obtenerTexto(txtEmail);
                nuevo.setEmail(email.isEmpty() ? null : email);

                nuevo.setRol(cmbRol.getValue());
                nuevo.setFechaAlta(dpFechaAlta.getValue());

                if (trabajadorDAO.guardar(nuevo)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo guardar");
                }
            } else {
                // EDITAR TRABAJADOR EXISTENTE
                trabajadorEditar.setNumeroTarjeta(obtenerTexto(txtNumeroTarjeta));

                // Solo actualizar PIN si se ingresó uno nuevo
                if (!pin.isEmpty()) {
                    trabajadorEditar.setPin(pin);
                }

                trabajadorEditar.setNombre(obtenerTexto(txtNombre));
                trabajadorEditar.setApellidos(obtenerTexto(txtApellidos));

                String email = obtenerTexto(txtEmail);
                trabajadorEditar.setEmail(email.isEmpty() ? null : email);

                trabajadorEditar.setRol(cmbRol.getValue());
                trabajadorEditar.setFechaAlta(dpFechaAlta.getValue());

                if (trabajadorDAO.actualizar(trabajadorEditar)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo actualizar");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al guardar trabajador: " + e.getMessage());
            e.printStackTrace();
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