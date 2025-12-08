package org.example.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.controller.DashboardController;
import org.example.model.entity.Trabajador;

import java.io.IOException;

/**
 * Utilidad para navegación entre vistas de la aplicación.
 * Centraliza la carga de FXMLs y el cambio de escenas.
 */
public class NavegacionUtil {

    private NavegacionUtil() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Carga una vista FXML y devuelve su controlador.
     *
     * @param rutaFxml Ruta al archivo FXML (ej: "/views/dashboard.fxml")
     * @param stage Stage donde se mostrará la vista
     * @param titulo Título de la ventana
     * @param ancho Ancho de la ventana
     * @param alto Alto de la ventana
     * @return El controlador de la vista cargada
     * @throws IOException Si hay error cargando el FXML
     */
    public static <T> T cargarVista(
            String rutaFxml,
            Stage stage,
            String titulo,
            int ancho,
            int alto) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                NavegacionUtil.class.getResource(rutaFxml)
        );
        Parent root = loader.load();

        stage.setScene(new Scene(root, ancho, alto));
        stage.setTitle(titulo);

        return loader.getController();
    }

    /**
     * Abre el dashboard del usuario (admin o trabajador).
     *
     * @param botonOrigen Cualquier control de la ventana actual
     * @param trabajador Usuario que ha iniciado sesión
     */
    public static void abrirDashboard(Button botonOrigen, Trabajador trabajador) {
        try {
            Stage stage = (Stage) botonOrigen.getScene().getWindow();

            DashboardController controller = cargarVista(
                    "/views/dashboard.fxml",
                    stage,
                    "Control Horario - Dashboard",
                    800,
                    600
            );

            controller.inicializar(trabajador);

        } catch (IOException e) {
            AlertasUtil.mostrarError(
                    "Error de Navegación",
                    "No se pudo cargar el dashboard: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }

    /**
     * Abre un diálogo modal y devuelve su controlador.
     *
     * @param rutaFxml Ruta al archivo FXML del diálogo
     * @param titulo Título del diálogo
     * @return El controlador del diálogo
     * @throws IOException Si hay error cargando el FXML
     */
    public static <T> T abrirDialogoModal(String rutaFxml, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                NavegacionUtil.class.getResource(rutaFxml)
        );
        Parent root = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle(titulo);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setScene(new Scene(root));
        dialogStage.setResizable(false);

        return loader.getController();
    }

    /**
     * Vuelve al login cerrando la sesión.
     *
     * @param botonOrigen Cualquier control de la ventana actual
     */
    public static void volverAlLogin(Button botonOrigen) {
        try {
            Stage stage = (Stage) botonOrigen.getScene().getWindow();

            cargarVista(
                    "/views/login.fxml",
                    stage,
                    "Control Horario - Login",
                    450,
                    550
            );

        } catch (IOException e) {
            AlertasUtil.mostrarError(
                    "Error de Navegación",
                    "No se pudo volver al login: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}