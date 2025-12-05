package org.example.controller.trabajador;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.model.Fichaje;
import org.example.model.TipoFichaje;
import org.example.model.Trabajador;
import org.example.service.ClimaService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class FicharController {

    @FXML private Label lblAccion;
    @FXML private Label lblFechaHora;
    @FXML private Label lblClima;
    @FXML private TextArea taNotas;
    @FXML private Button btnFichar;
    @FXML private Button btnCancelar;
    @FXML private ProgressIndicator progressClima;

    private Trabajador trabajadorActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();
    private ClimaService climaService = new ClimaService();
    private TipoFichaje accionDetectada;
    private String climaActual = "Desconocido";

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;
        detectarAccion();
        actualizarFechaHora();
        cargarClimaAsync();
    }

    private void detectarAccion() {
        Optional<Fichaje> ultimo = fichajeDAO.obtenerUltimoFichaje(trabajadorActual.getId());
        if (ultimo.isEmpty()) {
            accionDetectada = TipoFichaje.ENTRADA;
        } else {
            TipoFichaje tipoUltimo = ultimo.get().getTipo();
            accionDetectada = tipoUltimo == TipoFichaje.SALIDA ? TipoFichaje.ENTRADA : TipoFichaje.SALIDA;
        }

        String emoji = accionDetectada == TipoFichaje.ENTRADA ? "⬇️" : "⬆️";
        lblAccion.setText("Vas a fichar: " + emoji + " " + accionDetectada.name());
    }

    private void actualizarFechaHora() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFechaHora.setText("Fecha y hora: " + LocalDateTime.now().format(fmt));
    }

    private void cargarClimaAsync() {
        if (progressClima != null) {
            progressClima.setVisible(true);
        }
        lblClima.setText("Obteniendo clima...");

        new Thread(() -> {
            String clima = climaService.obtenerClimaConDetalles();
            climaActual = clima.split(" \\(")[0]; // Solo la descripción sin temperatura

            Platform.runLater(() -> {
                lblClima.setText("🌤️ Clima: " + clima);
                if (progressClima != null) {
                    progressClima.setVisible(false);
                }
            });
        }).start();
    }

    @FXML
    private void handleFichar() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirmar fichaje");
        dialog.setHeaderText(null);

        ButtonType confirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmar, cancelar);

        String resumen = "Acción: " + accionDetectada.name() + "\n"
                + "Fecha/Hora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n"
                + "Clima: " + climaActual + "\n\n"
                + "Nota (puedes editarla):";

        TextArea notaDialog = new TextArea(taNotas.getText());
        notaDialog.setPrefRowCount(4);
        VBox content = new VBox(new Label(resumen), notaDialog);
        content.setSpacing(8);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == confirmar) {
            String nota = notaDialog.getText();

            Fichaje f = new Fichaje();
            f.setFechaHora(LocalDateTime.now());
            f.setTipo(accionDetectada);
            f.setNotas(nota != null && !nota.isBlank() ? nota : null);
            f.setClima(climaActual);
            f.setTrabajador(trabajadorActual);

            boolean ok = fichajeDAO.guardar(f);
            if (ok) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Fichaje guardado");
                info.setHeaderText(null);
                info.setContentText("Fichaje registrado correctamente.\nClima: " + climaActual);
                info.showAndWait();

                volverAlDashboard();
            } else {
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Error");
                err.setHeaderText(null);
                err.setContentText("Error al guardar el fichaje. Intenta de nuevo.");
                err.showAndWait();
            }
        }
    }

    @FXML
    private void handleCancelar() {
        volverAlDashboard();
    }

    private void volverAlDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            org.example.controller.DashboardController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}