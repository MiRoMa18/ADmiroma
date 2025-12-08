package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.entity.Trabajador;
import org.example.model.enums.Rol;
import org.example.util.AlertasUtil;
import org.example.util.NavegacionUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class CrudTrabajadoresController {
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private Button btnRefrescar;

    @FXML private TableView<Trabajador> tableTrabajadores;
    @FXML private TableColumn<Trabajador, Integer> colId;
    @FXML private TableColumn<Trabajador, String> colNumeroTarjeta;
    @FXML private TableColumn<Trabajador, String> colNombre;
    @FXML private TableColumn<Trabajador, String> colApellidos;
    @FXML private TableColumn<Trabajador, String> colEmail;
    @FXML private TableColumn<Trabajador, Rol> colRol;
    @FXML private TableColumn<Trabajador, LocalDate> colFechaAlta;

    @FXML private Label lblTotalTrabajadores;

    private Trabajador trabajadorActual;
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;
        configurarTabla();
        configurarRolCombo();
        cargarTrabajadores();
        configurarBusqueda();

        if (btnEditar != null) btnEditar.setDisable(true);
        if (btnEliminar != null) btnEliminar.setDisable(true);
    }

    private void configurarTabla() {
        if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colNumeroTarjeta != null) colNumeroTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        if (colApellidos != null) colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        if (colEmail != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colRol != null) colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        if (colFechaAlta != null) colFechaAlta.setCellValueFactory(new PropertyValueFactory<>("fechaAlta"));

        if (colRol != null) {
            colRol.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Rol rol, boolean empty) {
                    super.updateItem(rol, empty);
                    if (empty || rol == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(rol.name());
                        if (rol == Rol.ADMIN) {
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #1976d2;");
                        }
                    }
                }
            });
        }

        if (tableTrabajadores != null) {
            tableTrabajadores.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        boolean seleccionado = newValue != null;
                        if (btnEditar != null) btnEditar.setDisable(!seleccionado);
                        if (btnEliminar != null) btnEliminar.setDisable(!seleccionado);
                    }
            );
        }
    }

    private void configurarRolCombo() {
        if (cbRol == null) {
            System.out.println("⚠️ cbRol no disponible");
            return;
        }

        cbRol.getItems().clear();
        cbRol.getItems().addAll("TODOS", "ADMIN", "TRABAJADOR");
        cbRol.setValue("TODOS");

        cbRol.valueProperty().addListener((obs, old, nuevo) -> filtrarTrabajadores(txtBuscar.getText()));
    }

    private void cargarTrabajadores() {
        try {
            List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();

            if (tableTrabajadores != null) {
                tableTrabajadores.setItems(FXCollections.observableArrayList(trabajadores));
            }

            if (lblTotalTrabajadores != null) {
                lblTotalTrabajadores.setText("Total: " + trabajadores.size());
            }
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar los trabajadores");
        }
    }

    private void configurarBusqueda() {
        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, old, nuevo) -> filtrarTrabajadores(nuevo));
        }
    }

    @FXML
    private void handleBuscar() {
        filtrarTrabajadores(txtBuscar != null ? txtBuscar.getText() : "");
    }

    @FXML
    private void handleLimpiar() {
        if (txtBuscar != null) txtBuscar.clear();
        if (cbRol != null) cbRol.setValue("TODOS");
        cargarTrabajadores();
    }

    private void filtrarTrabajadores(String texto) {
        try {
            List<Trabajador> todos = trabajadorDAO.obtenerTodos();

            if (texto == null) texto = "";
            String busqueda = texto.toLowerCase();

            List<Trabajador> filtrados = todos.stream()
                    .filter(t ->
                            t.getNombre().toLowerCase().contains(busqueda) ||
                                    t.getApellidos().toLowerCase().contains(busqueda) ||
                                    t.getNumeroTarjeta().contains(busqueda) ||
                                    (t.getEmail() != null && t.getEmail().toLowerCase().contains(busqueda))
                    )
                    .toList();

            if (cbRol != null && cbRol.getValue() != null && !cbRol.getValue().equals("TODOS")) {
                Rol rolSeleccionado = Rol.valueOf(cbRol.getValue());
                filtrados = filtrados.stream()
                        .filter(t -> t.getRol() == rolSeleccionado)
                        .toList();
            }

            if (tableTrabajadores != null) {
                tableTrabajadores.setItems(FXCollections.observableArrayList(filtrados));
            }

            if (lblTotalTrabajadores != null) {
                lblTotalTrabajadores.setText("Encontrados: " + filtrados.size());
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al filtrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/dialogo_trabajador.fxml"));
            Parent root = loader.load();
            DialogoTrabajadorController controller = loader.getController();
            controller.inicializarNuevo();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Trabajador");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNuevo.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isGuardado()) {
                cargarTrabajadores();
                AlertasUtil.mostrarExito("Éxito", "Trabajador creado correctamente");
            }
        } catch (IOException e) {
            AlertasUtil.mostrarError("Error", "No se pudo abrir el diálogo");
        }
    }

    @FXML
    private void handleEditar() {
        Trabajador seleccionado = tableTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertasUtil.mostrarAdvertencia("Advertencia", "Debe seleccionar un trabajador");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/dialogo_trabajador.fxml"));
            Parent root = loader.load();
            DialogoTrabajadorController controller = loader.getController();
            controller.inicializarEditar(seleccionado);

            Stage stage = new Stage();
            stage.setTitle("Editar Trabajador");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnEditar.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isGuardado()) {
                cargarTrabajadores();
                AlertasUtil.mostrarExito("Éxito", "Trabajador actualizado correctamente");
            }
        } catch (IOException e) {
            AlertasUtil.mostrarError("Error", "No se pudo abrir el diálogo");
        }
    }

    @FXML
    private void handleEliminar() {
        Trabajador seleccionado = tableTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertasUtil.mostrarAdvertencia("Advertencia", "Debe seleccionar un trabajador");
            return;
        }

        if (seleccionado.getId().equals(trabajadorActual.getId())) {
            AlertasUtil.mostrarError("Error", "No puede eliminarse a sí mismo");
            return;
        }

        boolean tieneFichajes = fichajeDAO.tieneFichajes(seleccionado.getId());
        String mensaje = tieneFichajes
                ? "¿Eliminar a " + seleccionado.getNombreCompleto() + "?\n\n" +
                "ADVERTENCIA: Se eliminarán TODOS sus fichajes."
                : "¿Eliminar a " + seleccionado.getNombreCompleto() + "?";

        if (!AlertasUtil.confirmarAccion("Confirmar eliminación", mensaje)) {
            return;
        }

        try {
            if (trabajadorDAO.eliminar(seleccionado.getId())) {
                cargarTrabajadores();
                AlertasUtil.mostrarExito("Éxito", "Trabajador eliminado correctamente");
            } else {
                AlertasUtil.mostrarError("Error", "No se pudo eliminar el trabajador");
            }
        } catch (Exception e) {
            AlertasUtil.mostrarError("Error", "Error al eliminar: " + e.getMessage());
        }
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }

    @FXML
    private void handleRefrescar() {
        if (txtBuscar != null) txtBuscar.clear();
        if (cbRol != null) cbRol.setValue("TODOS");
        cargarTrabajadores();
        AlertasUtil.mostrarInfo("Información", "Lista actualizada");
    }
}