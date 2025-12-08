package org.example.controller.admin;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.model.entity.Fichaje;
import org.example.model.enums.TipoFichaje;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller para seleccionar un fichaje específico de un día
 * cuando hay múltiples fichajes en el mismo día
 */
public class DialogoSelectorFichajeController {

    @FXML private TableView<Fichaje> tableFichajes;
    @FXML private TableColumn<Fichaje, Integer> colId;
    @FXML private TableColumn<Fichaje, LocalDateTime> colFechaHora;
    @FXML private TableColumn<Fichaje, TipoFichaje> colTipo;
    @FXML private TableColumn<Fichaje, String> colClima;
    @FXML private TableColumn<Fichaje, String> colNotas;

    @FXML private Button btnSeleccionar;
    @FXML private Button btnCancelar;
    @FXML private Label lblInstruccion;

    private Fichaje fichajeSeleccionado = null;
    private boolean seleccionado = false;

    /**
     * Inicializa el diálogo con la lista de fichajes del día
     */
    public void inicializar(List<Fichaje> fichajes, String accion) {
        configurarTabla();
        cargarFichajes(fichajes);

        if (lblInstruccion != null) {
            String texto = accion.equals("eliminar")
                    ? "⚠️ Selecciona el fichaje que deseas ELIMINAR:"
                    : "✏️ Selecciona el fichaje que deseas EDITAR:";
            lblInstruccion.setText(texto);
        }

        if (btnSeleccionar != null) {
            btnSeleccionar.setDisable(true);
        }
    }

    private void configurarTabla() {
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        }

        if (colFechaHora != null) {
            colFechaHora.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
            colFechaHora.setCellFactory(column -> new TableCell<Fichaje, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.toLocalTime().toString());
                    }
                }
            });
        }

        if (colTipo != null) {
            colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
            colTipo.setCellFactory(column -> new TableCell<Fichaje, TipoFichaje>() {
                @Override
                protected void updateItem(TipoFichaje item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.name());
                        if (item == TipoFichaje.ENTRADA) {
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        }
                    }
                }
            });
        }

        if (colClima != null) {
            colClima.setCellValueFactory(new PropertyValueFactory<>("clima"));
        }

        if (colNotas != null) {
            colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
            colNotas.setCellFactory(column -> new TableCell<Fichaje, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        String texto = item.length() > 30
                                ? item.substring(0, 30) + "..."
                                : item;
                        setText(texto);

                        if (item.length() > 30) {
                            Tooltip tooltip = new Tooltip(item);
                            setTooltip(tooltip);
                        }
                    }
                }
            });
        }

        if (tableFichajes != null) {
            tableFichajes.getSelectionModel().selectedItemProperty().addListener(
                    (obs, old, nuevo) -> {
                        if (btnSeleccionar != null) {
                            btnSeleccionar.setDisable(nuevo == null);
                        }
                    }
            );
        }
    }

    private void cargarFichajes(List<Fichaje> fichajes) {
        if (tableFichajes != null) {
            tableFichajes.getItems().setAll(fichajes);
        }
    }

    @FXML
    private void handleSeleccionar() {
        fichajeSeleccionado = tableFichajes.getSelectionModel().getSelectedItem();
        if (fichajeSeleccionado != null) {
            seleccionado = true;
            cerrarDialogo();
        }
    }

    @FXML
    private void handleCancelar() {
        seleccionado = false;
        fichajeSeleccionado = null;
        cerrarDialogo();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    public Fichaje getFichajeSeleccionado() {
        return fichajeSeleccionado;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }
}