package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.dto.FichajeDiaDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.util.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class CrudFichajesController {

    @FXML private ComboBox<Trabajador> cbEmpleado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cbTipo;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;
    @FXML private Button btnExportarExcel;
    @FXML private Button btnExportarPDF;

    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private Button btnRefrescar;

    @FXML private TableView<FichajeDiaDTO> tableFichajes;
    @FXML private TableColumn<FichajeDiaDTO, String> colEmpleado;
    @FXML private TableColumn<FichajeDiaDTO, String> colTarjeta;
    @FXML private TableColumn<FichajeDiaDTO, LocalDate> colFecha;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada1;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida1;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada2;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida2;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada3;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida3;
    @FXML private TableColumn<FichajeDiaDTO, String> colNotas;
    @FXML private TableColumn<FichajeDiaDTO, String> colClima;
    @FXML private TableColumn<FichajeDiaDTO, String> colHorasTotales;
    @FXML private TableColumn<FichajeDiaDTO, String> colEstado;

    @FXML private Label lblContador;

    private Trabajador trabajadorActual;
    private final FichajeDAO fichajeDAO = new FichajeDAO();
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;
        configurarTabla();
        configurarTipoFichaje();
        configurarFiltrosDiaActual();
        cargarTrabajadores();

        if (btnExportarExcel != null) {
            btnExportarExcel.setOnAction(e -> exportarExcel());
        }

        if (btnExportarPDF != null) {
            btnExportarPDF.setOnAction(e -> exportarPDF());
        }

        if (btnEditar != null) btnEditar.setDisable(true);
        if (btnEliminar != null) btnEliminar.setDisable(true);
    }

    private void configurarTabla() {
        if (colEmpleado != null) colEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        if (colTarjeta != null) colTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        if (colFecha != null) colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        if (colEntrada1 != null) colEntrada1.setCellValueFactory(new PropertyValueFactory<>("entrada1"));
        if (colSalida1 != null) colSalida1.setCellValueFactory(new PropertyValueFactory<>("salida1"));
        if (colEntrada2 != null) colEntrada2.setCellValueFactory(new PropertyValueFactory<>("entrada2"));
        if (colSalida2 != null) colSalida2.setCellValueFactory(new PropertyValueFactory<>("salida2"));
        if (colEntrada3 != null) colEntrada3.setCellValueFactory(new PropertyValueFactory<>("entrada3"));
        if (colSalida3 != null) colSalida3.setCellValueFactory(new PropertyValueFactory<>("salida3"));
        if (colNotas != null) colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        if (colClima != null) colClima.setCellValueFactory(new PropertyValueFactory<>("clima"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (colHorasTotales != null) {
            colHorasTotales.setCellValueFactory(cellData -> {
                Double horas = cellData.getValue().getHorasTotales();
                return new javafx.beans.property.SimpleStringProperty(HorasFormateador.formatearHoras(horas));
            });
        }

        if (tableFichajes != null) {
            tableFichajes.getSelectionModel().selectedItemProperty().addListener(
                    (obs, old, nuevo) -> {
                        boolean seleccionado = nuevo != null;
                        if (btnEditar != null) btnEditar.setDisable(!seleccionado);
                        if (btnEliminar != null) btnEliminar.setDisable(!seleccionado);
                    }
            );
        }
    }

    private void cargarTrabajadores() {
        if (cbEmpleado == null) {
            System.out.println("⚠️ cbEmpleado no disponible");
            return;
        }

        try {
            List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();
            cbEmpleado.getItems().clear();
            cbEmpleado.getItems().add(null);
            cbEmpleado.getItems().addAll(trabajadores);
            cbEmpleado.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Trabajador item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Todos los empleados" : item.getNombreCompleto());
                }
            });
            cbEmpleado.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Trabajador item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "Todos los empleados" : item.getNombreCompleto());
                }
            });

            cbEmpleado.setValue(null);
        } catch (Exception e) {
            System.err.println("💥 ERROR cargando trabajadores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void configurarTipoFichaje() {
        if (cbTipo == null) {
            System.out.println("⚠️ cbTipo no disponible");
            return;
        }
        cbTipo.getItems().clear();
        cbTipo.getItems().addAll("TODOS", "ENTRADA", "SALIDA");
        cbTipo.setValue("TODOS");
    }

    private void configurarFiltrosDiaActual() {
        LocalDate hoy = LocalDate.now();

        if (dpFechaInicio != null) {
            dpFechaInicio.setValue(hoy);
        }

        if (dpFechaFin != null) {
            dpFechaFin.setValue(hoy);
        }
    }

    private void cargarFichajes() {
        if (cbEmpleado == null || dpFechaInicio == null || dpFechaFin == null) {
            System.out.println("⚠️ Componentes no disponibles");
            return;
        }

        Trabajador trabajadorFiltro = cbEmpleado.getValue();
        LocalDate inicio = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();
        if (inicio == null || fin == null) {
            AlertasUtil.mostrarError("Error", "Seleccione ambas fechas");
            return;
        }

        if (inicio.isAfter(fin)) {
            AlertasUtil.mostrarError("Error", "Fecha inicio debe ser ≤ fecha fin");
            return;
        }

        try {
            Integer trabajadorId = trabajadorFiltro != null ? trabajadorFiltro.getId() : null;
            String tipoSeleccionado = (cbTipo != null && cbTipo.getValue() != null)
                    ? cbTipo.getValue()
                    : "TODOS";

            List<Fichaje> fichajes = fichajeDAO.buscar(trabajadorId, inicio, fin, tipoSeleccionado);
            List<FichajeDiaDTO> fichajesPorDia = FichajesProcesador.agruparFichajesPorDia(fichajes, true);

            if (tableFichajes != null) {
                tableFichajes.setItems(FXCollections.observableArrayList(fichajesPorDia));
            }

            if (lblContador != null) {
                String rangoTexto = inicio.equals(fin)
                        ? "del " + inicio
                        : "del " + inicio + " al " + fin;
                lblContador.setText("Mostrando " + fichajesPorDia.size() + " registro(s) " + rangoTexto);
            }
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar fichajes: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuscar() {
        cargarFichajes();
    }

    @FXML
    private void handleRefrescar() {
        cargarFichajes();
    }

    @FXML
    private void handleLimpiar() {
        if (cbEmpleado != null) cbEmpleado.setValue(null);
        if (cbTipo != null) cbTipo.setValue("TODOS");
        configurarFiltrosDiaActual();
        cargarFichajes();
    }

    @FXML
    private void handleNuevo() throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/dialogo_fichaje.fxml"));
            Parent root = loader.load();
            DialogoFichajeController controller = loader.getController();
            controller.inicializarNuevo();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Fichaje");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnNuevo.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            if (controller.isGuardado()) {
                cargarFichajes();
                AlertasUtil.mostrarExito("Éxito", "Fichaje creado");
            }
        } catch (IOException e) {
            AlertasUtil.mostrarError("Error", "No se pudo abrir el diálogo");
        }
    }

    @FXML
    private void handleEditar() {
        FichajeDiaDTO seleccionado = tableFichajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertasUtil.mostrarAdvertencia("Advertencia", "Seleccione un fichaje");
            return;
        }
        AlertasUtil.mostrarInfo("Info", "Funcionalidad en desarrollo");
    }

    @FXML
    private void handleEliminar() {
        FichajeDiaDTO seleccionado = tableFichajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            AlertasUtil.mostrarAdvertencia("Advertencia", "Seleccione un fichaje");
            return;
        }
        AlertasUtil.mostrarInfo("Info", "Funcionalidad en desarrollo");
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }

    private void exportarExcel() {
        if (tableFichajes == null || tableFichajes.getItems().isEmpty()) {
            AlertasUtil.mostrarError("Error", "No hay datos para exportar");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo Excel");
            fileChooser.setInitialFileName("fichajes_" + LocalDate.now() + ".xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
            );

            File archivo = fileChooser.showSaveDialog(btnExportarExcel.getScene().getWindow());

            if (archivo != null) {
                List<FichajeDiaDTO> fichajes = tableFichajes.getItems();
                LocalDate fechaInicio = dpFechaInicio != null && dpFechaInicio.getValue() != null
                        ? dpFechaInicio.getValue()
                        : LocalDate.now();
                LocalDate fechaFin = dpFechaFin != null && dpFechaFin.getValue() != null
                        ? dpFechaFin.getValue()
                        : LocalDate.now();

                Trabajador trabajadorFiltro = cbEmpleado != null ? cbEmpleado.getValue() : null;
                String nombreEmpleado = trabajadorFiltro != null ? trabajadorFiltro.getNombreCompleto() : null;

                boolean exito = ExcelExportador.exportar(
                        fichajes,
                        archivo,
                        nombreEmpleado,
                        fechaInicio,
                        fechaFin
                );

                if (exito) {
                    AlertasUtil.mostrarExito("Éxito",
                            "Excel generado correctamente en:\n" + archivo.getAbsolutePath());
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo generar el Excel");
                }
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al exportar Excel: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    private void exportarPDF() {
        if (tableFichajes == null || tableFichajes.getItems().isEmpty()) {
            AlertasUtil.mostrarError("Error", "No hay datos para exportar");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo PDF");
            fileChooser.setInitialFileName("fichajes_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );

            File archivo = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());
            if (archivo != null) {
                List<FichajeDiaDTO> fichajes = tableFichajes.getItems();
                LocalDate fechaInicio = dpFechaInicio != null && dpFechaInicio.getValue() != null
                        ? dpFechaInicio.getValue()
                        : LocalDate.now();
                LocalDate fechaFin = dpFechaFin != null && dpFechaFin.getValue() != null
                        ? dpFechaFin.getValue()
                        : LocalDate.now();
                Trabajador trabajadorFiltro = cbEmpleado != null ? cbEmpleado.getValue() : null;
                String nombreEmpleado = trabajadorFiltro != null ? trabajadorFiltro.getNombreCompleto() : null;
                boolean exito = PDFExportador.exportar(
                        fichajes,
                        archivo,
                        nombreEmpleado,
                        fechaInicio,
                        fechaFin
                );

                if (exito) {
                    AlertasUtil.mostrarExito("Éxito",
                            "PDF generado correctamente en:\n" + archivo.getAbsolutePath());
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo generar el PDF");
                }
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al exportar PDF: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al exportar: " + e.getMessage());
        }
    }
}