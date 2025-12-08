package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.model.entity.Trabajador;
import org.example.model.enums.Rol;
import org.example.util.AlertasUtil;
import org.example.util.NavegacionUtil;

public class DashboardController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    @FXML
    private Button btnFichar;

    @FXML
    private Button btnCerrarSesion;

    @FXML
    private Button btnCrudTrabajadores;

    @FXML
    private Button btnVerTodosFichajes;

    @FXML
    private Button btnEstadisticasGlobales;

    @FXML
    private VBox menuAdmin;

    private Trabajador trabajadorActual;
    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        lblBienvenida.setText("¡Bienvenido, " + trabajador.getNombreCompleto() + "!");
        lblRol.setText("Rol: " + trabajador.getRol());

        configurarVistaPorRol();
    }

    private void configurarVistaPorRol() {
        boolean esAdmin = trabajadorActual.getRol() == Rol.ADMIN;

        // Ocultar/mostrar el menú de administrador completo
        if (menuAdmin != null) {
            menuAdmin.setVisible(esAdmin);
            menuAdmin.setManaged(esAdmin);
        }

        // Botones solo para ADMIN
        if (btnCrudTrabajadores != null) {
            btnCrudTrabajadores.setVisible(esAdmin);
            btnCrudTrabajadores.setManaged(esAdmin);
        }

        if (btnVerTodosFichajes != null) {
            btnVerTodosFichajes.setVisible(esAdmin);
            btnVerTodosFichajes.setManaged(esAdmin);
        }

        if (btnEstadisticasGlobales != null) {
            btnEstadisticasGlobales.setVisible(esAdmin);
            btnEstadisticasGlobales.setManaged(esAdmin);
        }
    }

    @FXML
    private void handleFichar() {
        cargarVista("/views/fichar.fxml", "Fichar", 500, 650);
    }

    @FXML
    private void handleMisFichajes() {
        cargarVista("/views/trabajador/mis_fichajes.fxml", "Mis Fichajes", 1000, 600);
    }

    @FXML
    private void handleMisEstadisticas() {
        cargarVista("/views/trabajador/mis_estadisticas.fxml", "Mis Estadísticas", 900, 600);
    }

    @FXML
    private void handleCrudTrabajadores() {
        cargarVista("/views/admin/crud_trabajadores.fxml", "Gestión de Trabajadores", 1000, 600);
    }

    @FXML
    private void handleVerTodosFichajes() {
        cargarVista("/views/admin/crud_fichajes.fxml", "Gestión de Fichajes", 1200, 700);
    }

    @FXML
    private void handleMisDatos() {
        AlertasUtil.mostrarInfo(
                "Mis Datos",
                "Número de Tarjeta: " + trabajadorActual.getNumeroTarjeta() + "\n" +
                        "Nombre: " + trabajadorActual.getNombreCompleto() + "\n" +
                        "Rol: " + trabajadorActual.getRol() + "\n" +
                        "Fecha de Alta: " + trabajadorActual.getFechaAlta()
        );
    }

    @FXML
    private void handleEstadisticasGlobales() {
        cargarVista("/views/admin/estadisticas_globales.fxml", "Estadísticas Globales", 1000, 600);
    }

    @FXML
    private void handleCerrarSesion() {

        boolean confirmar = AlertasUtil.confirmarAccion(
                "Cerrar Sesión",
                "¿Está seguro que desea cerrar sesión?"
        );

        if (confirmar) {
            NavegacionUtil.volverAlLogin(btnCerrarSesion);
        }
    }

    private void cargarVista(String rutaFxml, String titulo, int ancho, int alto) {
        try {
            // Verificar que el recurso existe
            if (getClass().getResource(rutaFxml) == null) {
                System.err.println("💥 ERROR: Archivo FXML no encontrado: " + rutaFxml);
                AlertasUtil.mostrarError(
                        "Archivo no encontrado",
                        "No se pudo encontrar el archivo:\n" + rutaFxml +
                                "\n\nVerifique que el archivo existe en src/main/resources" + rutaFxml
                );
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent root = loader.load();

            // Obtener el controlador y pasarle el trabajador
            Object controller = loader.getController();

            try {
                controller.getClass()
                        .getMethod("inicializar", Trabajador.class)
                        .invoke(controller, trabajadorActual);
            } catch (NoSuchMethodException e) {
                System.out.println("ℹ️  El controlador no tiene método inicializar(Trabajador)");
            }

            Stage stage = (Stage) btnFichar.getScene().getWindow();
            stage.setScene(new Scene(root, ancho, alto));
            stage.setTitle(titulo);

        } catch (Exception e) {
            System.err.println("💥 ERROR al cargar vista: " + rutaFxml);
            e.printStackTrace();

            AlertasUtil.mostrarError(
                    "Error al cargar vista",
                    "No se pudo abrir " + titulo + ": " + e.getMessage()
            );
        }
    }
}