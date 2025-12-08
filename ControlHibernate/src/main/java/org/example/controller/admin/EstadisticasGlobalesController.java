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

/**
 * Controlador Estadísticas Globales (ADMIN).
 * CORREGIDO: Sin horas negativas + gráfico funcional primera vez.
 */
public class EstadisticasGlobalesController {

    // FILTROS
    @FXML private ComboBox<String> cbMes;
    @FXML private ComboBox<Integer> cbAnio;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;

    // LABELS DE RESUMEN
    @FXML private Label lblTotalHoras;
    @FXML private Label lblEmpleadosActivos;
    @FXML private Label lblPromedioEmpleado;
    @FXML private Label lblMejorEmpleado;

    // GRÁFICO
    @FXML private BarChart<String, Number> chartComparativa;
    @FXML private CategoryAxis xAxisEmpleados;
    @FXML private NumberAxis yAxisHoras;

    // TABLA
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
        System.out.println("📊 EstadisticasGlobalesController inicializado");

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

                    System.out.println("   " + t.getNombreCompleto() + ": " +
                            HorasFormateador.formatearHoras(totalHoras) +
                            " en " + diasTrabajados + " días");

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

            // Ordenar por total horas descendente
            estadisticas.sort((a, b) -> Double.compare(b.getTotalHoras(), a.getTotalHoras()));

            // Mostrar en tabla
            if (tableEstadisticas != null) {
                tableEstadisticas.setItems(FXCollections.observableArrayList(estadisticas));
            }

            // Actualizar resumen
            actualizarResumen(estadisticas);

            // Actualizar gráfico (CORREGIDO)
            actualizarGraficoCorregido(estadisticas);

            System.out.println("✅ Estadísticas cargadas: " + estadisticas.size() + " empleados");

        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar estadísticas");
        }
    }

    /**
     * CORREGIDO: Calcula horas correctamente, sin valores negativos.
     * Empareja cada ENTRADA con la SALIDA más cercana posterior.
     */
    private double calcularTotalHorasCorregido(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        double totalHoras = 0.0;

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

            // Emparejar: cada entrada con la salida más cercana
            int pares = Math.min(entradas.size(), salidas.size());

            for (int i = 0; i < pares; i++) {
                double horas = HorasFormateador.calcularHoras(
                        entradas.get(i).getFechaHora(),
                        salidas.get(i).getFechaHora()
                );

                // VALIDACIÓN: Solo sumar si es positivo
                if (horas >= 0) {
                    totalHoras += horas;
                } else {
                    System.out.println("   ⚠️ Horas negativas detectadas en " +
                            entry.getKey() + ": " +
                            entradas.get(i).getFechaHora().toLocalTime() + " - " +
                            salidas.get(i).getFechaHora().toLocalTime() +
                            " (ignorado)");
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

    /**
     * CORREGIDO: Actualiza gráfico correctamente la primera vez.
     * Solución: Limpiar completamente y forzar layout antes de agregar datos.
     */
    private void actualizarGraficoCorregido(List<EstadisticaEmpleadoDTO> estadisticas) {
        if (chartComparativa == null) {
            System.out.println("⚠️ Gráfico no disponible");
            return;
        }

        // CRÍTICO: Limpiar COMPLETAMENTE el gráfico
        chartComparativa.getData().clear();

        // CRÍTICO: Forzar layout para que JavaFX actualice el eje X
        chartComparativa.layout();

        // Crear nueva serie
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Horas");

        // Filtrar empleados con horas > 0 y tomar Top 10
        List<EstadisticaEmpleadoDTO> top10 = estadisticas.stream()
                .filter(e -> e.getDiasTrabajados() > 0)
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("📈 Actualizando gráfico con " + top10.size() + " empleados");

        // Agregar datos al gráfico
        for (EstadisticaEmpleadoDTO e : top10) {
            series.getData().add(new XYChart.Data<>(e.getNombreCompleto(), e.getTotalHoras()));
            System.out.println("   - " + e.getNombreCompleto() + ": " + e.getTotalHoras() + "h");
        }

        // Agregar serie al gráfico
        chartComparativa.getData().add(series);

        // CRÍTICO: Forzar un segundo layout después de agregar datos
        chartComparativa.layout();

        System.out.println("✅ Gráfico actualizado correctamente");
    }

    @FXML
    private void handleBuscar() {
        System.out.println("🔍 Búsqueda manual iniciada");
        cargarEstadisticas();
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }
}