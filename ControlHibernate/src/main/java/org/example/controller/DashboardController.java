package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.Rol;
import org.example.model.Trabajador;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private VBox menuAdmin;

    @FXML
    private VBox menuComun;

    // Botones ADMIN
    @FXML
    private Button btnVerTodosFichajes;

    @FXML
    private Button btnEstadisticasGlobales;

    @FXML
    private Button btnCrudTrabajadores;

    // Botones COMUNES
    @FXML
    private Button btnMisFichajes;

    @FXML
    private Button btnMisEstadisticas;

    @FXML
    private Button btnMisDatos;

    @FXML
    private Button btnFichar;

    @FXML
    private Button btnCerrarSesion;

    private Trabajador trabajadorActual;

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        lblBienvenida.setText("¡Bienvenido, " + trabajador.getNombre() + "!");
        lblRol.setText("Rol: " + trabajador.getRol());

        if (trabajador.getRol() == Rol.ADMIN) {
            menuAdmin.setVisible(true);
            menuAdmin.setManaged(true);
            System.out.println("✅ Dashboard cargado para ADMIN");
        } else {
            menuAdmin.setVisible(false);
            menuAdmin.setManaged(false);
            System.out.println("✅ Dashboard cargado para TRABAJADOR");
        }
    }

    // ========== MÉTODOS ADMIN ==========

    @FXML
    private void handleVerTodosFichajes() {
        System.out.println("🔘 Admin: Ver todos los fichajes (CRUD)");
        // TODO: Cargar vista de tabla fichajes
        mostrarMensajeTemporal("Vista de Fichajes (próximamente)");
    }

    @FXML
    private void handleEstadisticasGlobales() {
        System.out.println("🔘 Admin: Ver estadísticas globales");
        // TODO: Cargar vista estadísticas globales
        mostrarMensajeTemporal("Estadísticas Globales (próximamente)");
    }

    @FXML
    private void handleCrudTrabajadores() {
        System.out.println("🔘 Admin: CRUD Trabajadores");
        // TODO: Cargar vista CRUD trabajadores
        mostrarMensajeTemporal("CRUD Trabajadores (próximamente)");
    }

    // ========== MÉTODOS COMUNES ==========

    @FXML
    private void handleMisFichajes() {
        System.out.println("🔘 Usuario: Ver mis fichajes - ID: " + trabajadorActual.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/trabajador/mis_fichajes.fxml"));
            Parent root = loader.load();

            org.example.controller.trabajador.MisFichajesController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            Stage stage = (Stage) btnMisFichajes.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 650));
            stage.setTitle("Control Horario - Mis Fichajes");

            System.out.println("✅ Vista 'Mis Fichajes' cargada");

        } catch (IOException e) {
            System.err.println("❌ Error al cargar Mis Fichajes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMisEstadisticas() {
        System.out.println("🔘 Usuario: Ver mis estadísticas - ID: " + trabajadorActual.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/trabajador/mis_estadisticas.fxml"));
            Parent root = loader.load();

            org.example.controller.trabajador.MisEstadisticasController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            Stage stage = (Stage) btnMisEstadisticas.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 750));
            stage.setTitle("Control Horario - Mis Estadísticas");

            System.out.println("✅ Vista 'Mis Estadísticas' cargada");

        } catch (IOException e) {
            System.err.println("❌ Error al cargar Mis Estadísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMisDatos() {
        System.out.println("🔘 Usuario: Ver mis datos");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/trabajador/mis_datos.fxml"));
            Parent root = loader.load();

            // Pasar trabajador al controlador
            org.example.controller.trabajador.MisDatosController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            // Cambiar escena
            Stage stage = (Stage) btnMisDatos.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Mis Datos");

            System.out.println("✅ Vista 'Mis Datos' cargada");

        } catch (IOException e) {
            System.err.println("❌ Error al cargar Mis Datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFichar() {
        System.out.println("🔘 Usuario: Fichar - ID: " + trabajadorActual.getId());
        // TODO: Cargar vista fichar
        mostrarMensajeTemporal("Pantalla de Fichaje (próximamente)");
    }

    @FXML
    private void handleCerrarSesion() {
        System.out.println("🔘 Cerrando sesión...");
        try {
            // Volver al login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setScene(new Scene(root, 450, 550));
            stage.setTitle("Control Horario - Login");

            System.out.println("✅ Sesión cerrada, volviendo a login");

        } catch (IOException e) {
            System.err.println("❌ Error al cerrar sesión: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== UTILIDADES ==========

    private void mostrarMensajeTemporal(String mensaje) {
        // Por ahora solo muestra en consola
        // Más adelante puedes mostrar un Alert o cambiar de vista
        System.out.println("ℹ️  " + mensaje);
    }
}
