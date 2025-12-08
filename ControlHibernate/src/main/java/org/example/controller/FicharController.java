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

/**
 * Controlador para la vista de fichar (entrada/salida).
 * Registra el fichaje con la fecha y hora EXACTA del momento del clic.
 * ACTUALIZADO: Sin edición de fecha/hora - solo muestra la hora actual en tiempo real.
 */
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

    /**
     * Inicializa el controlador con el trabajador actual.
     *
     * @param trabajador Usuario que va a fichar
     */
    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        System.out.println("🕒 FicharController inicializado para: " + trabajador.getNombreCompleto());

        // Mostrar nombre
        lblNombreTrabajador.setText(trabajador.getNombreCompleto());

        // Iniciar reloj en tiempo real
        iniciarReloj();

        // Detectar próxima acción (ENTRADA o SALIDA)
        detectarProximaAccion();

        // Obtener clima
        obtenerClima();
    }

    /**
     * Inicia un reloj que actualiza la fecha y hora cada segundo.
     */
    private void iniciarReloj() {
        // Formatear fecha y hora en español
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEEE, dd 'de' MMMM 'de' yyyy - HH:mm:ss",
                new Locale("es", "ES")
        );

        // Actualizar inmediatamente
        actualizarFechaHora(formatter);

        // Crear Timeline que se ejecuta cada 1 segundo
        relojActualizador = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            actualizarFechaHora(formatter);
        }));

        relojActualizador.setCycleCount(Animation.INDEFINITE);
        relojActualizador.play();

        System.out.println("⏰ Reloj en tiempo real iniciado");
    }

    /**
     * Actualiza el label con la fecha y hora actual.
     */
    private void actualizarFechaHora(DateTimeFormatter formatter) {
        LocalDateTime ahora = LocalDateTime.now();
        String fechaHoraFormateada = ahora.format(formatter);

        // Capitalizar primera letra
        fechaHoraFormateada = fechaHoraFormateada.substring(0, 1).toUpperCase()
                + fechaHoraFormateada.substring(1);

        lblFechaHoraActual.setText(fechaHoraFormateada);
    }

    /**
     * Detecta si la próxima acción debe ser ENTRADA o SALIDA
     * basándose en el último fichaje del trabajador.
     */
    private void detectarProximaAccion() {
        Optional<Fichaje> ultimoFichaje = fichajeDAO.obtenerUltimoFichaje(trabajadorActual.getId());

        if (ultimoFichaje.isPresent()) {
            TipoFichaje ultimoTipo = ultimoFichaje.get().getTipo();

            // Si el último fue ENTRADA → ahora toca SALIDA
            // Si el último fue SALIDA → ahora toca ENTRADA
            proximaAccion = ultimoTipo == TipoFichaje.ENTRADA
                    ? TipoFichaje.SALIDA
                    : TipoFichaje.ENTRADA;

            System.out.println("ℹ️  Último fichaje: " + ultimoTipo + " → Próxima acción: " + proximaAccion);

        } else {
            // No hay fichajes previos → primera vez → ENTRADA
            proximaAccion = TipoFichaje.ENTRADA;
            System.out.println("ℹ️  No hay fichajes previos → Próxima acción: ENTRADA");
        }

        // Actualizar etiqueta
        String emoji = proximaAccion == TipoFichaje.ENTRADA ? "🟢" : "🔴";
        lblTipoAccion.setText(emoji + " " + proximaAccion);

        // Cambiar estilo del botón
        btnFichar.getStyleClass().removeAll("btn-entrada", "btn-salida");
        btnFichar.getStyleClass().add(
                proximaAccion == TipoFichaje.ENTRADA ? "btn-entrada" : "btn-salida"
        );
    }

    /**
     * Obtiene el clima actual usando el servicio de clima.
     * Se ejecuta en un thread separado para no bloquear la UI.
     */
    private void obtenerClima() {
        // Mostrar mensaje de carga
        lblClima.setText("🌤️ Cargando clima...");

        // Ejecutar consulta en thread separado
        new Thread(() -> {
            try {
                String clima = climaService.obtenerClimaConDetalles();
                climaActual = clima.split(" \\(")[0]; // Solo la descripción sin temperatura

                // Actualizar UI en el thread de JavaFX
                javafx.application.Platform.runLater(() -> {
                    lblClima.setText("🌤️ Clima: " + clima);
                });

                System.out.println("✅ Clima obtenido: " + clima);

            } catch (Exception e) {
                climaActual = "No disponible";

                javafx.application.Platform.runLater(() -> {
                    lblClima.setText("🌤️ Clima no disponible");
                });

                System.err.println("⚠️ No se pudo obtener el clima: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Maneja el evento de click en el botón Fichar.
     * Registra el fichaje con la fecha y hora EXACTA del momento del clic.
     */
    @FXML
    private void handleFichar() {
        System.out.println("🕒 Fichando: " + proximaAccion);

        try {
            // Obtener fecha y hora EXACTA del momento actual
            LocalDateTime fechaHoraActual = LocalDateTime.now();

            // Crear fichaje
            Fichaje fichaje = new Fichaje();
            fichaje.setTrabajador(trabajadorActual);
            fichaje.setFechaHora(fechaHoraActual);
            fichaje.setTipo(proximaAccion);
            fichaje.setClima(climaActual);
            fichaje.setNotas(txtNotas.getText().trim());

            System.out.println("📋 Fichaje a registrar:");
            System.out.println("   Trabajador: " + trabajadorActual.getNombreCompleto());
            System.out.println("   Fecha/Hora: " + fechaHoraActual);
            System.out.println("   Tipo: " + proximaAccion);
            System.out.println("   Clima: " + climaActual);

            // Guardar en BD
            boolean exito = fichajeDAO.guardar(fichaje);

            if (exito) {
                System.out.println("✅ Fichaje registrado correctamente");

                // Detener el reloj antes de cambiar de vista
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

                // Volver al dashboard
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

    /**
     * Vuelve al dashboard.
     */
    @FXML
    private void handleVolver() {
        // Detener el reloj antes de cambiar de vista
        if (relojActualizador != null) {
            relojActualizador.stop();
            System.out.println("⏰ Reloj detenido");
        }

        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }
}