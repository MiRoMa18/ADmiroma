package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.model.Fichaje;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class MisEstadisticasController {

    @FXML
    private ComboBox<String> cbFiltro;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private Button btnBuscar;

    @FXML
    private Label lblTotalMes;

    @FXML
    private Label lblPromedioDiario;

    @FXML
    private Label lblDiasTrabajados;

    @FXML
    private Label lblRachaActual;

    @FXML
    private BarChart<String, Number> chartPorDia;

    @FXML
    private LineChart<String, Number> chartEvolucion;

    @FXML
    private Button btnVolver;

    private Trabajador trabajadorActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();

    /**
     * Configurar ejes de los gráficos
     */
    private void configurarEjes() {
        // Configurar eje Y del gráfico de barras
        chartPorDia.getYAxis().setLabel("Horas trabajadas");

        // Configurar eje X del gráfico de barras
        chartPorDia.getXAxis().setLabel("Día de la semana");

        // Configurar eje Y del gráfico de líneas
        chartEvolucion.getYAxis().setLabel("Horas trabajadas");

        // Configurar eje X del gráfico de líneas
        chartEvolucion.getXAxis().setLabel("Fecha");

        System.out.println("✅ Ejes configurados");
    }

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        // Configurar ComboBox de filtros
        cbFiltro.setItems(FXCollections.observableArrayList("Semana", "Mes", "Año"));
        cbFiltro.setValue("Mes");

        // Configurar DatePicker con fecha actual
        dpFecha.setValue(LocalDate.now());

        // ⭐ AGREGAR ESTA LÍNEA:
        configurarEjes();

        System.out.println("✅ Vista 'Mis Estadísticas' cargada para: " + trabajador.getNombre());

        // Cargar estadísticas iniciales
        cargarEstadisticas();
    }

    @FXML
    private void handleBuscar() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        String filtro = cbFiltro.getValue();
        LocalDate fechaSeleccionada = dpFecha.getValue();

        if (fechaSeleccionada == null) {
            mostrarAlerta("Error", "Por favor selecciona una fecha");
            return;
        }

        System.out.println("🔍 Cargando estadísticas - Filtro: " + filtro + ", Fecha: " + fechaSeleccionada);

        // Calcular rango de fechas según filtro
        LocalDate fechaInicio;
        LocalDate fechaFin;

        switch (filtro) {
            case "Semana":
                fechaInicio = fechaSeleccionada.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;

            case "Mes":
                fechaInicio = fechaSeleccionada.withDayOfMonth(1);
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.lastDayOfMonth());
                break;

            case "Año":
                fechaInicio = fechaSeleccionada.withDayOfYear(1);
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.lastDayOfYear());
                break;

            default:
                fechaInicio = fechaSeleccionada.withDayOfMonth(1);
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.lastDayOfMonth());
        }

        System.out.println("   Rango calculado: " + fechaInicio + " a " + fechaFin);

        // Obtener fichajes del rango
        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(),
                fechaInicio,
                fechaFin
        );

        System.out.println("   📊 Fichajes obtenidos: " + fichajes.size());

        // Calcular estadísticas principales
        calcularEstadisticasPrincipales(fichajes);

        // Generar gráfico de barras por día de la semana
        generarGraficoPorDia(fichajes);

        // Generar gráfico de evolución
        generarGraficoEvolucion(fichajes, filtro);

        System.out.println("📊 Estadísticas calculadas correctamente");
    }

    private void calcularEstadisticasPrincipales(List<Fichaje> fichajes) {
        // Agrupar por día
        Map<LocalDate, List<Fichaje>> fichajesPorDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        double totalHoras = 0.0;
        int diasCompletos = 0;
        List<LocalDate> fechasConFichajes = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Fichaje>> entry : fichajesPorDia.entrySet()) {
            List<Fichaje> fichajesDia = entry.getValue();
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            double horasDia = calcularHorasDia(fichajesDia);

            if (horasDia > 0) { // Solo días completos
                totalHoras += horasDia;
                diasCompletos++;
                fechasConFichajes.add(entry.getKey());
            }
        }

        // Calcular racha actual (días consecutivos trabajados)
        fechasConFichajes.sort(Comparator.reverseOrder());
        int racha = calcularRachaActual(fechasConFichajes);

        // Actualizar labels
        lblTotalMes.setText(String.format("%.2f h", totalHoras));
        lblDiasTrabajados.setText(diasCompletos + " días");

        if (diasCompletos > 0) {
            double promedio = totalHoras / diasCompletos;
            lblPromedioDiario.setText(String.format("%.2f h", promedio));
        } else {
            lblPromedioDiario.setText("0.00 h");
        }

        lblRachaActual.setText(racha + " días");

        System.out.println("   Total horas: " + totalHoras);
        System.out.println("   Días trabajados: " + diasCompletos);
        System.out.println("   Promedio: " + (diasCompletos > 0 ? totalHoras/diasCompletos : 0));
    }

    private double calcularHorasDia(List<Fichaje> fichajes) {
        long entradas = fichajes.stream().filter(f -> f.getTipo().name().equals("ENTRADA")).count();
        long salidas = fichajes.stream().filter(f -> f.getTipo().name().equals("SALIDA")).count();

        if (entradas != salidas) {
            return -1.0; // Incompleto
        }

        double totalHoras = 0.0;

        for (int i = 0; i < fichajes.size() - 1; i += 2) {
            LocalDateTime entrada = fichajes.get(i).getFechaHora();
            LocalDateTime salida = fichajes.get(i + 1).getFechaHora();

            Duration duracion = Duration.between(entrada, salida);
            totalHoras += duracion.toMinutes() / 60.0;
        }

        return totalHoras;
    }

    private int calcularRachaActual(List<LocalDate> fechasOrdenadas) {
        if (fechasOrdenadas.isEmpty()) {
            return 0;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        // Si no trabajó hoy ni ayer, racha = 0
        if (!fechasOrdenadas.contains(hoy) && !fechasOrdenadas.contains(ayer)) {
            return 0;
        }

        int racha = 0;
        LocalDate fechaEsperada = fechasOrdenadas.contains(hoy) ? hoy : ayer;

        for (LocalDate fecha : fechasOrdenadas) {
            if (fecha.equals(fechaEsperada)) {
                racha++;
                fechaEsperada = fechaEsperada.minusDays(1);
            } else {
                break;
            }
        }

        return racha;
    }

    private void generarGraficoPorDia(List<Fichaje> fichajes) {
        // Agrupar por día de la semana
        Map<DayOfWeek, Double> horasPorDia = new HashMap<>();

        // Inicializar todos los días en 0
        for (DayOfWeek dia : DayOfWeek.values()) {
            horasPorDia.put(dia, 0.0);
        }

        // Agrupar fichajes por fecha
        Map<LocalDate, List<Fichaje>> fichajesPorFecha = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        // Calcular horas por día de la semana
        for (Map.Entry<LocalDate, List<Fichaje>> entry : fichajesPorFecha.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<Fichaje> fichajesDia = entry.getValue();
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            double horas = calcularHorasDia(fichajesDia);

            if (horas > 0) {
                DayOfWeek diaSemana = fecha.getDayOfWeek();
                horasPorDia.put(diaSemana, horasPorDia.get(diaSemana) + horas);
            }
        }

        // Crear serie para el gráfico
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas trabajadas");

        // Agregar datos en orden (Lunes a Domingo)
        DayOfWeek[] diasOrdenados = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };

        for (DayOfWeek dia : diasOrdenados) {
            String nombreDia = dia.getDisplayName(TextStyle.SHORT, Locale.getDefault());
            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(nombreDia, horasPorDia.get(dia));
            serie.getData().add(dataPoint);
        }

        // ⭐ CAMBIO: Usar Platform.runLater
        javafx.application.Platform.runLater(() -> {
            chartPorDia.getData().clear();
            chartPorDia.getData().add(serie);
            chartPorDia.setTitle("Horas por día de la semana");
        });

        System.out.println("   📊 Gráfico de barras generado");
    }

    private void generarGraficoEvolucion(List<Fichaje> fichajes, String filtro) {
        // Agrupar por fecha
        Map<LocalDate, List<Fichaje>> fichajesPorFecha = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        // Calcular horas por día
        Map<LocalDate, Double> horasPorFecha = new TreeMap<>();

        for (Map.Entry<LocalDate, List<Fichaje>> entry : fichajesPorFecha.entrySet()) {
            List<Fichaje> fichajesDia = entry.getValue();
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            double horas = calcularHorasDia(fichajesDia);

            if (horas > 0) {
                horasPorFecha.put(entry.getKey(), horas);
            }
        }

        // Crear serie para el gráfico
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas diarias");

        // Agregar datos según el filtro
        if (filtro.equals("Año")) {
            // Para año: agrupar por mes
            Map<Integer, Double> horasPorMes = new TreeMap<>();
            for (int i = 1; i <= 12; i++) {
                horasPorMes.put(i, 0.0);
            }

            for (Map.Entry<LocalDate, Double> entry : horasPorFecha.entrySet()) {
                int mes = entry.getKey().getMonthValue();
                horasPorMes.put(mes, horasPorMes.get(mes) + entry.getValue());
            }

            String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
            for (int i = 1; i <= 12; i++) {
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(meses[i-1], horasPorMes.get(i));
                serie.getData().add(dataPoint);
            }

        } else {
            // Para semana/mes: mostrar día a día
            for (Map.Entry<LocalDate, Double> entry : horasPorFecha.entrySet()) {
                String fechaStr = entry.getKey().getDayOfMonth() + "/" + entry.getKey().getMonthValue();
                XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(fechaStr, entry.getValue());
                serie.getData().add(dataPoint);
            }
        }

        // ⭐ CAMBIO: Usar Platform.runLater
        javafx.application.Platform.runLater(() -> {
            chartEvolucion.getData().clear();
            chartEvolucion.getData().add(serie);
            chartEvolucion.setTitle("Evolución del período");
        });

        System.out.println("   📈 Gráfico de evolución generado");
    }

    @FXML
    private void handleVolver() {
        System.out.println("🔙 Volviendo al dashboard...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            org.example.controller.DashboardController controller = loader.getController();
            controller.inicializar(trabajadorActual);

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error al volver al dashboard: " + e.getMessage());
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