package org.example.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.dao.TrabajadorDAO;
import org.example.model.entity.Trabajador;
import org.example.util.AlertasUtil;
import org.example.util.NavegacionUtil;
import org.example.util.ValidadorUtil;

import java.util.Optional;

/**
 * Controlador para la vista de login.
 * Autentica trabajadores mediante número de tarjeta y PIN.
 */
public class LoginController {

    @FXML
    private TextField txtNumeroTarjeta;

    @FXML
    private PasswordField txtPin;

    @FXML
    private Button btnLogin;

    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    /**
     * Inicializa el controlador.
     * Configura listeners y valores por defecto.
     */
    @FXML
    public void initialize() {
        System.out.println("🔐 LoginController inicializado");

        // Enter en cualquier campo = hacer login
        txtNumeroTarjeta.setOnAction(event -> handleLogin());
        txtPin.setOnAction(event -> handleLogin());

        // Focus automático en número de tarjeta
        txtNumeroTarjeta.requestFocus();
    }

    /**
     * Maneja el evento de click en el botón Login.
     */
    @FXML
    private void handleLogin() {
        System.out.println("🔐 Intento de login");

        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String pin = txtPin.getText().trim();

        // Validar campos vacíos
        if (numeroTarjeta.isEmpty() || pin.isEmpty()) {
            AlertasUtil.mostrarError(
                    "Campos incompletos",
                    "Por favor ingrese número de tarjeta y PIN"
            );
            return;
        }

        // Validar formato de número de tarjeta
        if (!ValidadorUtil.esNumeroTarjetaValido(numeroTarjeta)) {
            AlertasUtil.mostrarError(
                    "Número de tarjeta inválido",
                    "El número de tarjeta debe tener entre 4 y 20 dígitos"
            );
            return;
        }

        // Validar formato de PIN
        if (!ValidadorUtil.esPinValido(pin)) {
            AlertasUtil.mostrarError(
                    "PIN inválido",
                    "El PIN debe tener entre 4 y 10 dígitos"
            );
            return;
        }

        // Autenticar
        Optional<Trabajador> trabajadorOpt = trabajadorDAO.autenticar(numeroTarjeta, pin);

        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();

            System.out.println("✅ Login exitoso: " + trabajador.getNombreCompleto());

            // Navegar al dashboard
            NavegacionUtil.abrirDashboard(btnLogin, trabajador);

        } else {
            System.out.println("❌ Login fallido");

            AlertasUtil.mostrarError(
                    "Autenticación fallida",
                    "Número de tarjeta o PIN incorrectos"
            );

            // Limpiar campos
            txtPin.clear();
            txtNumeroTarjeta.requestFocus();
        }
    }

    /**
     * Maneja el evento de click en "Salir".
     */
    @FXML
    private void handleSalir() {
        System.out.println("🚪 Cerrando aplicación...");

        boolean confirmar = AlertasUtil.confirmarAccion(
                "Salir",
                "¿Está seguro que desea salir?"
        );

        if (confirmar) {
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.close();
        }
    }
}