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

import java.io.IOException;

/**
 * Controlador para el dashboard principal.
 * Muestra diferentes opciones según el rol del usuario (ADMIN o TRABAJADOR).
 */
public class DashboardController {

    @FXML
    private Label lblBienvenida;

    @FXML
    private Label lblRol;

    // Botones comunes
    @FXML
    private Button btnFichar;

    @FXML
    private Button btnMisFichajes;

    @FXML
    private Button btnMisEstadisticas;

    @FXML
    private Button btnMisDatos;

    @FXML
    private Button btnCerrarSesion;

    // Botones solo ADMIN
    @FXML
    private Button btnCrudTrabajadores;

    @FXML
    private Button btnVerTodosFichajes;

    @FXML
    private Button btnEstadisticasGlobales;

    // Contenedores para ocultar secciones completas
    @FXML
    private VBox menuAdmin;

    @FXML
    private VBox menuComun;

    private Trabajador trabajadorActual;

    /**
     * Inicializa el dashboard con los datos del trabajador.
     * Este método debe ser llamado después de cargar el FXML.
     *
     * @param trabajador Usuario que ha iniciado sesión
     */
    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        System.out.println("🏠 Dashboard inicializado para: " + trabajador.getNombreCompleto());

        // Mostrar bienvenida
        lblBienvenida.setText("¡Bienvenido, " + trabajador.getNombreCompleto() + "!");
        lblRol.setText("Rol: " + trabajador.getRol());

        // Configurar visibilidad según rol
        configurarVistaPorRol();
    }

    /**
     * Configura qué botones son visibles según el rol del usuario.
     */
    private void configurarVistaPorRol() {
        boolean esAdmin = trabajadorActual.getRol() == Rol.ADMIN;

        // Ocultar/mostrar el menú de administrador completo
        if (menuAdmin != null) {
            menuAdmin.setVisible(esAdmin);
            menuAdmin.setManaged(esAdmin);
        }

        // Botones solo para ADMIN (por si acaso no están en menuAdmin)
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

        System.out.println("✅ Vista configurada para: " +
                (esAdmin ? "ADMINISTRADOR" : "TRABAJADOR"));
    }

    /**
     * Abre la vista de fichar (común para todos).
     */
    @FXML
    private void handleFichar() {
        cargarVista("/views/fichar.fxml", "Fichar", 500, 650);
    }

    /**
     * Alias para handleFichar (compatibilidad).
     */
    @FXML
    private void handlerFichar() {
        handleFichar();
    }

    /**
     * Alias para handleFichar (compatibilidad).
     */
    @FXML
    private void handlerRegistrarFichaje() {
        handleFichar();
    }

    /**
     * Abre la vista de fichajes del trabajador actual.
     */
    @FXML
    private void handleMisFichajes() {
        cargarVista("/views/trabajador/mis_fichajes.fxml", "Mis Fichajes", 1000, 600);
    }

    /**
     * Abre la vista de estadísticas del trabajador actual.
     */
    @FXML
    private void handleMisEstadisticas() {
        cargarVista("/views/trabajador/mis_estadisticas.fxml", "Mis Estadísticas", 900, 600);
    }

    /**
     * Abre CRUD de trabajadores (solo ADMIN).
     */
    @FXML
    private void handleCrudTrabajadores() {
        cargarVista("/views/admin/crud_trabajadores.fxml", "Gestión de Trabajadores", 1000, 600);
    }

    /**
     * Abre CRUD de fichajes (solo ADMIN).
     * Método llamado desde el FXML.
     */
    @FXML
    private void handleVerTodosFichajes() {
        cargarVista("/views/admin/crud_fichajes.fxml", "Gestión de Fichajes", 1200, 700);
    }

    /**
     * Abre la vista de datos personales (tarjeta y PIN).
     */
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

    /**
     * Abre estadísticas globales (solo ADMIN).
     */
    @FXML
    private void handleEstadisticasGlobales() {
        cargarVista("/views/admin/estadisticas_globales.fxml", "Estadísticas Globales", 1000, 600);
    }

    /**
     * Cierra sesión y vuelve al login.
     */
    @FXML
    private void handleCerrarSesion() {
        System.out.println("🚪 Cerrando sesión de: " + trabajadorActual.getNombreCompleto());

        boolean confirmar = AlertasUtil.confirmarAccion(
                "Cerrar Sesión",
                "¿Está seguro que desea cerrar sesión?"
        );

        if (confirmar) {
            NavegacionUtil.volverAlLogin(btnCerrarSesion);
        }
    }

    /**
     * Carga una vista y pasa el trabajador actual al controlador.
     *
     * @param rutaFxml Ruta al archivo FXML
     * @param titulo Título de la ventana
     * @param ancho Ancho de la ventana
     * @param alto Alto de la ventana
     */
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

            // Usar reflexión para llamar al método inicializar si existe
            try {
                controller.getClass()
                        .getMethod("inicializar", Trabajador.class)
                        .invoke(controller, trabajadorActual);
            } catch (NoSuchMethodException e) {
                System.out.println("ℹ️  El controlador no tiene método inicializar(Trabajador)");
            }

            // Cambiar escena
            Stage stage = (Stage) btnFichar.getScene().getWindow();
            stage.setScene(new Scene(root, ancho, alto));
            stage.setTitle(titulo);

            System.out.println("✅ Vista cargada: " + titulo);

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