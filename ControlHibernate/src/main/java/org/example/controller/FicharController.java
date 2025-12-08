package org.example.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.example.dao.FichajeDAO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.model.enums.TipoFichaje;
import org.example.service.ClimaService;
import org.example.util.AlertasUtil;
import org.example.util.NavegacionUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class FicharController {

    @FXML
    private Label lblNombreTrabajador;

    @FXML
    private Label lblTipoAccion;

    @FXML
    private Label lblFechaHoraActual;

    @FXML
    private TextArea txtNotas;

    @FXML
    private Label lblClima;

    @FXML
    private Button btnFichar;

    @FXML
    private Button btnVolver;

    private Trabajador trabajadorActual;
    private final FichajeDAO fichajeDAO = new FichajeDAO();
    private final ClimaService climaService = new ClimaService();

    private TipoFichaje proximaAccion;
    private String climaActual;
    private Timeline relojActualizador;


    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        lblNombreTrabajador.setText(trabajador.getNombreCompleto());

        iniciarReloj();

        detectarProximaAccion();

        obtenerClima();
    }

    private void iniciarReloj() {
        // Formatear fecha y hora en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEEE, dd 'de' MMMM 'de' yyyy - HH:mm:ss",
                new Locale("es", "ES")
        );

        actualizarFechaHora(formatter);

        relojActualizador = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            actualizarFechaHora(formatter);
        }));

        relojActualizador.setCycleCount(Animation.INDEFINITE);
        relojActualizador.play();
    }

    private void actualizarFechaHora(DateTimeFormatter formatter) {
        LocalDateTime ahora = LocalDateTime.now();
        String fechaHoraFormateada = ahora.format(formatter);

        // Mayúscula primera letra
        fechaHoraFormateada = fechaHoraFormateada.substring(0, 1).toUpperCase()
                + fechaHoraFormateada.substring(1);

        lblFechaHoraActual.setText(fechaHoraFormateada);
    }

    private void detectarProximaAccion() {
        Optional<Fichaje> ultimoFichaje = fichajeDAO.obtenerUltimoFichaje(trabajadorActual.getId());

        if (ultimoFichaje.isPresent()) {
            TipoFichaje ultimoTipo = ultimoFichaje.get().getTipo();

            proximaAccion = ultimoTipo == TipoFichaje.ENTRADA
                    ? TipoFichaje.SALIDA
                    : TipoFichaje.ENTRADA;


        } else {
            proximaAccion = TipoFichaje.ENTRADA;
        }

        String emoji = proximaAccion == TipoFichaje.ENTRADA ? "🟢" : "🔴";
        lblTipoAccion.setText(emoji + " " + proximaAccion);

        btnFichar.getStyleClass().removeAll("btn-entrada", "btn-salida");
        btnFichar.getStyleClass().add(
                proximaAccion == TipoFichaje.ENTRADA ? "btn-entrada" : "btn-salida"
        );
    }

    private void obtenerClima() {
        lblClima.setText("🌤️ Cargando clima...");

        new Thread(() -> {
            try {
                String clima = climaService.obtenerClimaConDetalles();
                climaActual = clima.split(" \\(")[0];

                javafx.application.Platform.runLater(() -> {
                    lblClima.setText("🌤️ Clima: " + clima);
                });

            } catch (Exception e) {
                climaActual = "No disponible";

                javafx.application.Platform.runLater(() -> {
                    lblClima.setText("🌤️ Clima no disponible");
                });

                System.err.println("⚠️ No se pudo obtener el clima: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleFichar() {
        try {
            LocalDateTime fechaHoraActual = LocalDateTime.now();

            Fichaje fichaje = new Fichaje();
            fichaje.setTrabajador(trabajadorActual);
            fichaje.setFechaHora(fechaHoraActual);
            fichaje.setTipo(proximaAccion);
            fichaje.setClima(climaActual);
            fichaje.setNotas(txtNotas.getText().trim());

            boolean exito = fichajeDAO.guardar(fichaje);

            if (exito) {
                if (relojActualizador != null) {
                    relojActualizador.stop();
                }

                String mensaje = proximaAccion == TipoFichaje.ENTRADA
                        ? "¡Buen día de trabajo!"
                        : "¡Hasta mañana!";

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                String horaFormateada = fechaHoraActual.format(formatter);

                AlertasUtil.mostrarExito(
                        "Fichaje registrado",
                        mensaje + "\n\n" + proximaAccion + " registrada a las " + horaFormateada
                );
                NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);

            } else {
                AlertasUtil.mostrarError(
                        "Error",
                        "No se pudo registrar el fichaje"
                );
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al fichar: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al procesar el fichaje: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        if (relojActualizador != null) {
            relojActualizador.stop();
            System.out.println("⏰ Reloj detenido");
        }

        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }
}