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
        dpFechaAlta.setValue(LocalDate.now());
        cmbRol.setValue(Rol.TRABAJADOR);
        lblPinInfo.setText("* PIN requerido (4-10 dígitos)");
        lblPinInfo.setStyle("-fx-text-fill: red;");
    }

    public void inicializarEditar(Trabajador trabajador) {
        this.trabajadorEditar = trabajador;

        configurarComponentes();
        txtNumeroTarjeta.setText(trabajador.getNumeroTarjeta());
        txtNombre.setText(trabajador.getNombre());
        txtApellidos.setText(trabajador.getApellidos());
        txtEmail.setText(trabajador.getEmail());
        cmbRol.setValue(trabajador.getRol());
        dpFechaAlta.setValue(trabajador.getFechaAlta());

        lblPinInfo.setText("Dejar vacío para mantener el PIN actual");
        lblPinInfo.setStyle("-fx-text-fill: gray;");
    }

    private void configurarComponentes() {
        cmbRol.getItems().setAll(Rol.values());
        dpFechaAlta.setValue(LocalDate.now());

        txtNumeroTarjeta.setTextFormatter(crearLimitador(20));
        txtPin.setTextFormatter(crearLimitador(10));
        txtPinConfirmar.setTextFormatter(crearLimitador(10));
        txtNombre.setTextFormatter(crearLimitador(50));
        txtApellidos.setTextFormatter(crearLimitador(100));
        txtEmail.setTextFormatter(crearLimitador(100));
    }

    private TextFormatter<String> crearLimitador(int maxLength) {
        return new TextFormatter<>(change -> {
            if (change.getControlNewText().length() <= maxLength) {
                return change;
            }
            return null;
        });
    }

    private boolean validarCampos() {
        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        if (!ValidadorUtil.esNumeroTarjetaValido(numeroTarjeta)) {
            AlertasUtil.mostrarError("Error", "Número de tarjeta inválido (4-20 dígitos)");
            return false;
        }
        boolean tarjetaDuplicada = trabajadorEditar == null
                ? trabajadorDAO.existeNumeroTarjeta(numeroTarjeta)
                : trabajadorDAO.existeNumeroTarjetaExcluyendo(numeroTarjeta, trabajadorEditar.getId());

        if (tarjetaDuplicada) {
            AlertasUtil.mostrarError("Error", "Ya existe un trabajador con ese número de tarjeta");
            return false;
        }
        String pin = txtPin.getText().trim();
        String pinConfirmar = txtPinConfirmar.getText().trim();
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

        if (!ValidadorUtil.esNombreValido(txtNombre.getText().trim())) {
            AlertasUtil.mostrarError("Error", "Nombre inválido");
            return false;
        }

        if (!ValidadorUtil.esNombreValido(txtApellidos.getText().trim())) {
            AlertasUtil.mostrarError("Error", "Apellidos inválidos");
            return false;
        }

        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !ValidadorUtil.esEmailValido(email)) {
            AlertasUtil.mostrarError("Error", "Email inválido");
            return false;
        }

        if (cmbRol.getValue() == null) {
            AlertasUtil.mostrarError("Error", "Debe seleccionar un rol");
            return false;
        }

        if (dpFechaAlta.getValue() == null) {
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
            String pin = txtPin.getText().trim();

            if (trabajadorEditar == null) {
                // CREAR
                Trabajador nuevo = new Trabajador();
                nuevo.setNumeroTarjeta(txtNumeroTarjeta.getText().trim());
                nuevo.setPin(pin); // Guardar PIN en texto plano
                nuevo.setNombre(txtNombre.getText().trim());
                nuevo.setApellidos(txtApellidos.getText().trim());
                nuevo.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
                nuevo.setRol(cmbRol.getValue());
                nuevo.setFechaAlta(dpFechaAlta.getValue());

                if (trabajadorDAO.guardar(nuevo)) {
                    guardado = true;
                    cerrarDialogo();
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo guardar");
                }
            } else {
                // EDITAR
                trabajadorEditar.setNumeroTarjeta(txtNumeroTarjeta.getText().trim());

                if (!pin.isEmpty()) {
                    trabajadorEditar.setPin(pin);
                }

                trabajadorEditar.setNombre(txtNombre.getText().trim());
                trabajadorEditar.setApellidos(txtApellidos.getText().trim());
                trabajadorEditar.setEmail(txtEmail.getText().trim().isEmpty() ? null : txtEmail.getText().trim());
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