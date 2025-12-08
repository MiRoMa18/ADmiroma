package org.example.controller.trabajador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.example.model.entity.Trabajador;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class MisDatosController {

    @FXML
    private Label lblNombreCompleto;

    @FXML
    private Label lblNumeroTarjeta;

    @FXML
    private Label lblPin;

    @FXML
    private Label lblEmail;

    @FXML
    private Label lblRol;

    @FXML
    private Label lblFechaAlta;

    @FXML
    private Button btnVolver;

    private Trabajador trabajadorActual;

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        // Mostrar datos
        lblNombreCompleto.setText(trabajador.getNombreCompleto());
        lblNumeroTarjeta.setText(trabajador.getNumeroTarjeta());
        lblPin.setText(trabajador.getPin());
        lblEmail.setText(trabajador.getEmail() != null ? trabajador.getEmail() : "No especificado");
        lblRol.setText(trabajador.getRol().toString());

        if (trabajador.getFechaAlta() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblFechaAlta.setText(trabajador.getFechaAlta().format(formatter));
        } else {
            lblFechaAlta.setText("No especificada");
        }
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            org.example.controller.DashboardController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}