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

public class LoginController {

    @FXML
    private TextField txtNumeroTarjeta;

    @FXML
    private PasswordField txtPin;

    @FXML
    private Button btnLogin;

    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    @FXML
    public void initialize() {
        System.out.println("🔐 LoginController inicializado");

        txtNumeroTarjeta.setOnAction(event -> handleLogin());
        txtPin.setOnAction(event -> handleLogin());

        txtNumeroTarjeta.requestFocus();
    }

    @FXML
    private void handleLogin() {
        System.out.println("🔐 Intento de login");

        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String pin = txtPin.getText().trim();

        if (numeroTarjeta.isEmpty() || pin.isEmpty()) {
            AlertasUtil.mostrarError(
                    "Campos incompletos",
                    "Por favor ingrese número de tarjeta y PIN"
            );
            return;
        }

        if (!ValidadorUtil.esNumeroTarjetaValido(numeroTarjeta)) {
            AlertasUtil.mostrarError(
                    "Número de tarjeta inválido",
                    "El número de tarjeta debe tener entre 4 y 20 dígitos"
            );
            return;
        }

        if (!ValidadorUtil.esPinValido(pin)) {
            AlertasUtil.mostrarError(
                    "PIN inválido",
                    "El PIN debe tener entre 4 y 10 dígitos"
            );
            return;
        }

        Optional<Trabajador> trabajadorOpt = trabajadorDAO.autenticar(numeroTarjeta, pin);

        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();

            NavegacionUtil.abrirDashboard(btnLogin, trabajador);
        } else {
            AlertasUtil.mostrarError(
                    "Autenticación fallida",
                    "Número de tarjeta o PIN incorrectos"
            );

            txtPin.clear();
            txtNumeroTarjeta.requestFocus();
        }
    }

    @FXML
    private void handleSalir() {
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