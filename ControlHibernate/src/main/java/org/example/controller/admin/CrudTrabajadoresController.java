package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.TrabajadorDAO;
import org.example.model.Rol;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CrudTrabajadoresController {

    // === FILTROS ===
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbRol;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;

    // === BOTONES DE ACCIÓN ===
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;
    @FXML private Button btnVolver;

    // === TABLA ===
    @FXML private TableView<Trabajador> tableTrabajadores;
    @FXML private TableColumn<Trabajador, Integer> colId;
    @FXML private TableColumn<Trabajador, String> colNumeroTarjeta;
    @FXML private TableColumn<Trabajador, String> colNombre;
    @FXML private TableColumn<Trabajador, String> colApellidos;
    @FXML private TableColumn<Trabajador, String> colEmail;
    @FXML private TableColumn<Trabajador, Rol> colRol;
    @FXML private TableColumn<Trabajador, LocalDate> colFechaAlta;

    @FXML private Label lblContador;

    private Trabajador adminActual;
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    public void inicializar(Trabajador admin) {
        this.adminActual = admin;

        // Configurar ComboBox de rol
        cbRol.setItems(FXCollections.observableArrayList("TODOS", "ADMIN", "TRABAJADOR"));
        cbRol.setValue("TODOS");

        // Configurar tabla
        configurarTabla();

        // Deshabilitar botones editar/eliminar hasta seleccionar
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        // Listener para selección
        tableTrabajadores.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean haySeleccion = newSel != null;
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });

        // Doble clic para editar
        tableTrabajadores.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (tableTrabajadores.getSelectionModel().getSelectedItem() != null) {
                    handleEditar();
                }
            }
        });

        // Cargar trabajadores
        cargarTrabajadores();

        System.out.println("✅ Vista 'CRUD Trabajadores' cargada para ADMIN: " + admin.getNombre());
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumeroTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colFechaAlta.setCellValueFactory(new PropertyValueFactory<>("fechaAlta"));

        // Formatear columna de rol con colores
        colRol.setCellFactory(col -> new TableCell<Trabajador, Rol>() {
            @Override
            protected void updateItem(Rol rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(rol.name());
                    if (rol == Rol.ADMIN) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Formatear columna de fecha
        colFechaAlta.setCellFactory(col -> new TableCell<Trabajador, LocalDate>() {
            private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setText(null);
                } else {
                    setText(fecha.format(formatter));
                }
            }
        });

        // Email en cursiva
        colEmail.setCellFactory(col -> new TableCell<Trabajador, String>() {
            @Override
            protected void updateItem(String email, boolean empty) {
                super.updateItem(email, empty);
                if (empty || email == null || email.isEmpty()) {
                    setText("—");
                    setStyle("-fx-text-fill: #95a5a6;");
                } else {
                    setText(email);
                    setStyle("-fx-font-style: italic; -fx-text-fill: #3498db;");
                }
            }
        });
    }

    private void cargarTrabajadores() {
        System.out.println("🔍 Cargando trabajadores...");

        String busqueda = txtBuscar.getText().trim();
        String rolFiltro = cbRol.getValue();

        List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();

        // Aplicar filtros
        if (!busqueda.isEmpty()) {
            String busquedaLower = busqueda.toLowerCase();
            trabajadores = trabajadores.stream()
                    .filter(t ->
                            t.getNombre().toLowerCase().contains(busquedaLower) ||
                                    (t.getApellidos() != null && t.getApellidos().toLowerCase().contains(busquedaLower)) ||
                                    t.getNumeroTarjeta().toLowerCase().contains(busquedaLower) ||
                                    (t.getEmail() != null && t.getEmail().toLowerCase().contains(busquedaLower))
                    )
                    .collect(Collectors.toList());
        }

        if (!rolFiltro.equals("TODOS")) {
            Rol rol = Rol.valueOf(rolFiltro);
            trabajadores = trabajadores.stream()
                    .filter(t -> t.getRol() == rol)
                    .collect(Collectors.toList());
        }

        ObservableList<Trabajador> items = FXCollections.observableArrayList(trabajadores);
        tableTrabajadores.setItems(items);
        lblContador.setText("Mostrando " + items.size() + " trabajador(es)");

        System.out.println("   ✅ Cargados " + items.size() + " trabajador(es)");
    }

    @FXML
    private void handleBuscar() {
        cargarTrabajadores();
    }

    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cbRol.setValue("TODOS");
        cargarTrabajadores();
    }

    @FXML
    private void handleNuevo() {
        abrirDialogoTrabajador(null);
    }

    @FXML
    private void handleEditar() {
        Trabajador seleccionado = tableTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        abrirDialogoTrabajador(seleccionado);
    }

    @FXML
    private void handleEliminar() {
        Trabajador seleccionado = tableTrabajadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        // Validación: no se puede eliminar a sí mismo
        if (seleccionado.getId().equals(adminActual.getId())) {
            mostrarAlerta("Error", "No puedes eliminar tu propia cuenta.");
            return;
        }

        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este trabajador?");
        confirmacion.setContentText(
                "Trabajador: " + seleccionado.getNombreCompleto() + "\n" +
                        "Tarjeta: " + seleccionado.getNumeroTarjeta() + "\n" +
                        "Rol: " + seleccionado.getRol().name() + "\n\n" +
                        "⚠️ ATENCIÓN: También se eliminarán todos sus fichajes."
        );

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean exito = trabajadorDAO.eliminar(seleccionado.getId());

            if (exito) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Eliminado");
                info.setHeaderText(null);
                info.setContentText("✅ Trabajador eliminado correctamente");
                info.showAndWait();

                cargarTrabajadores();
            } else {
                mostrarAlerta("Error", "❌ Error al eliminar el trabajador");
            }
        }
    }

    @FXML
    private void handleRefrescar() {
        cargarTrabajadores();
    }

    @FXML
    private void handleVolver() {
        System.out.println("🔙 Volviendo al dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            org.example.controller.DashboardController controller = loader.getController();
            controller.inicializar(adminActual);

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error al volver al dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirDialogoTrabajador(Trabajador trabajador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/dialogo_trabajador.fxml"));
            Parent root = loader.load();

            DialogoTrabajadorController controller = loader.getController();
            controller.inicializar(trabajador);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(trabajador == null ? "Nuevo Trabajador" : "Editar Trabajador");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNuevo.getScene().getWindow());
            dialogStage.setScene(new Scene(root, 550, 650));
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if (controller.isGuardado()) {
                cargarTrabajadores();
            }

        } catch (IOException e) {
            System.err.println("❌ Error al abrir diálogo de trabajador: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}