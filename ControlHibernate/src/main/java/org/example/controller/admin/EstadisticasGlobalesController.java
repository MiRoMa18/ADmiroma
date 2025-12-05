package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.EstadisticaEmpleadoDTO;
import org.example.model.Fichaje;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class EstadisticasGlobalesController {

    // === FILTROS ===
    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    // === TARJETAS DE RESUMEN ===
    @FXML private Label lblTotalHoras;
    @FXML private Label lblEmpleadosActivos;
    @FXML private Label lblPromedioEmpleado;
    @FXML private Label lblMejorEmpleado;

    // === GRÁFICO ===
    @FXML private BarChart<String, Number> chartComparativa;
    @FXML private CategoryAxis xAxisEmpleados;
    @FXML private NumberAxis yAxisHoras;

    // === TABLA ===
    @FXML private TableView<EstadisticaEmpleadoDTO> tableEstadisticas;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colNombre;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colTarjeta;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Integer> colDias;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Double> colTotalHoras;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Double> colPromedio;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, Integer> colIncompletos;
    @FXML private TableColumn<EstadisticaEmpleadoDTO, String> colEstado;

    private Trabajador adminActual;
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador admin) {
        this.adminActual = admin;

        // Configurar ComboBox de meses
        cbMes.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
        cbMes.setValue(obtenerNombreMes(LocalDate.now().getMonthValue()));

        // Configurar ComboBox de años
        ObservableList<Integer> anios = FXCollections.observableArrayList();
        int anioActual = LocalDate.now().getYear();
        for (int i = anioActual; i >= anioActual - 5; i--) {
            anios.add(i);
        }
        cbAnio.setItems(anios);
        cbAnio.setValue(anioActual);

        // Configurar tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarEstadisticas();

        System.out.println("✅ Vista 'Estadísticas Globales' cargada para ADMIN: " + admin.getNombre());
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("diasTrabajados"));
        colTotalHoras.setCellValueFactory(new PropertyValueFactory<>("totalHoras"));
        colPromedio.setCellValueFactory(new PropertyValueFactory<>("promedioDiario"));
        colIncompletos.setCellValueFactory(new PropertyValueFactory<>("fichajesIncompletos"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Formatear columna de total horas
        colTotalHoras.setCellFactory(col -> new TableCell<EstadisticaEmpleadoDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatearHoras(horas));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        });

        // Formatear columna de promedio
        colPromedio.setCellFactory(col -> new TableCell<EstadisticaEmpleadoDTO, Double>() {
            @Override
            protected void updateItem(Double promedio, boolean empty) {
                super.updateItem(promedio, empty);
                if (empty || promedio == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatearHoras(promedio));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");
                }
            }
        });

        // Formatear columna de estado con colores
        colEstado.setCellFactory(col -> new TableCell<EstadisticaEmpleadoDTO, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if (estado.contains("✅")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (estado.contains("⚠️")) {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Destacar fichajes incompletos
        colIncompletos.setCellFactory(col -> new TableCell<EstadisticaEmpleadoDTO, Integer>() {
            @Override
            protected void updateItem(Integer incompletos, boolean empty) {
                super.updateItem(incompletos, empty);
                if (empty || incompletos == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(incompletos));
                    if (incompletos > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #27ae60;");
                    }
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

        System.out.println("🔍 Cargando estadísticas globales - Mes: " + mes + ", Año: " + anio);

        LocalDate fechaInicio = LocalDate.of(anio, mes, 1);
        LocalDate fechaFin = fechaInicio.with(TemporalAdjusters.lastDayOfMonth());

        // Obtener todos los trabajadores
        List<Trabajador> trabajadores = trabajadorDAO.obtenerTodos();

        if (trabajadores.isEmpty()) {
            System.out.println("⚠️ No hay trabajadores en el sistema");
            mostrarAlerta("Sin datos", "No hay trabajadores registrados en el sistema");
            return;
        }

        // Procesar estadísticas de cada trabajador
        List<EstadisticaEmpleadoDTO> estadisticas = new ArrayList<>();
        double totalHorasGlobal = 0.0;
        int empleadosActivos = 0;

        for (Trabajador trabajador : trabajadores) {
            // Obtener fichajes del trabajador en el período
            List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                    trabajador.getId(), fechaInicio, fechaFin
            );

            if (fichajes.isEmpty()) {
                // Empleado sin fichajes en el período
                EstadisticaEmpleadoDTO dto = new EstadisticaEmpleadoDTO(
                        trabajador.getId(),
                        trabajador.getNombreCompleto(),
                        trabajador.getNumeroTarjeta(),
                        0, 0.0, 0.0, 0, "❌ Inactivo"
                );
                estadisticas.add(dto);
                continue;
            }

            // Agrupar fichajes por día
            Map<LocalDate, List<Fichaje>> fichajesPorDia = fichajes.stream()
                    .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

            // Calcular horas trabajadas y fichajes incompletos
            double totalHoras = 0.0;
            int fichajesIncompletos = 0;

            for (Map.Entry<LocalDate, List<Fichaje>> entry : fichajesPorDia.entrySet()) {
                List<Fichaje> fichajesDia = entry.getValue();
                fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

                LocalDateTime ultimaEntrada = null;
                boolean diaCompleto = false;

                for (Fichaje f : fichajesDia) {
                    if (f.getTipo().name().equals("ENTRADA")) {
                        ultimaEntrada = f.getFechaHora();
                    } else if (f.getTipo().name().equals("SALIDA") && ultimaEntrada != null) {
                        Duration duracion = Duration.between(ultimaEntrada, f.getFechaHora());
                        totalHoras += duracion.toMinutes() / 60.0;
                        ultimaEntrada = null;
                        diaCompleto = true;
                    }
                }

                // Si quedó una entrada sin salida, es incompleto
                if (ultimaEntrada != null) {
                    fichajesIncompletos++;
                }
            }

            int diasTrabajados = fichajesPorDia.size() - fichajesIncompletos;
            double promedioDiario = diasTrabajados > 0 ? totalHoras / diasTrabajados : 0.0;

            // Determinar estado (basado en promedio de 8h/día como referencia)
            String estado;
            if (promedioDiario >= 7.5) {
                estado = "✅ Normal";
            } else if (promedioDiario >= 5.0) {
                estado = "⚠️ Bajo";
            } else {
                estado = "❌ Muy Bajo";
            }

            EstadisticaEmpleadoDTO dto = new EstadisticaEmpleadoDTO(
                    trabajador.getId(),
                    trabajador.getNombreCompleto(),
                    trabajador.getNumeroTarjeta(),
                    diasTrabajados,
                    totalHoras,
                    promedioDiario,
                    fichajesIncompletos,
                    estado
            );
            estadisticas.add(dto);

            if (totalHoras > 0) {
                empleadosActivos++;
                totalHorasGlobal += totalHoras;
            }
        }

        // Actualizar tarjetas de resumen
        lblTotalHoras.setText(formatearHoras(totalHorasGlobal));
        lblEmpleadosActivos.setText(String.valueOf(empleadosActivos));

        double promedioGlobal = empleadosActivos > 0 ? totalHorasGlobal / empleadosActivos : 0.0;
        lblPromedioEmpleado.setText(formatearHoras(promedioGlobal));

        // Encontrar mejor empleado
        Optional<EstadisticaEmpleadoDTO> mejorEmpleado = estadisticas.stream()
                .filter(e -> e.getTotalHoras() != null && e.getTotalHoras() > 0)
                .max(Comparator.comparing(EstadisticaEmpleadoDTO::getTotalHoras));

        if (mejorEmpleado.isPresent()) {
            lblMejorEmpleado.setText(mejorEmpleado.get().getNombreCompleto() +
                    " (" + formatearHoras(mejorEmpleado.get().getTotalHoras()) + ")");
        } else {
            lblMejorEmpleado.setText("N/A");
        }

        // Actualizar tabla
        ObservableList<EstadisticaEmpleadoDTO> items = FXCollections.observableArrayList(estadisticas);
        tableEstadisticas.setItems(items);

        // Actualizar gráfico
        actualizarGrafico(estadisticas);
    }

    private void actualizarGrafico(List<EstadisticaEmpleadoDTO> estadisticas) {
        chartComparativa.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Horas Trabajadas");

        // Ordenar por horas descendente y tomar top 10
        estadisticas.stream()
                .filter(e -> e.getTotalHoras() != null && e.getTotalHoras() > 0)
                .sorted(Comparator.comparing(EstadisticaEmpleadoDTO::getTotalHoras).reversed())
                .limit(10)
                .forEach(e -> {
                    serie.getData().add(new XYChart.Data<>(e.getNombreCompleto(), e.getTotalHoras()));
                });

        chartComparativa.getData().add(serie);
    }

    /**
     * Convierte horas decimales a formato "Xh Ym"
     */
    private String formatearHoras(double horasDecimal) {
        int horas = (int) horasDecimal;
        int minutos = (int) Math.round((horasDecimal - horas) * 60);

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

    private String obtenerNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes - 1];
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}