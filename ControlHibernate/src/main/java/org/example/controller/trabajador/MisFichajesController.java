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
import org.example.model.FichajeDiaDTO;
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

    @FXML private ComboBox<String> cbFiltro;
    @FXML private DatePicker dpFecha;
    @FXML private Button btnBuscar;
    @FXML private TableView<FichajeDiaDTO> tableFichajes;

    // Columnas
    @FXML private TableColumn<FichajeDiaDTO, LocalDate> colFecha;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada1;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida1;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada2;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida2;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada3;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida3;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada4;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida4;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colEntrada5;
    @FXML private TableColumn<FichajeDiaDTO, LocalTime> colSalida5;
    @FXML private TableColumn<FichajeDiaDTO, String> colNotas;
    @FXML private TableColumn<FichajeDiaDTO, String> colClima;
    @FXML private TableColumn<FichajeDiaDTO, Double> colHoras;

    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblPromedioDiario;
    @FXML private Label lblDiasTrabajados;
    @FXML private Button btnVolver;

    private Trabajador trabajadorActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();

    public void inicializar(Trabajador trabajador) {
        this.trabajadorActual = trabajador;

        cbFiltro.setItems(FXCollections.observableArrayList("Día", "Semana", "Mes"));
        cbFiltro.setValue("Mes");
        dpFecha.setValue(LocalDate.now());

        configurarTabla();
        cargarDatos();

        System.out.println("✅ Vista 'Mis Fichajes' cargada para: " + trabajador.getNombre());
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colEntrada1.setCellValueFactory(new PropertyValueFactory<>("entrada1"));
        colSalida1.setCellValueFactory(new PropertyValueFactory<>("salida1"));
        colEntrada2.setCellValueFactory(new PropertyValueFactory<>("entrada2"));
        colSalida2.setCellValueFactory(new PropertyValueFactory<>("salida2"));
        colEntrada3.setCellValueFactory(new PropertyValueFactory<>("entrada3"));
        colSalida3.setCellValueFactory(new PropertyValueFactory<>("salida3"));
        colEntrada4.setCellValueFactory(new PropertyValueFactory<>("entrada4"));
        colSalida4.setCellValueFactory(new PropertyValueFactory<>("salida4"));
        colEntrada5.setCellValueFactory(new PropertyValueFactory<>("entrada5"));
        colSalida5.setCellValueFactory(new PropertyValueFactory<>("salida5"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));
        colClima.setCellValueFactory(new PropertyValueFactory<>("clima"));
        colHoras.setCellValueFactory(new PropertyValueFactory<>("horasTotales"));

        // Formatear fecha
        colFecha.setCellFactory(col -> new TableCell<FichajeDiaDTO, LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            @Override
            protected void updateItem(LocalDate fecha, boolean empty) {
                super.updateItem(fecha, empty);
                setText(empty || fecha == null ? null : fecha.format(formatter));
            }
        });

        // Formatear horas (entrada/salida)
        formatearColumnaHora(colEntrada1);
        formatearColumnaHora(colSalida1);
        formatearColumnaHora(colEntrada2);
        formatearColumnaHora(colSalida2);
        formatearColumnaHora(colEntrada3);
        formatearColumnaHora(colSalida3);
        formatearColumnaHora(colEntrada4);
        formatearColumnaHora(colSalida4);
        formatearColumnaHora(colEntrada5);
        formatearColumnaHora(colSalida5);

        // Formatear total horas
        colHoras.setCellFactory(col -> new TableCell<FichajeDiaDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null || horas == 0) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(formatearHoras(horas));
                    setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        // Formatear notas
        colNotas.setCellFactory(col -> new TableCell<FichajeDiaDTO, String>() {
            @Override
            protected void updateItem(String notas, boolean empty) {
                super.updateItem(notas, empty);
                if (empty || notas == null || notas.isEmpty()) {
                    setText(null);
                } else {
                    String texto = notas.length() > 30 ? notas.substring(0, 27) + "..." : notas;
                    setText(texto);
                    setStyle("-fx-font-style: italic; -fx-text-fill: #7f8c8d;");
                }
            }
        });

        // Formatear clima
        colClima.setCellFactory(col -> new TableCell<FichajeDiaDTO, String>() {
            @Override
            protected void updateItem(String clima, boolean empty) {
                super.updateItem(clima, empty);
                setText(empty || clima == null ? "" : clima);
                setStyle("-fx-font-style: italic; -fx-text-fill: #3498db;");
            }
        });
    }

    private void formatearColumnaHora(TableColumn<FichajeDiaDTO, LocalTime> columna) {
        columna.setCellFactory(col -> new TableCell<FichajeDiaDTO, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            @Override
            protected void updateItem(LocalTime hora, boolean empty) {
                super.updateItem(hora, empty);
                if (empty || hora == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(hora.format(formatter));
                    // Verde para entradas, rojo para salidas
                    if (getTableColumn().getText().contains("Entrada")) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
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

        // Calcular rango
        LocalDate fechaInicio, fechaFin;
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

        List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                trabajadorActual.getId(), fechaInicio, fechaFin
        );

        // Agrupar por día
        Map<LocalDate, List<Fichaje>> fichajesPorDia = new HashMap<>();
        for (Fichaje f : fichajes) {
            LocalDate fecha = f.getFechaHora().toLocalDate();
            fichajesPorDia.computeIfAbsent(fecha, k -> new ArrayList<>()).add(f);
        }

        // Crear filas agrupadas por día
        ObservableList<FichajeDiaDTO> filas = FXCollections.observableArrayList();
        double totalGeneralHoras = 0.0;
        int diasTrabajados = 0;

        List<LocalDate> fechasOrdenadas = new ArrayList<>(fichajesPorDia.keySet());
        fechasOrdenadas.sort(Comparator.reverseOrder());

        for (LocalDate fecha : fechasOrdenadas) {
            List<Fichaje> fichajesDia = fichajesPorDia.get(fecha);
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            FichajeDiaDTO dto = new FichajeDiaDTO();
            dto.setFecha(fecha);

            // Procesar fichajes del día
            double totalDia = 0.0;
            LocalDateTime ultimaEntrada = null;
            int parIndex = 1;
            List<String> notasList = new ArrayList<>();
            Set<String> climaSet = new HashSet<>();

            for (Fichaje f : fichajesDia) {
                LocalTime hora = f.getFechaHora().toLocalTime();

                if (f.getTipo().name().equals("ENTRADA")) {
                    ultimaEntrada = f.getFechaHora();
                    // Asignar a entrada1, entrada2, etc
                    switch (parIndex) {
                        case 1: dto.setEntrada1(hora); break;
                        case 2: dto.setEntrada2(hora); break;
                        case 3: dto.setEntrada3(hora); break;
                        case 4: dto.setEntrada4(hora); break;
                        case 5: dto.setEntrada5(hora); break;
                    }
                } else if (f.getTipo().name().equals("SALIDA") && ultimaEntrada != null) {
                    // Calcular horas del segmento
                    Duration duracion = Duration.between(ultimaEntrada, f.getFechaHora());
                    double horas = duracion.toMinutes() / 60.0;
                    totalDia += horas;

                    // Asignar a salida1, salida2, etc
                    switch (parIndex) {
                        case 1: dto.setSalida1(hora); break;
                        case 2: dto.setSalida2(hora); break;
                        case 3: dto.setSalida3(hora); break;
                        case 4: dto.setSalida4(hora); break;
                        case 5: dto.setSalida5(hora); break;
                    }

                    parIndex++;
                    ultimaEntrada = null;
                }

                // Recopilar notas y clima
                if (f.getNotas() != null && !f.getNotas().isEmpty()) {
                    notasList.add(f.getNotas());
                }
                if (f.getClima() != null && !f.getClima().isEmpty()) {
                    climaSet.add(f.getClima());
                }
            }

            // Consolidar notas y clima
            dto.setNotas(String.join("; ", notasList));
            dto.setClima(String.join(", ", climaSet));
            dto.setHorasTotales(totalDia);

            filas.add(dto);

            if (totalDia > 0) {
                totalGeneralHoras += totalDia;
                diasTrabajados++;
            }
        }

        tableFichajes.setItems(filas);

        // Actualizar estadísticas
        lblTotalPeriodo.setText(formatearHoras(totalGeneralHoras));
        lblDiasTrabajados.setText(diasTrabajados + " días");

        if (diasTrabajados > 0) {
            double promedio = totalGeneralHoras / diasTrabajados;
            lblPromedioDiario.setText(formatearHoras(promedio));
        } else {
            lblPromedioDiario.setText("0h");
        }
    }

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

    @FXML
    private void handleVolver() {
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