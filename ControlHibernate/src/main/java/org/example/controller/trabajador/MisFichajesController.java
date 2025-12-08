package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dao.FichajeDAO;
import org.example.model.dto.FichajeDiaDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.util.AlertasUtil;
import org.example.util.FichajesProcesador;
import org.example.util.HorasFormateador;
import org.example.util.NavegacionUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Controlador Mis Fichajes (TRABAJADOR).
 * CORREGIDO: Usa cbFiltro y dpFecha en lugar de dpFechaInicio/dpFechaFin.
 */
public class MisFichajesController {

    // FILTROS
    @FXML private ComboBox<String> cbFiltro;  // ← CORREGIDO (era dpFechaInicio/dpFechaFin)
    @FXML private DatePicker dpFecha;         // ← CORREGIDO (un solo DatePicker)
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    // ESTADÍSTICAS
    @FXML private Label lblTotalPeriodo;      // ← CORREGIDO (era lblTotalHoras)
    @FXML private Label lblPromedioDiario;
    @FXML private Label lblDiasTrabajados;    // ← CORREGIDO (era lblTotalDias)

    // TABLA
    @FXML private TableView<FichajeDiaDTO> tableFichajes;
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

    private Trabajador trabajadorActual;
    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;
        System.out.println("📅 MisFichajesController inicializado para: " + trabajador.getNombreCompleto());

        configurarTabla();
        configurarFiltros();  // ← NUEVO
        cargarFichajes();
    }

    private void configurarTabla() {
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

        if (colFecha != null) {
            colFecha.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate fecha, boolean empty) {
                    super.updateItem(fecha, empty);
                    setText(empty || fecha == null ? null : fecha.toString());
                }
            });
        }
    }

    /**
     * NUEVO: Configura ComboBox de filtros y DatePicker.
     */
    private void configurarFiltros() {
        if (cbFiltro == null) {
            System.out.println("⚠️ cbFiltro no disponible");
            return;
        }

        cbFiltro.getItems().clear();
        cbFiltro.getItems().addAll(
                "HOY",
                "ESTA SEMANA",
                "ESTE MES",
                "MES PASADO",
                "PERSONALIZADO"
        );

        cbFiltro.setValue("HOY");  // Por defecto: hoy

        // Listener para cambios en el filtro
        cbFiltro.valueProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                if (nuevo.equals("PERSONALIZADO")) {
                    // Habilitar DatePicker
                    if (dpFecha != null) {
                        dpFecha.setDisable(false);
                        dpFecha.setValue(LocalDate.now());
                    }
                } else {
                    // Deshabilitar DatePicker
                    if (dpFecha != null) dpFecha.setDisable(true);
                    // Cargar automáticamente
                    cargarFichajes();
                }
            }
        });

        // Configurar DatePicker (deshabilitado por defecto)
        if (dpFecha != null) {
            dpFecha.setValue(LocalDate.now());
            dpFecha.setDisable(true);
        }

        System.out.println("✅ Filtros configurados");
    }

    /**
     * ACTUALIZADO: Carga fichajes según el filtro seleccionado.
     */
    private void cargarFichajes() {
        if (cbFiltro == null) {
            System.out.println("⚠️ cbFiltro no disponible");
            return;
        }

        String filtro = cbFiltro.getValue();
        if (filtro == null) {
            AlertasUtil.mostrarError("Error", "Seleccione un filtro");
            return;
        }

        LocalDate inicio;
        LocalDate fin;
        LocalDate hoy = LocalDate.now();

        // Calcular rango según filtro
        switch (filtro) {
            case "HOY":
                inicio = hoy;
                fin = hoy;
                break;

            case "ESTA SEMANA":
                inicio = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);  // Lunes
                fin = inicio.plusDays(6);  // Domingo
                break;

            case "ESTE MES":
                inicio = hoy.withDayOfMonth(1);
                fin = hoy.withDayOfMonth(hoy.lengthOfMonth());
                break;

            case "MES PASADO":
                LocalDate primerDiaMesPasado = hoy.minusMonths(1).withDayOfMonth(1);
                inicio = primerDiaMesPasado;
                fin = primerDiaMesPasado.withDayOfMonth(primerDiaMesPasado.lengthOfMonth());
                break;

            case "PERSONALIZADO":
                if (dpFecha == null || dpFecha.getValue() == null) {
                    AlertasUtil.mostrarError("Error", "Seleccione una fecha");
                    return;
                }
                inicio = dpFecha.getValue();
                fin = dpFecha.getValue();
                break;

            default:
                inicio = hoy;
                fin = hoy;
        }

        System.out.println("📅 Cargando fichajes:");
        System.out.println("   Filtro: " + filtro);
        System.out.println("   Rango: " + inicio + " - " + fin);

        try {
            // Obtener fichajes
            List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                    trabajadorActual.getId(),
                    inicio,
                    fin
            );

            // Agrupar por día
            List<FichajeDiaDTO> fichajesPorDia = FichajesProcesador.agruparFichajesPorDia(
                    fichajes,
                    false  // No incluir datos de empleado
            );

            // Mostrar en tabla
            if (tableFichajes != null) {
                tableFichajes.setItems(FXCollections.observableArrayList(fichajesPorDia));
            }

            // Calcular estadísticas
            calcularEstadisticas(fichajesPorDia, fichajes);

            System.out.println("✅ Fichajes cargados: " + fichajesPorDia.size() + " días");

        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar fichajes");
        }
    }

    /**
     * Calcula y muestra estadísticas.
     */
    private void calcularEstadisticas(List<FichajeDiaDTO> fichajesPorDia, List<Fichaje> fichajesRaw) {
        int totalDias = fichajesPorDia.size();

        double totalHoras = fichajesPorDia.stream()
                .mapToDouble(f -> f.getHorasTotales() != null ? f.getHorasTotales() : 0.0)
                .sum();

        double promedioDiario = totalDias > 0 ? totalHoras / totalDias : 0.0;

        // Actualizar labels
        if (lblTotalPeriodo != null) {
            lblTotalPeriodo.setText(HorasFormateador.formatearHoras(totalHoras));
        }

        if (lblPromedioDiario != null) {
            lblPromedioDiario.setText(HorasFormateador.formatearHorasDecimal(promedioDiario));
        }

        if (lblDiasTrabajados != null) {
            lblDiasTrabajados.setText(totalDias + " días");
        }

        System.out.println("📊 Estadísticas:");
        System.out.println("   Días: " + totalDias);
        System.out.println("   Total: " + HorasFormateador.formatearHoras(totalHoras));
        System.out.println("   Promedio: " + HorasFormateador.formatearHorasDecimal(promedioDiario));
    }

    @FXML
    private void handleBuscar() {
        System.out.println("🔍 Búsqueda manual");
        cargarFichajes();
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }
}