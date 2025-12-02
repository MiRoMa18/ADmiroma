package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.model.Fichaje;
import org.example.model.FichajeDetalleDTO;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class MisFichajesController {

    @FXML
    private ComboBox<String> cbFiltro;

    @FXML
    private DatePicker dpFecha;

    @FXML
    private Button btnBuscar;

    @FXML
    private TableView<FichajeDetalleDTO> tableFichajes;

    @FXML
    private TableColumn<FichajeDetalleDTO, LocalDate> colFecha;

    @FXML
    private TableColumn<FichajeDetalleDTO, LocalTime> colHora;

    @FXML
    private TableColumn<FichajeDetalleDTO, String> colTipo;

    @FXML
    private TableColumn<FichajeDetalleDTO, String> colNotas;

    @FXML
    private TableColumn<FichajeDetalleDTO, String> colClima;

    @FXML
    private TableColumn<FichajeDetalleDTO, Double> colHorasSegmento;

    @FXML
    private Label lblTotalPeriodo;

    @FXML
    private Label lblPromedioDiario;

    @FXML
    private Label lblDiasTrabajados;

    @FXML
    private Button btnVolver;

    private Trabajador trabajadorActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        // Configurar ComboBox de filtros
        cbFiltro.setItems(FXCollections.observableArrayList("Día", "Semana", "Mes"));
        cbFiltro.setValue("Mes");

        // Configurar DatePicker con fecha actual
        dpFecha.setValue(LocalDate.now());

        // Configurar columnas de la tabla
        configurarTabla();

        // Cargar datos iniciales
        cargarDatos();

        System.out.println("✅ Vista 'Mis Fichajes' cargada para: " + trabajador.getNombre());
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colClima.setCellValueFactory(new PropertyValueFactory<>("clima"));
        colHorasSegmento.setCellValueFactory(new PropertyValueFactory<>("horasSegmento"));

        // Formatear fecha (solo mostrar en primera fila del día)
        colFecha.setCellFactory(col -> new TableCell<FichajeDetalleDTO, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            private LocalDate ultimaFecha = null;

            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                if (empty || fecha == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());

                    if (fila.isEsResumenDia()) {
                        setText("");
                        setStyle("-fx-background-color: #ecf0f1; -fx-font-weight: bold;");
                    } else if (!fecha.equals(ultimaFecha)) {
                        setText(fecha.format(formatter));
                        ultimaFecha = fecha;
                        setStyle("");
                    } else {
                        setText("");
                        setStyle("");
                    }
                }
            }
        });

        // Formatear hora
        colHora.setCellFactory(col -> new TableCell<FichajeDetalleDTO, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            protected void updateItem(LocalTime hora, boolean empty) {
                super.updateItem(hora, empty);
                if (empty || hora == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());
                    if (fila.isEsResumenDia()) {
                        setText("");
                        setStyle("-fx-background-color: #ecf0f1;");
                    } else {
                        setText(hora.format(formatter));
                        setStyle("");
                    }
                }
            }
        });

        // Formatear tipo (con emojis)
        colTipo.setCellFactory(col -> new TableCell<FichajeDetalleDTO, String>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());
                    if (fila.isEsResumenDia()) {
                        setText("");
                        setStyle("-fx-background-color: #ecf0f1;");
                    } else if (tipo.equals("ENTRADA")) {
                        setText("⬇️ Entrada");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText("⬆️ Salida");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Formatear notas
        colNotas.setCellFactory(col -> new TableCell<FichajeDetalleDTO, String>() {
            @Override
            protected void updateItem(String notas, boolean empty) {
                super.updateItem(notas, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());
                    if (fila.isEsResumenDia()) {
                        setText("");
                        setStyle("-fx-background-color: #ecf0f1;");
                    } else {
                        setText(notas != null ? notas : "");
                        setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
                    }
                }
            }
        });

        // Formatear clima
        colClima.setCellFactory(col -> new TableCell<FichajeDetalleDTO, String>() {
            @Override
            protected void updateItem(String clima, boolean empty) {
                super.updateItem(clima, empty);
                if (empty || clima == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());
                    if (fila.isEsResumenDia()) {
                        setText("TOTAL DÍA");
                        setStyle("-fx-background-color: #ecf0f1; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    } else {
                        setText(clima);
                        setStyle("");
                    }
                }
            }
        });

        // Formatear horas con formato "Xh Ym"
        colHorasSegmento.setCellFactory(col -> new TableCell<FichajeDetalleDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null) {
                    setText(null);
                    setStyle("");
                } else {
                    FichajeDetalleDTO fila = getTableView().getItems().get(getIndex());

                    if (fila.isEsResumenDia()) {
                        if (horas == -1.0) {
                            setText("INCOMPLETO");
                            setStyle("-fx-background-color: #ffcccc; -fx-text-fill: #c0392b; -fx-font-weight: bold;");
                        } else {
                            setText(formatearHoras(horas));
                            setStyle("-fx-background-color: #d5f4e6; -fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 14px;");
                        }
                    } else if (fila.getTipo().equals("SALIDA")) {
                        setText(formatearHoras(horas));
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    } else {
                        setText("");
                        setStyle("");
                    }
                }
            }
        });
    }

    @FXML
    private void handleBuscar() {
        cargarDatos();
    }

    private void cargarDatos() {
        String filtro = cbFiltro.getValue();
        LocalDate fechaSeleccionada = dpFecha.getValue();

        if (fechaSeleccionada == null) {
            mostrarAlerta("Error", "Por favor selecciona una fecha");
            return;
        }

        System.out.println("🔍 Cargando fichajes - Filtro: " + filtro + ", Fecha: " + fechaSeleccionada);

        // Calcular rango de fechas según filtro
        LocalDate fechaInicio;
        LocalDate fechaFin;

        switch (filtro) {
            case "Día":
                fechaInicio = fechaSeleccionada;
                fechaFin = fechaSeleccionada;
                break;

            case "Semana":
                fechaInicio = fechaSeleccionada.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                break;

            case "Mes":
                fechaInicio = fechaSeleccionada.withDayOfMonth(1);
                fechaFin = fechaSeleccionada.with(TemporalAdjusters.lastDayOfMonth());
                break;

            default:
                fechaInicio = fechaSeleccionada;
                fechaFin = fechaSeleccionada;
        }

        System.out.println("   Rango calculado: " + fechaInicio + " a " + fechaFin);

        // Obtener fichajes de la BD
        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(),
                fechaInicio,
                fechaFin
        );

        // Agrupar por día
        Map<LocalDate, List<Fichaje>> fichajesPorDia = new HashMap<>();
        for (Fichaje f : fichajes) {
            LocalDate fecha = f.getFechaHora().toLocalDate();
            fichajesPorDia.computeIfAbsent(fecha, k -> new ArrayList<>()).add(f);
        }

        // Crear filas detalladas
        ObservableList<FichajeDetalleDTO> filas = FXCollections.observableArrayList();
        double totalGeneralHoras = 0.0;
        int diasTrabajados = 0;

        // Ordenar fechas descendentes
        List<LocalDate> fechasOrdenadas = new ArrayList<>(fichajesPorDia.keySet());
        fechasOrdenadas.sort(Comparator.reverseOrder());

        for (LocalDate fecha : fechasOrdenadas) {
            List<Fichaje> fichajesDia = fichajesPorDia.get(fecha);
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            double totalDia = 0.0;
            LocalDateTime ultimaEntrada = null;

            // Crear fila por cada fichaje
            for (Fichaje f : fichajesDia) {
                FichajeDetalleDTO fila = new FichajeDetalleDTO();
                fila.setFecha(fecha);
                fila.setHora(f.getFechaHora().toLocalTime());
                fila.setTipo(f.getTipo().name());
                fila.setNotas(f.getNotas());
                fila.setClima(f.getClima());
                fila.setEsResumenDia(false);

                // Si es SALIDA, calcular horas desde última ENTRADA
                if (f.getTipo().name().equals("SALIDA") && ultimaEntrada != null) {
                    Duration duracion = Duration.between(ultimaEntrada, f.getFechaHora());
                    double horas = duracion.toMinutes() / 60.0;
                    fila.setHorasSegmento(horas);
                    totalDia += horas;
                    ultimaEntrada = null;
                } else if (f.getTipo().name().equals("ENTRADA")) {
                    ultimaEntrada = f.getFechaHora();
                }

                filas.add(fila);
            }

            // Fila de TOTAL DÍA
            FichajeDetalleDTO resumenDia = new FichajeDetalleDTO();
            resumenDia.setFecha(fecha);
            resumenDia.setEsResumenDia(true);
            resumenDia.setClima("TOTAL DÍA");

            if (ultimaEntrada != null) {
                // Fichaje incompleto
                resumenDia.setHorasSegmento(-1.0);
            } else {
                resumenDia.setHorasSegmento(totalDia);
                totalGeneralHoras += totalDia;
                diasTrabajados++;
            }

            filas.add(resumenDia);
        }

        // Actualizar tabla
        tableFichajes.setItems(filas);

        // Actualizar estadísticas con nuevo formato
        lblTotalPeriodo.setText(formatearHoras(totalGeneralHoras));
        lblDiasTrabajados.setText(diasTrabajados + " días");

        if (diasTrabajados > 0) {
            double promedio = totalGeneralHoras / diasTrabajados;
            lblPromedioDiario.setText(formatearHoras(promedio));
        } else {
            lblPromedioDiario.setText("0h");
        }
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

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}