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

/**
 * Controlador Mis Estadísticas (TRABAJADOR).
 * CORREGIDO: Sin horas negativas + gráfico funcional primera vez.
 */
public class MisEstadisticasController {

    // FILTROS
    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    // TAB MENSUAL - Labels
    @FXML private Label lblMensualTotal;
    @FXML private Label lblMensualPromedio;
    @FXML private Label lblMensualDias;
    @FXML private Label lblMensualMax;

    // TAB MENSUAL - Gráfico
    @FXML private BarChart<String, Number> chartMensualBarras;
    @FXML private CategoryAxis xAxisMensual;
    @FXML private NumberAxis yAxisMensual;

    // TAB MENSUAL - Tabla
    @FXML private TableView<DiaEstadistica> tableMensual;
    @FXML private TableColumn<DiaEstadistica, String> colDiaFecha;
    @FXML private TableColumn<DiaEstadistica, String> colDiaSemana;
    @FXML private TableColumn<DiaEstadistica, String> colDiaHoras;
    @FXML private TableColumn<DiaEstadistica, String> colDiaEstado;

    private Trabajador trabajadorActual;
    private final FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;
        System.out.println("📊 MisEstadisticasController inicializado para: " + trabajador.getNombreCompleto());

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

        System.out.println("✅ ComboBox meses configurado");
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

        System.out.println("✅ ComboBox años configurado");
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

        System.out.println("📊 Cargando estadísticas:");
        System.out.println("   Mes: " + mesSeleccionado + " (" + numeroMes + ")");
        System.out.println("   Año: " + anioSeleccionado);
        System.out.println("   Rango: " + primerDia + " - " + ultimoDia);

        try {
            List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                    trabajadorActual.getId(),
                    primerDia,
                    ultimoDia
            );

            // Calcular estadísticas por día (CORREGIDO)
            Map<LocalDate, Double> horasPorDia = calcularHorasPorDiaCorregido(fichajes);

            // Actualizar labels
            actualizarLabels(horasPorDia);

            // Actualizar gráfico (CORREGIDO)
            actualizarGraficoCorregido(horasPorDia);

            // Actualizar tabla
            actualizarTabla(horasPorDia, fichajes);

            System.out.println("✅ Estadísticas cargadas");

        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar estadísticas");
        }
    }

    /**
     * CORREGIDO: Calcula horas por día sin valores negativos.
     */
    private Map<LocalDate, Double> calcularHorasPorDiaCorregido(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        Map<LocalDate, Double> horasPorDia = new LinkedHashMap<>();

        for (Map.Entry<LocalDate, List<Fichaje>> entry : porDia.entrySet()) {
            List<Fichaje> fichajesDia = entry.getValue();

            // CRÍTICO: Ordenar por hora ANTES de separar
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            // Separar entradas y salidas YA ORDENADAS
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

                // VALIDACIÓN: Solo sumar si es positivo
                if (horas >= 0) {
                    horasDia += horas;
                } else {
                    System.out.println("   ⚠️ Horas negativas en " + entry.getKey() +
                            ": " + entradas.get(i).getFechaHora().toLocalTime() +
                            " - " + salidas.get(i).getFechaHora().toLocalTime() +
                            " (ignorado)");
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

        System.out.println("📊 Resumen:");
        System.out.println("   Total: " + HorasFormateador.formatearHoras(totalHoras));
        System.out.println("   Días: " + diasTrabajados);
        System.out.println("   Promedio: " + HorasFormateador.formatearHorasDecimal(promedioDiario));
        System.out.println("   Máximo: " + HorasFormateador.formatearHoras(diaMaximo));
    }

    /**
     * CORREGIDO: Actualiza gráfico correctamente la primera vez.
     */
    private void actualizarGraficoCorregido(Map<LocalDate, Double> horasPorDia) {
        if (chartMensualBarras == null) {
            System.out.println("⚠️ Gráfico no disponible");
            return;
        }

        // CRÍTICO: Limpiar COMPLETAMENTE el gráfico
        chartMensualBarras.getData().clear();

        // CRÍTICO: Forzar layout para que JavaFX actualice el eje X
        chartMensualBarras.layout();

        // Crear nueva serie
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Horas");

        System.out.println("📈 Actualizando gráfico con " + horasPorDia.size() + " días");

        // Ordenar por fecha y agregar al gráfico
        horasPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String fecha = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
                    double horas = entry.getValue();
                    series.getData().add(new XYChart.Data<>(fecha, horas));
                    System.out.println("   - " + fecha + ": " + HorasFormateador.formatearHoras(horas));
                });

        // Agregar serie al gráfico
        chartMensualBarras.getData().add(series);

        // CRÍTICO: Forzar un segundo layout después de agregar datos
        chartMensualBarras.layout();

        System.out.println("✅ Gráfico actualizado correctamente");
    }

    private void actualizarTabla(Map<LocalDate, Double> horasPorDia, List<Fichaje> fichajes) {
        if (tableMensual == null) return;

        List<DiaEstadistica> estadisticas = new ArrayList<>();

        for (Map.Entry<LocalDate, Double> entry : horasPorDia.entrySet()) {
            LocalDate fecha = entry.getKey();
            Double horas = entry.getValue();

            // Verificar si el día está incompleto
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

        // Ordenar por fecha descendente
        estadisticas.sort((a, b) -> b.fecha.compareTo(a.fecha));

        tableMensual.setItems(FXCollections.observableArrayList(estadisticas));

        System.out.println("📋 Tabla actualizada con " + estadisticas.size() + " filas");
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

    /**
     * Clase interna para representar estadísticas de un día.
     */
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