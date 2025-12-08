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
import org.example.model.dto.FichajeDiaDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.util.AlertasUtil;
import org.example.util.FichajesProcesador;
import org.example.util.HorasFormateador;
import org.example.util.NavegacionUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador CRUD Fichajes (ADMIN).
 * CORREGIDO: Muestra solo el día actual por defecto.
 */
public class CrudFichajesController {

    // FILTROS
    @FXML private ComboBox<Trabajador> cbEmpleado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cbTipo;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;

    // BOTONES
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver;
    @FXML private Button btnRefrescar;

    // TABLA
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
        System.out.println("📋 CrudFichajesController inicializado");

        configurarTabla();
        cargarTrabajadores();
        configurarTipoFichaje();
        configurarFiltrosDiaActual();  // ← CAMBIADO: Solo día actual
        cargarFichajes();

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

            System.out.println("✅ ComboBox empleados cargado: " + trabajadores.size());

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

        System.out.println("✅ ComboBox tipo fichaje configurado");
    }

    /**
     * NUEVO: Configura filtros para mostrar SOLO el día actual.
     * Antes mostraba el mes completo.
     */
    private void configurarFiltrosDiaActual() {
        LocalDate hoy = LocalDate.now();

        if (dpFechaInicio != null) {
            dpFechaInicio.setValue(hoy);  // ← HOY (no primer día del mes)
        }

        if (dpFechaFin != null) {
            dpFechaFin.setValue(hoy);     // ← HOY (no hoy)
        }

        System.out.println("✅ Filtros configurados: " + hoy);
    }

    /**
     * ACTUALIZADO: Filtra fichajes según el RANGO DE FECHAS exacto.
     * Si seleccionas del 4 al 8, muestra SOLO esos días.
     */
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

        System.out.println("🔍 Buscando fichajes:");
        System.out.println("   Empleado: " + (trabajadorFiltro != null ? trabajadorFiltro.getNombreCompleto() : "TODOS"));
        System.out.println("   Desde: " + inicio + " hasta: " + fin);

        try {
            Integer trabajadorId = trabajadorFiltro != null ? trabajadorFiltro.getId() : null;
            String tipoSeleccionado = (cbTipo != null && cbTipo.getValue() != null)
                    ? cbTipo.getValue()
                    : "TODOS";

            // Buscar fichajes en el rango EXACTO
            List<Fichaje> fichajes = fichajeDAO.buscar(trabajadorId, inicio, fin, tipoSeleccionado);

            System.out.println("   ✅ Fichajes encontrados: " + fichajes.size());

            // Agrupar por día
            List<FichajeDiaDTO> fichajesPorDia = FichajesProcesador.agruparFichajesPorDia(fichajes, true);

            System.out.println("   ✅ Días agrupados: " + fichajesPorDia.size());

            // Mostrar en tabla
            if (tableFichajes != null) {
                tableFichajes.setItems(FXCollections.observableArrayList(fichajesPorDia));
            }

            // Actualizar contador
            if (lblContador != null) {
                String rangoTexto = inicio.equals(fin)
                        ? "del día " + inicio
                        : "del " + inicio + " al " + fin;
                lblContador.setText("Mostrando " + fichajesPorDia.size() + " registro(s) " + rangoTexto);
            }

            System.out.println("✅ Vista actualizada correctamente");

        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar fichajes: " + e.getMessage());
        }
    }

    @FXML
    private void handleBuscar() {
        System.out.println("🔍 Búsqueda manual iniciada");
        cargarFichajes();
    }

    @FXML
    private void handleRefrescar() {
        System.out.println("🔄 Refrescando datos");
        cargarFichajes();
    }

    @FXML
    private void handleLimpiar() {
        System.out.println("🧹 Limpiando filtros");

        if (cbEmpleado != null) cbEmpleado.setValue(null);
        if (cbTipo != null) cbTipo.setValue("TODOS");
        configurarFiltrosDiaActual();  // ← Vuelve al día actual
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
}