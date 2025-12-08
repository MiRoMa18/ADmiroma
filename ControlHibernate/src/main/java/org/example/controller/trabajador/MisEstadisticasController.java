package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import org.example.dao.FichajeDAO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.model.enums.TipoFichaje;
import org.example.util.AlertasUtil;
import org.example.util.HorasFormateador;
import org.example.util.NavegacionUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class MisEstadisticasController {

    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    @FXML private Label lblMensualTotal;
    @FXML private Label lblMensualPromedio;
    @FXML private Label lblMensualDias;
    @FXML private Label lblMensualMax;

    @FXML private BarChart<String, Number> chartMensualBarras;
    @FXML private CategoryAxis xAxisMensual;
    @FXML private NumberAxis yAxisMensual;

    @FXML private TableView<DiaEstadistica> tableMensual;
    @FXML private TableColumn<DiaEstadistica, String> colDiaFecha;
    @FXML private TableColumn<DiaEstadistica, String> colDiaSemana;
    @FXML private TableColumn<DiaEstadistica, String> colDiaHoras;
    @FXML private TableColumn<DiaEstadistica, String> colDiaEstado;

    private Trabajador trabajadorActual;
    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        configurarTabla();
        configurarMeses();
        configurarAnios();
        cargarEstadisticas();
    }

    private void configurarTabla() {
        if (colDiaFecha != null) {
            colDiaFecha.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().fecha));
        }

        if (colDiaSemana != null) {
            colDiaSemana.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().diaSemana));
        }

        if (colDiaHoras != null) {
            colDiaHoras.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().horas));
        }

        if (colDiaEstado != null) {
            colDiaEstado.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().estado));
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
            List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                    trabajadorActual.getId(),
                    primerDia,
                    ultimoDia
            );

            Map<LocalDate, Double> horasPorDia = calcularHorasPorDiaCorregido(fichajes);
            actualizarLabels(horasPorDia);
            actualizarGraficoCorregido(horasPorDia);
            actualizarTabla(horasPorDia, fichajes);
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar estadísticas");
        }
    }

    private Map<LocalDate, Double> calcularHorasPorDiaCorregido(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        Map<LocalDate, Double> horasPorDia = new LinkedHashMap<>();

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

            double horasDia = 0.0;
            int pares = Math.min(entradas.size(), salidas.size());

            for (int i = 0; i < pares; i++) {
                double horas = HorasFormateador.calcularHoras(
                        entradas.get(i).getFechaHora(),
                        salidas.get(i).getFechaHora()
                );
                if (horas >= 0) {
                    horasDia += horas;
                }
            }
            horasPorDia.put(entry.getKey(), horasDia);
        }

        return horasPorDia;
    }

    private void actualizarLabels(Map<LocalDate, Double> horasPorDia) {
        double totalHoras = horasPorDia.values().stream().mapToDouble(Double::doubleValue).sum();
        int diasTrabajados = horasPorDia.size();
        double promedioDiario = diasTrabajados > 0 ? totalHoras / diasTrabajados : 0.0;
        double diaMaximo = horasPorDia.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        if (lblMensualTotal != null) {
            lblMensualTotal.setText(HorasFormateador.formatearHoras(totalHoras));
        }

        if (lblMensualPromedio != null) {
            lblMensualPromedio.setText(HorasFormateador.formatearHorasDecimal(promedioDiario));
        }

        if (lblMensualDias != null) {
            lblMensualDias.setText(String.valueOf(diasTrabajados));
        }

        if (lblMensualMax != null) {
            lblMensualMax.setText(HorasFormateador.formatearHoras(diaMaximo));
        }
    }
    private void actualizarGraficoCorregido(Map<LocalDate, Double> horasPorDia) {
        if (chartMensualBarras == null) {
            System.out.println("⚠️ Gráfico no disponible");
            return;
        }

        chartMensualBarras.getData().clear();
        chartMensualBarras.layout();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Horas");
        // Ordenar por fecha y agregar al gráfico
        horasPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String fecha = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
                    double horas = entry.getValue();
                    series.getData().add(new XYChart.Data<>(fecha, horas));
                    System.out.println("   - " + fecha + ": " + HorasFormateador.formatearHoras(horas));
                });

        chartMensualBarras.getData().add(series);
        chartMensualBarras.layout();
    }

    private void actualizarTabla(Map<LocalDate, Double> horasPorDia, List<Fichaje> fichajes) {
        if (tableMensual == null) return;

        List<DiaEstadistica> estadisticas = new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entry : horasPorDia.entrySet()) {
            LocalDate fecha = entry.getKey();
            Double horas = entry.getValue();

            long entradas = fichajes.stream()
                    .filter(f -> f.getFechaHora().toLocalDate().equals(fecha))
                    .filter(f -> f.getTipo() == TipoFichaje.ENTRADA)
                    .count();

            long salidas = fichajes.stream()
                    .filter(f -> f.getFechaHora().toLocalDate().equals(fecha))
                    .filter(f -> f.getTipo() == TipoFichaje.SALIDA)
                    .count();

            String estado = (entradas == salidas) ? "Completo" : "Incompleto";

            DayOfWeek dia = fecha.getDayOfWeek();
            String diaSemana = dia.getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
            diaSemana = diaSemana.substring(0, 1).toUpperCase() + diaSemana.substring(1);

            estadisticas.add(new DiaEstadistica(
                    fecha.toString(),
                    diaSemana,
                    HorasFormateador.formatearHoras(horas),
                    estado
            ));
        }
        estadisticas.sort((a, b) -> b.fecha.compareTo(a.fecha));

        tableMensual.setItems(FXCollections.observableArrayList(estadisticas));
    }

    @FXML
    private void handleBuscar() {
        System.out.println("🔍 Búsqueda manual");
        cargarEstadisticas();
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }

    public static class DiaEstadistica {
        private final String fecha;
        private final String diaSemana;
        private final String horas;
        private final String estado;

        public DiaEstadistica(String fecha, String diaSemana, String horas, String estado) {
            this.fecha = fecha;
            this.diaSemana = diaSemana;
            this.horas = horas;
            this.estado = estado;
        }

        public String getFecha() { return fecha; }
        public String getDiaSemana() { return diaSemana; }
        public String getHoras() { return horas; }
        public String getEstado() { return estado; }
    }
}