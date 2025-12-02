package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.model.Fichaje;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class MisEstadisticasController {

    // === FILTROS ===
    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    // === TABS ===
    @FXML private TabPane tabPane;
    @FXML private Tab tabMensual;
    @FXML private Tab tabSemanal;
    @FXML private Tab tabAnual;

    // === TAB MENSUAL ===
    @FXML private Label lblMensualTotal;
    @FXML private Label lblMensualPromedio;
    @FXML private Label lblMensualDias;
    @FXML private Label lblMensualMax;
    @FXML private BarChart<String, Number> chartMensualBarras;
    @FXML private CategoryAxis xAxisMensual;
    @FXML private NumberAxis yAxisMensual;
    @FXML private TableView<EstadisticaDiaDTO> tableMensual;
    @FXML private TableColumn<EstadisticaDiaDTO, String> colMensualFecha;
    @FXML private TableColumn<EstadisticaDiaDTO, String> colMensualDia;
    @FXML private TableColumn<EstadisticaDiaDTO, Double> colMensualHoras;
    @FXML private TableColumn<EstadisticaDiaDTO, String> colMensualEstado;

    // === TAB SEMANAL ===
    @FXML private Label lblSemanalTotal;
    @FXML private Label lblSemanalPromedio;
    @FXML private Label lblSemanalSemanas;
    @FXML private Label lblSemanalMejor;
    @FXML private BarChart<String, Number> chartSemanalBarras;
    @FXML private CategoryAxis xAxisSemanal;
    @FXML private NumberAxis yAxisSemanal;
    @FXML private PieChart chartSemanalPie;
    @FXML private TableView<EstadisticaSemanaDTO> tableSemanal;
    @FXML private TableColumn<EstadisticaSemanaDTO, Integer> colSemanalNumero;
    @FXML private TableColumn<EstadisticaSemanaDTO, String> colSemanalRango;
    @FXML private TableColumn<EstadisticaSemanaDTO, Integer> colSemanalDias;
    @FXML private TableColumn<EstadisticaSemanaDTO, Double> colSemanalHoras;
    @FXML private TableColumn<EstadisticaSemanaDTO, Double> colSemanalPromedio;

    // === TAB ANUAL ===
    @FXML private Label lblAnualTotal;
    @FXML private Label lblAnualPromedio;
    @FXML private Label lblAnualMeses;
    @FXML private Label lblAnualMejor;
    @FXML private BarChart<String, Number> chartAnualBarras;
    @FXML private CategoryAxis xAxisAnual;
    @FXML private NumberAxis yAxisAnual;
    @FXML private LineChart<String, Number> chartAnualLinea;
    @FXML private CategoryAxis xAxisAnualLinea;
    @FXML private NumberAxis yAxisAnualLinea;
    @FXML private TableView<EstadisticaMesDTO> tableAnual;
    @FXML private TableColumn<EstadisticaMesDTO, String> colAnualMes;
    @FXML private TableColumn<EstadisticaMesDTO, Integer> colAnualDias;
    @FXML private TableColumn<EstadisticaMesDTO, Double> colAnualHoras;
    @FXML private TableColumn<EstadisticaMesDTO, Double> colAnualPromedio;

    private Trabajador trabajadorActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        // Configurar ComboBox de meses
        cbMes.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
        cbMes.setValue(obtenerNombreMes(LocalDate.now().getMonthValue()));

        // Configurar ComboBox de años (últimos 5 años)
        ObservableList<Integer> anios = FXCollections.observableArrayList();
        int anioActual = LocalDate.now().getYear();
        for (int i = anioActual; i >= anioActual - 5; i--) {
            anios.add(i);
        }
        cbAnio.setItems(anios);
        cbAnio.setValue(anioActual);

        // Configurar tablas
        configurarTablas();

        // Cargar datos iniciales
        cargarEstadisticas();

        System.out.println("✅ Vista 'Mis Estadísticas' cargada para: " + trabajador.getNombre());
    }

    private void configurarTablas() {
        // === TABLA MENSUAL ===
        colMensualFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colMensualDia.setCellValueFactory(new PropertyValueFactory<>("diaSemana"));
        colMensualHoras.setCellValueFactory(new PropertyValueFactory<>("horas"));
        colMensualEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        colMensualHoras.setCellFactory(col -> new TableCell<EstadisticaDiaDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatearHoras(horas));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        colMensualEstado.setCellFactory(col -> new TableCell<EstadisticaDiaDTO, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if (estado.equals("✅ Completo")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // === TABLA SEMANAL ===
        colSemanalNumero.setCellValueFactory(new PropertyValueFactory<>("numeroSemana"));
        colSemanalRango.setCellValueFactory(new PropertyValueFactory<>("rangoFechas"));
        colSemanalDias.setCellValueFactory(new PropertyValueFactory<>("diasTrabajados"));
        colSemanalHoras.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
        colSemanalPromedio.setCellValueFactory(new PropertyValueFactory<>("promedioDiario"));

        colSemanalHoras.setCellFactory(col -> new TableCell<EstadisticaSemanaDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null) {
                    setText(null);
                } else {
                    setText(formatearHoras(horas));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        });

        colSemanalPromedio.setCellFactory(col -> new TableCell<EstadisticaSemanaDTO, Double>() {
            @Override
            protected void updateItem(Double promedio, boolean empty) {
                super.updateItem(promedio, empty);
                if (empty || promedio == null) {
                    setText(null);
                } else {
                    setText(formatearHoras(promedio));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        // === TABLA ANUAL ===
        colAnualMes.setCellValueFactory(new PropertyValueFactory<>("nombreMes"));
        colAnualDias.setCellValueFactory(new PropertyValueFactory<>("diasTrabajados"));
        colAnualHoras.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
        colAnualPromedio.setCellValueFactory(new PropertyValueFactory<>("promedioDiario"));

        colAnualHoras.setCellFactory(col -> new TableCell<EstadisticaMesDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null) {
                    setText(null);
                } else {
                    setText(formatearHoras(horas));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        });

        colAnualPromedio.setCellFactory(col -> new TableCell<EstadisticaMesDTO, Double>() {
            @Override
            protected void updateItem(Double promedio, boolean empty) {
                super.updateItem(promedio, empty);
                if (empty || promedio == null) {
                    setText(null);
                } else {
                    setText(formatearHoras(promedio));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });
    }

    @FXML
    private void handleBuscar() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        int mes = cbMes.getSelectionModel().getSelectedIndex() + 1;
        int anio = cbAnio.getValue();

        System.out.println("🔍 Cargando estadísticas - Mes: " + mes + ", Año: " + anio);

        cargarEstadisticasMensuales(mes, anio);
        cargarEstadisticasSemanales(mes, anio);
        cargarEstadisticasAnuales(anio);
    }

    // ============================================
    // ESTADÍSTICAS MENSUALES
    // ============================================
    private void cargarEstadisticasMensuales(int mes, int anio) {
        LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
        LocalDate fechaFin = fechaInicio.with(TemporalAdjusters.lastDayOfMonth());

        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(), fechaInicio, fechaFin
        );

        Map<LocalDate, List<Fichaje>> fichajesPorDia = agruparPorDia(fichajes);
        Map<LocalDate, Double> horasPorDia = calcularHorasPorDia(fichajesPorDia);

        // Calcular estadísticas
        double totalHoras = horasPorDia.values().stream().mapToDouble(Double::doubleValue).sum();
        int diasTrabajados = horasPorDia.size();
        double promedio = diasTrabajados > 0 ? totalHoras / diasTrabajados : 0.0;
        double maxHoras = horasPorDia.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        // Actualizar labels
        lblMensualTotal.setText(formatearHoras(totalHoras));
        lblMensualPromedio.setText(formatearHoras(promedio));
        lblMensualDias.setText(String.valueOf(diasTrabajados));
        lblMensualMax.setText(formatearHoras(maxHoras));

        // Actualizar gráfico de barras
        actualizarGraficoMensual(horasPorDia);

        // Actualizar tabla
        actualizarTablaMensual(fichajesPorDia, horasPorDia);
    }

    private void actualizarGraficoMensual(Map<LocalDate, Double> horasPorDia) {
        chartMensualBarras.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas Trabajadas");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        horasPorDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String fecha = entry.getKey().format(formatter);
                    serie.getData().add(new XYChart.Data<>(fecha, entry.getValue()));
                });

        chartMensualBarras.getData().add(serie);
    }

    private void actualizarTablaMensual(Map<LocalDate, List<Fichaje>> fichajesPorDia,
                                        Map<LocalDate, Double> horasPorDia) {
        ObservableList<EstadisticaDiaDTO> items = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        horasPorDia.entrySet().stream()
                .sorted(Map.Entry.<LocalDate, Double>comparingByKey().reversed())
                .forEach(entry -> {
                    LocalDate fecha = entry.getKey();
                    Double horas = entry.getValue();

                    List<Fichaje> fichajesDia = fichajesPorDia.get(fecha);
                    boolean completo = esJornadaCompleta(fichajesDia);

                    String diaSemana = fecha.getDayOfWeek()
                            .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));

                    EstadisticaDiaDTO dto = new EstadisticaDiaDTO(
                            fecha.format(formatter),
                            diaSemana,
                            horas,
                            completo ? "✅ Completo" : "⚠️ Incompleto"
                    );
                    items.add(dto);
                });

        tableMensual.setItems(items);
    }

    // ============================================
    // ESTADÍSTICAS SEMANALES
    // ============================================
    private void cargarEstadisticasSemanales(int mes, int anio) {
        LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
        LocalDate fechaFin = fechaInicio.with(TemporalAdjusters.lastDayOfMonth());

        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(), fechaInicio, fechaFin
        );

        Map<LocalDate, List<Fichaje>> fichajesPorDia = agruparPorDia(fichajes);
        Map<LocalDate, Double> horasPorDia = calcularHorasPorDia(fichajesPorDia);

        // Agrupar por semana
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        Map<Integer, List<Map.Entry<LocalDate, Double>>> horasPorSemana = horasPorDia.entrySet().stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().get(weekFields.weekOfMonth())));

        // Calcular totales por semana
        Map<Integer, Double> totalesPorSemana = new LinkedHashMap<>();
        Map<Integer, Integer> diasPorSemana = new LinkedHashMap<>();
        Map<Integer, String> rangosPorSemana = new LinkedHashMap<>();

        horasPorSemana.forEach((semana, dias) -> {
            double total = dias.stream().mapToDouble(Map.Entry::getValue).sum();
            totalesPorSemana.put(semana, total);
            diasPorSemana.put(semana, dias.size());

            LocalDate primerDia = dias.stream().map(Map.Entry::getKey).min(LocalDate::compareTo).orElse(fechaInicio);
            LocalDate ultimoDia = dias.stream().map(Map.Entry::getKey).max(LocalDate::compareTo).orElse(fechaFin);
            rangosPorSemana.put(semana, primerDia.getDayOfMonth() + " - " + ultimoDia.getDayOfMonth());
        });

        // Calcular estadísticas
        double totalHoras = totalesPorSemana.values().stream().mapToDouble(Double::doubleValue).sum();
        int totalSemanas = totalesPorSemana.size();
        double promedio = totalSemanas > 0 ? totalHoras / totalSemanas : 0.0;
        double mejorSemana = totalesPorSemana.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        // Actualizar labels
        lblSemanalTotal.setText(formatearHoras(totalHoras));
        lblSemanalPromedio.setText(formatearHoras(promedio));
        lblSemanalSemanas.setText(String.valueOf(totalSemanas));
        lblSemanalMejor.setText(formatearHoras(mejorSemana));

        // Actualizar gráficos
        actualizarGraficoSemanal(totalesPorSemana);
        actualizarGraficoSemanalPie(totalesPorSemana);

        // Actualizar tabla
        actualizarTablaSemanal(totalesPorSemana, diasPorSemana, rangosPorSemana);
    }

    private void actualizarGraficoSemanal(Map<Integer, Double> totalesPorSemana) {
        chartSemanalBarras.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas por Semana");

        totalesPorSemana.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    serie.getData().add(new XYChart.Data<>("Semana " + entry.getKey(), entry.getValue()));
                });

        chartSemanalBarras.getData().add(serie);
    }

    private void actualizarGraficoSemanalPie(Map<Integer, Double> totalesPorSemana) {
        chartSemanalPie.getData().clear();

        totalesPorSemana.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    PieChart.Data data = new PieChart.Data(
                            "Semana " + entry.getKey() + " (" + formatearHoras(entry.getValue()) + ")",
                            entry.getValue()
                    );
                    chartSemanalPie.getData().add(data);
                });
    }

    private void actualizarTablaSemanal(Map<Integer, Double> totalesPorSemana,
                                        Map<Integer, Integer> diasPorSemana,
                                        Map<Integer, String> rangosPorSemana) {
        ObservableList<EstadisticaSemanaDTO> items = FXCollections.observableArrayList();

        totalesPorSemana.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    int semana = entry.getKey();
                    double total = entry.getValue();
                    int dias = diasPorSemana.getOrDefault(semana, 0);
                    double promedio = dias > 0 ? total / dias : 0.0;

                    EstadisticaSemanaDTO dto = new EstadisticaSemanaDTO(
                            semana,
                            rangosPorSemana.getOrDefault(semana, ""),
                            dias,
                            total,
                            promedio
                    );
                    items.add(dto);
                });

        tableSemanal.setItems(items);
    }

    // ============================================
    // ESTADÍSTICAS ANUALES
    // ============================================
    private void cargarEstadisticasAnuales(int anio) {
        LocalDate fechaInicio = LocalDate.of(anio, 1, 1);
        LocalDate fechaFin = LocalDate.of(anio, 12, 31);

        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(), fechaInicio, fechaFin
        );

        Map<LocalDate, List<Fichaje>> fichajesPorDia = agruparPorDia(fichajes);
        Map<LocalDate, Double> horasPorDia = calcularHorasPorDia(fichajesPorDia);

        // Agrupar por mes
        Map<Integer, Double> horasPorMes = new LinkedHashMap<>();
        Map<Integer, Integer> diasPorMes = new LinkedHashMap<>();

        for (int mes = 1; mes <= 12; mes++) {
            int mesFinal = mes;
            double totalMes = horasPorDia.entrySet().stream()
                    .filter(entry -> entry.getKey().getMonthValue() == mesFinal)
                    .mapToDouble(Map.Entry::getValue)
                    .sum();

            long diasMes = horasPorDia.keySet().stream()
                    .filter(fecha -> fecha.getMonthValue() == mesFinal)
                    .count();

            horasPorMes.put(mes, totalMes);
            diasPorMes.put(mes, (int) diasMes);
        }

        // Calcular estadísticas
        double totalHoras = horasPorMes.values().stream().mapToDouble(Double::doubleValue).sum();
        long mesesTrabajados = horasPorMes.values().stream().filter(h -> h > 0).count();
        double promedio = mesesTrabajados > 0 ? totalHoras / mesesTrabajados : 0.0;
        double mejorMes = horasPorMes.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

        // Actualizar labels
        lblAnualTotal.setText(formatearHoras(totalHoras));
        lblAnualPromedio.setText(formatearHoras(promedio));
        lblAnualMeses.setText(String.valueOf(mesesTrabajados));
        lblAnualMejor.setText(formatearHoras(mejorMes));

        // Actualizar gráficos
        actualizarGraficoAnualBarras(horasPorMes);
        actualizarGraficoAnualLinea(horasPorMes);

        // Actualizar tabla
        actualizarTablaAnual(horasPorMes, diasPorMes);
    }

    private void actualizarGraficoAnualBarras(Map<Integer, Double> horasPorMes) {
        chartAnualBarras.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas por Mes");

        horasPorMes.forEach((mes, horas) -> {
            String nombreMes = obtenerNombreMes(mes);
            serie.getData().add(new XYChart.Data<>(nombreMes, horas));
        });

        chartAnualBarras.getData().add(serie);
    }

    private void actualizarGraficoAnualLinea(Map<Integer, Double> horasPorMes) {
        chartAnualLinea.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Evolución Anual");

        horasPorMes.forEach((mes, horas) -> {
            String nombreMes = obtenerNombreMes(mes);
            serie.getData().add(new XYChart.Data<>(nombreMes, horas));
        });

        chartAnualLinea.getData().add(serie);
    }

    private void actualizarTablaAnual(Map<Integer, Double> horasPorMes, Map<Integer, Integer> diasPorMes) {
        ObservableList<EstadisticaMesDTO> items = FXCollections.observableArrayList();

        horasPorMes.forEach((mes, horas) -> {
            if (horas > 0) { // Solo mostrar meses con datos
                int dias = diasPorMes.getOrDefault(mes, 0);
                double promedio = dias > 0 ? horas / dias : 0.0;

                EstadisticaMesDTO dto = new EstadisticaMesDTO(
                        obtenerNombreMes(mes),
                        dias,
                        horas,
                        promedio
                );
                items.add(dto);
            }
        });

        tableAnual.setItems(items);
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================
    private Map<LocalDate, List<Fichaje>> agruparPorDia(List<Fichaje> fichajes) {
        return fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));
    }

    private Map<LocalDate, Double> calcularHorasPorDia(Map<LocalDate, List<Fichaje>> fichajesPorDia) {
        Map<LocalDate, Double> resultado = new LinkedHashMap<>();

        fichajesPorDia.forEach((fecha, fichajes) -> {
            fichajes.sort(Comparator.comparing(Fichaje::getFechaHora));

            double totalDia = 0.0;
            LocalDateTime ultimaEntrada = null;

            for (Fichaje f : fichajes) {
                if (f.getTipo().name().equals("ENTRADA")) {
                    ultimaEntrada = f.getFechaHora();
                } else if (f.getTipo().name().equals("SALIDA") && ultimaEntrada != null) {
                    Duration duracion = Duration.between(ultimaEntrada, f.getFechaHora());
                    totalDia += duracion.toMinutes() / 60.0;
                    ultimaEntrada = null;
                }
            }

            if (totalDia > 0) {
                resultado.put(fecha, totalDia);
            }
        });

        return resultado;
    }

    private boolean esJornadaCompleta(List<Fichaje> fichajes) {
        if (fichajes.isEmpty()) return false;

        fichajes.sort(Comparator.comparing(Fichaje::getFechaHora));

        boolean tieneEntrada = fichajes.stream().anyMatch(f -> f.getTipo().name().equals("ENTRADA"));
        boolean tieneSalida = fichajes.stream().anyMatch(f -> f.getTipo().name().equals("SALIDA"));

        // Verificar que la última acción sea una SALIDA
        Fichaje ultimo = fichajes.get(fichajes.size() - 1);
        boolean terminaEnSalida = ultimo.getTipo().name().equals("SALIDA");

        return tieneEntrada && tieneSalida && terminaEnSalida;
    }

    private String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes - 1];
    }

    /**
     * Convierte horas decimales a formato "Xh Ym"
     * Ejemplo: 8.92 → "8h 55m"
     */
    private String formatearHoras(double horasDecimal) {
        int horas = (int) horasDecimal;
        int minutos = (int) Math.round((horasDecimal - horas) * 60);

        // Si los minutos son 60, ajustar a la siguiente hora
        if (minutos == 60) {
            horas++;
            minutos = 0;
        }

        if (minutos == 0) {
            return horas + "h";
        } else {
            return horas + "h " + minutos + "m";
        }
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

    // ============================================
    // DTOs INTERNOS
    // ============================================
    public static class EstadisticaDiaDTO {
        private String fecha;
        private String diaSemana;
        private Double horas;
        private String estado;

        public EstadisticaDiaDTO(String fecha, String diaSemana, Double horas, String estado) {
            this.fecha = fecha;
            this.diaSemana = diaSemana;
            this.horas = horas;
            this.estado = estado;
        }

        public String getFecha() { return fecha; }
        public String getDiaSemana() { return diaSemana; }
        public Double getHoras() { return horas; }
        public String getEstado() { return estado; }
    }

    public static class EstadisticaSemanaDTO {
        private Integer numeroSemana;
        private String rangoFechas;
        private Integer diasTrabajados;
        private Double totalHoras;
        private Double promedioDiario;

        public EstadisticaSemanaDTO(Integer numeroSemana, String rangoFechas, Integer diasTrabajados,
                                    Double totalHoras, Double promedioDiario) {
            this.numeroSemana = numeroSemana;
            this.rangoFechas = rangoFechas;
            this.diasTrabajados = diasTrabajados;
            this.totalHoras = totalHoras;
            this.promedioDiario = promedioDiario;
        }

        public Integer getNumeroSemana() { return numeroSemana; }
        public String getRangoFechas() { return rangoFechas; }
        public Integer getDiasTrabajados() { return diasTrabajados; }
        public Double getTotalHoras() { return totalHoras; }
        public Double getPromedioDiario() { return promedioDiario; }
    }

    public static class EstadisticaMesDTO {
        private String nombreMes;
        private Integer diasTrabajados;
        private Double totalHoras;
        private Double promedioDiario;

        public EstadisticaMesDTO(String nombreMes, Integer diasTrabajados,
                                 Double totalHoras, Double promedioDiario) {
            this.nombreMes = nombreMes;
            this.diasTrabajados = diasTrabajados;
            this.totalHoras = totalHoras;
            this.promedioDiario = promedioDiario;
        }

        public String getNombreMes() { return nombreMes; }
        public Integer getDiasTrabajados() { return diasTrabajados; }
        public Double getTotalHoras() { return totalHoras; }
        public Double getPromedioDiario() { return promedioDiario; }
    }
}