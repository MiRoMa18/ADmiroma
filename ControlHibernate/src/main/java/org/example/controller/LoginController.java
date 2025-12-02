package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.TrabajadorDAO;
import org.example.model.Trabajador;

import java.util.Optional;

public class LoginController {

    @FXML
    private TextField txtNumeroTarjeta;

    @FXML
    private PasswordField txtPin;

    @FXML
    private Button btnLogin;

    @FXML
    private Label lblMensaje;

    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    @FXML
    private void initialize() {
        txtNumeroTarjeta.textProperty().addListener((obs, old, nuevo) -> lblMensaje.setText(""));
        txtPin.textProperty().addListener((obs, old, nuevo) -> lblMensaje.setText(""));
    }

    @FXML
    private void handleLogin() {
        String numeroTarjeta = txtNumeroTarjeta.getText().trim();
        String pin = txtPin.getText().trim();

        // Validar campos vacíos
        if (numeroTarjeta.isEmpty() || pin.isEmpty()) {
            mostrarError("Por favor, completa todos los campos");
            return;
        }

        // Autenticar
        Optional<Trabajador> trabajadorOpt = trabajadorDAO.autenticar(numeroTarjeta, pin);

        if (trabajadorOpt.isPresent()) {
            Trabajador trabajador = trabajadorOpt.get();

            // Cargar Dashboard
            cargarDashboard(trabajador);

        } else {
            mostrarError("Número de tarjeta o PIN incorrecto");
        }
    }

    private void cargarDashboard(Trabajador trabajador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            // Obtener el controlador y pasarle el trabajador
            DashboardController controller = loader.getController();
            controller.inicializar(trabajador);

            // Cambiar de escena
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");


        } catch (Exception e) {
            System.err.println("❌ Error al cargar dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarError("Error al cargar el panel principal");
        }
    }

    private void mostrarError(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void mostrarExito(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }
}