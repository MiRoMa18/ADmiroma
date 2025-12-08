package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.dto.EstadisticaEmpleadoDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.model.enums.TipoFichaje;
import org.example.util.AlertasUtil;
import org.example.util.HorasFormateador;
import org.example.util.NavegacionUtil;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class EstadisticasGlobalesController {

    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    @FXML private Label lblTotalHoras;
    @FXML private Label lblEmpleadosActivos;
    @FXML private Label lblPromedioEmpleado;
    @FXML private Label lblMejorEmpleado;

    @FXML private BarChart<String, Number> chartComparativa;
    @FXML private CategoryAxis xAxisEmpleados;
    @FXML private NumberAxis yAxisHoras;

    @FXML private TableView<EstadisticaEmpleadoDTO> tableEstadisticas;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colNombre;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colTarjeta;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Integer> colDiasTrabajados;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Double> colTotalHoras;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Double> colPromedioDiario;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Integer> colIncompletos;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colEstado;

    private Trabajador trabajadorActual;
    private final TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        configurarTabla();
        configurarMeses();
        configurarAnios();
        cargarEstadisticas();
    }

    private void configurarTabla() {
        if (colNombre != null) colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        if (colTarjeta != null) colTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        if (colDiasTrabajados != null) colDiasTrabajados.setCellValueFactory(new PropertyValueFactory<>("diasTrabajados"));
        if (colPromedioDiario != null) colPromedioDiario.setCellValueFactory(new PropertyValueFactory<>("promedioDiario"));
        if (colIncompletos != null) colIncompletos.setCellValueFactory(new PropertyValueFactory<>("fichajesIncompletos"));
        if (colEstado != null) colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        if (colTotalHoras != null) {
            colTotalHoras.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
            colTotalHoras.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Double horas, boolean empty) {
                    super.updateItem(horas, empty);
                    setText(empty || horas == null ? null : HorasFormateador.formatearHoras(horas));
                }
            });
        }

        if (colPromedioDiario != null) {
            colPromedioDiario.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Double horas, boolean empty) {
                    super.updateItem(horas, empty);
                    setText(empty || horas == null ? null : HorasFormateador.formatearHorasDecimal(horas));
                }
            });
        }

        if (colEstado != null) {
            colEstado.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String estado, boolean empty) {
                    super.updateItem(estado, empty);
                    if (empty || estado == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(estado);
                        if (estado.equals("Activo")) {
                            setStyle("-fx-text-fill: #2e7d32;");
                        } else {
                            setStyle("-fx-text-fill: #757575;");
                        }
                    }
                }
            });
        }
    }

    private void configurarMeses() {
        if (cbMes == null) {
            System.out.println("⚠️ cbMes no disponible");
            return;
        }

        cbMes.getItems().clear();

        for (Month mes : Month.values()) {
            String nombreMes = mes.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
            cbMes.getItems().add(nombreMes);
        }

        int mesActual = LocalDate.now().getMonthValue();
        cbMes.setValue(cbMes.getItems().get(mesActual - 1));
    }

    private void configurarAnios() {
        if (cbAnio == null) {
            System.out.println("⚠️ cbAnio no disponible");
            return;
        }

        cbAnio.getItems().clear();

        int anioActual = LocalDate.now().getYear();

        for (int i = anioActual; i >= anioActual - 5; i--) {
            cbAnio.getItems().add(i);
        }

        cbAnio.setValue(anioActual);
    }

    private void cargarEstadisticas() {
        if (cbMes == null || cbAnio == null) {
            System.out.println("⚠️ ComboBoxes no disponibles");
            return;
        }
        String mesSeleccionado = cbMes.getValue();
        Integer anioSeleccionado = cbAnio.getValue();

        if (mesSeleccionado == null || anioSeleccionado == null) {
            AlertasUtil.mostrarError("Error", "Seleccione mes y año");
            return;
        }
        int numeroMes = cbMes.getItems().indexOf(mesSeleccionado) + 1;
        LocalDate primerDia = LocalDate.of(anioSeleccionado, numeroMes, 1);
        LocalDate ultimoDia = primerDia.withDayOfMonth(primerDia.lengthOfMonth());

        try {
            List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();
            List<EstadisticaEmpleadoDTO> estadisticas = new ArrayList<>();

            for (Trabajador t : trabajadores) {
                List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                        t.getId(), primerDia, ultimoDia
                );

                if (fichajes.isEmpty()) {
                    estadisticas.add(new EstadisticaEmpleadoDTO(
                            t.getId(),
                            t.getNombreCompleto(),
                            t.getNumeroTarjeta(),
                            0, 0.0, 0.0, 0,
                            "Sin fichajes"
                    ));
                } else {
                    int diasTrabajados = (int) fichajes.stream()
                            .map(f -> f.getFechaHora().toLocalDate())
                            .distinct()
                            .count();

                    double totalHoras = calcularTotalHorasCorregido(fichajes);
                    double promedio = diasTrabajados > 0 ? totalHoras / diasTrabajados : 0.0;
                    int incompletos = contarDiasIncompletos(fichajes);
                    estadisticas.add(new EstadisticaEmpleadoDTO(
                            t.getId(),
                            t.getNombreCompleto(),
                            t.getNumeroTarjeta(),
                            diasTrabajados,
                            totalHoras,
                            promedio,
                            incompletos,
                            diasTrabajados > 0 ? "Activo" : "Sin fichajes"
                    ));
                }
            }

            estadisticas.sort((a, b) -> Double.compare(b.getTotalHoras(), a.getTotalHoras()));
            if (tableEstadisticas != null) {
                tableEstadisticas.setItems(FXCollections.observableArrayList(estadisticas));
            }

            actualizarResumen(estadisticas);
            actualizarGraficoCorregido(estadisticas);
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar estadísticas");
        }
    }

    private double calcularTotalHorasCorregido(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        double totalHoras = 0.0;

        for (Map.Entry<LocalDate, List<Fichaje>> entry : porDia.entrySet()) {
            List<Fichaje> fichajesDia = entry.getValue();
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));
            List<Fichaje> entradas = new ArrayList<>();
            List<Fichaje> salidas = new ArrayList<>();

            for (Fichaje f : fichajesDia) {
                if (f.getTipo() == TipoFichaje.ENTRADA) {
                    entradas.add(f);
                } else if (f.getTipo() == TipoFichaje.SALIDA) {
                    salidas.add(f);
                }
            }

            int pares = Math.min(entradas.size(), salidas.size());
            for (int i = 0; i < pares; i++) {
                double horas = HorasFormateador.calcularHoras(
                        entradas.get(i).getFechaHora(),
                        salidas.get(i).getFechaHora()
                );
                if (horas >= 0) {
                    totalHoras += horas;
                }
            }
        }

        return totalHoras;
    }

    private int contarDiasIncompletos(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        int incompletos = 0;

        for (List<Fichaje> fichajesDia : porDia.values()) {
            long entradas = fichajesDia.stream()
                    .filter(f -> f.getTipo() == TipoFichaje.ENTRADA).count();
            long salidas = fichajesDia.stream()
                    .filter(f -> f.getTipo() == TipoFichaje.SALIDA).count();

            if (entradas != salidas) {
                incompletos++;
            }
        }

        return incompletos;
    }

    private void actualizarResumen(List<EstadisticaEmpleadoDTO> estadisticas) {
        int empleadosActivos = (int) estadisticas.stream()
                .filter(e -> e.getDiasTrabajados() > 0)
                .count();

        double totalHoras = estadisticas.stream()
                .mapToDouble(EstadisticaEmpleadoDTO::getTotalHoras)
                .sum();

        double promedioEmpleado = empleadosActivos > 0
                ? estadisticas.stream()
                .filter(e -> e.getDiasTrabajados() > 0)
                .mapToDouble(EstadisticaEmpleadoDTO::getPromedioDiario)
                .average()
                .orElse(0.0)
                : 0.0;

        String mejorEmpleado = estadisticas.stream()
                .filter(e -> e.getDiasTrabajados() > 0)
                .max(Comparator.comparingDouble(EstadisticaEmpleadoDTO::getTotalHoras))
                .map(EstadisticaEmpleadoDTO::getNombreCompleto)
                .orElse("N/A");

        if (lblTotalHoras != null) lblTotalHoras.setText(HorasFormateador.formatearHoras(totalHoras));
        if (lblEmpleadosActivos != null) lblEmpleadosActivos.setText(String.valueOf(empleadosActivos));
        if (lblPromedioEmpleado != null) lblPromedioEmpleado.setText(HorasFormateador.formatearHorasDecimal(promedioEmpleado));
        if (lblMejorEmpleado != null) lblMejorEmpleado.setText(mejorEmpleado);
    }

    private void actualizarGraficoCorregido(List<EstadisticaEmpleadoDTO> estadisticas) {
        if (chartComparativa == null) {
            System.out.println("⚠️ Gráfico no disponible");
            return;
        }

        chartComparativa.getData().clear();
        chartComparativa.layout();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Horas");
        List<EstadisticaEmpleadoDTO> top10 = estadisticas.stream()
                .filter(e -> e.getDiasTrabajados() > 0)
                .limit(10)
                .collect(Collectors.toList());

        for (EstadisticaEmpleadoDTO e : top10) {
            series.getData().add(new XYChart.Data<>(e.getNombreCompleto(), e.getTotalHoras()));
            System.out.println("   - " + e.getNombreCompleto() + ": " + e.getTotalHoras() + "h");
        }
        chartComparativa.getData().add(series);
        chartComparativa.layout();
    }

    @FXML
    private void handleBuscar() {
        cargarEstadisticas();
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }
}