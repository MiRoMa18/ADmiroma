package org.example.controller.trabajador;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.example.dao.FichajeDAO;
import org.example.model.dto.FichajeDiaDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.util.*;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MisFichajesController {

    @FXML private ComboBox<String> cbFiltro;
    @FXML private Label lblFechaInicio;
    @FXML private DatePicker dpFechaInicio;
    @FXML private Label lblFechaFin;
    @FXML private DatePicker dpFechaFin;
    @FXML private Button btnBuscar;
    @FXML private Button btnVolver;
    @FXML private Button btnExportarExcel;
    @FXML private Button btnExportarPDF;

    @FXML private Label lblTotalPeriodo;
    @FXML private Label lblPromedioDiario;
    @FXML private Label lblDiasTrabajados;

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

        configurarTabla();
        configurarFiltros();

        if (btnExportarExcel != null) {
            btnExportarExcel.setOnAction(e -> exportarExcel());
        }

        if (btnExportarPDF != null) {
            btnExportarPDF.setOnAction(e -> exportarPDF());
        }

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

        cbFiltro.setValue("HOY");

        cbFiltro.valueProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null) {
                if (nuevo.equals("PERSONALIZADO")) {
                    // Mostrar DatePickers de rango
                    mostrarDatePickers(true);
                } else {
                    // Ocultar DatePickers
                    mostrarDatePickers(false);
                    cargarFichajes();
                }
            }
        });

        if (dpFechaInicio != null) {
            dpFechaInicio.setValue(LocalDate.now());
        }
        if (dpFechaFin != null) {
            dpFechaFin.setValue(LocalDate.now());
        }

        mostrarDatePickers(false);
    }
    private void mostrarDatePickers(boolean mostrar) {
        if (lblFechaInicio != null) {
            lblFechaInicio.setVisible(mostrar);
            lblFechaInicio.setManaged(mostrar);
        }
        if (dpFechaInicio != null) {
            dpFechaInicio.setVisible(mostrar);
            dpFechaInicio.setManaged(mostrar);
        }
        if (lblFechaFin != null) {
            lblFechaFin.setVisible(mostrar);
            lblFechaFin.setManaged(mostrar);
        }
        if (dpFechaFin != null) {
            dpFechaFin.setVisible(mostrar);
            dpFechaFin.setManaged(mostrar);
        }
    }

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

        switch (filtro) {
            case "HOY":
                inicio = hoy;
                fin = hoy;
                break;

            case "ESTA SEMANA":
                inicio = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
                fin = inicio.plusDays(6);
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
                if (dpFechaInicio == null || dpFechaInicio.getValue() == null ||
                        dpFechaFin == null || dpFechaFin.getValue() == null) {
                    AlertasUtil.mostrarError("Error", "Seleccione ambas fechas");
                    return;
                }
                inicio = dpFechaInicio.getValue();
                fin = dpFechaFin.getValue();

                if (inicio.isAfter(fin)) {
                    AlertasUtil.mostrarError("Error", "La fecha de inicio debe ser anterior o igual a la fecha de fin");
                    return;
                }
                break;

            default:
                inicio = hoy;
                fin = hoy;
        }

        try {
            List<Fichaje> fichajes = fichajeDAO.buscarPorTrabajadorYRango(
                    trabajadorActual.getId(),
                    inicio,
                    fin
            );

            List<FichajeDiaDTO> fichajesPorDia = FichajesProcesador.agruparFichajesPorDia(
                    fichajes,
                    false
            );

            if (tableFichajes != null) {
                tableFichajes.setItems(FXCollections.observableArrayList(fichajesPorDia));
            }

            calcularEstadisticas(fichajesPorDia, fichajes);
        } catch (Exception e) {
            System.err.println("💥 ERROR: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "No se pudieron cargar fichajes");
        }
    }

    private void calcularEstadisticas(List<FichajeDiaDTO> fichajesPorDia, List<Fichaje> fichajesRaw) {
        int totalDias = fichajesPorDia.size();

        double totalHoras = fichajesPorDia.stream()
                .mapToDouble(f -> f.getHorasTotales() != null ? f.getHorasTotales() : 0.0)
                .sum();

        double promedioDiario = totalDias > 0 ? totalHoras / totalDias : 0.0;

        if (lblTotalPeriodo != null) {
            lblTotalPeriodo.setText(HorasFormateador.formatearHoras(totalHoras));
        }

        if (lblPromedioDiario != null) {
            lblPromedioDiario.setText(HorasFormateador.formatearHorasDecimal(promedioDiario));
        }

        if (lblDiasTrabajados != null) {
            lblDiasTrabajados.setText(totalDias + " días");
        }
    }

    @FXML
    private void handleBuscar() {
        cargarFichajes();
    }

    @FXML
    private void handleVolver() {
        NavegacionUtil.abrirDashboard(btnVolver, trabajadorActual);
    }

    private void exportarExcel() {
        if (tableFichajes == null || tableFichajes.getItems().isEmpty()) {
            AlertasUtil.mostrarError("Error", "No hay datos para exportar");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo Excel");
            fileChooser.setInitialFileName("fichajes_" + LocalDate.now() + ".xlsx");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx")
            );

            File archivo = fileChooser.showSaveDialog(btnExportarExcel.getScene().getWindow());

            if (archivo != null) {
                List<FichajeDiaDTO> fichajes = tableFichajes.getItems();
                LocalDate fechaInicio = obtenerFechaInicio();
                LocalDate fechaFin = obtenerFechaFin();

                // Exportar
                boolean exito = ExcelExportador.exportar(
                        fichajes,
                        archivo,
                        trabajadorActual.getNombreCompleto(),
                        fechaInicio,
                        fechaFin
                );

                if (exito) {
                    AlertasUtil.mostrarExito("Éxito",
                            "Excel generado correctamente en:\n" + archivo.getAbsolutePath());
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo generar el Excel");
                }
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al exportar Excel: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    private void exportarPDF() {
        if (tableFichajes == null || tableFichajes.getItems().isEmpty()) {
            AlertasUtil.mostrarError("Error", "No hay datos para exportar");
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar archivo PDF");
            fileChooser.setInitialFileName("fichajes_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf")
            );

            File archivo = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());

            if (archivo != null) {
                List<FichajeDiaDTO> fichajes = tableFichajes.getItems();

                LocalDate fechaInicio = obtenerFechaInicio();
                LocalDate fechaFin = obtenerFechaFin();

                boolean exito = PDFExportador.exportar(
                        fichajes,
                        archivo,
                        trabajadorActual.getNombreCompleto(),
                        fechaInicio,
                        fechaFin
                );

                if (exito) {
                    AlertasUtil.mostrarExito("Éxito",
                            "PDF generado correctamente en:\n" + archivo.getAbsolutePath());
                } else {
                    AlertasUtil.mostrarError("Error", "No se pudo generar el PDF");
                }
            }

        } catch (Exception e) {
            System.err.println("💥 ERROR al exportar PDF: " + e.getMessage());
            e.printStackTrace();
            AlertasUtil.mostrarError("Error", "Error al exportar: " + e.getMessage());
        }
    }

    private LocalDate obtenerFechaInicio() {
        String filtro = cbFiltro != null ? cbFiltro.getValue() : "HOY";
        LocalDate hoy = LocalDate.now();

        switch (filtro) {
            case "ESTA SEMANA":
                return hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
            case "ESTE MES":
                return hoy.withDayOfMonth(1);
            case "MES PASADO":
                return hoy.minusMonths(1).withDayOfMonth(1);
            case "PERSONALIZADO":
                return dpFechaInicio != null && dpFechaInicio.getValue() != null ? dpFechaInicio.getValue() : hoy;
            default:
                return hoy;
        }
    }
    private LocalDate obtenerFechaFin() {
        String filtro = cbFiltro != null ? cbFiltro.getValue() : "HOY";
        LocalDate hoy = LocalDate.now();

        switch (filtro) {
            case "ESTA SEMANA":
                return hoy.minusDays(hoy.getDayOfWeek().getValue() - 1).plusDays(6);
            case "ESTE MES":
                return hoy.withDayOfMonth(hoy.lengthOfMonth());
            case "MES PASADO":
                LocalDate primerDiaMesPasado = hoy.minusMonths(1).withDayOfMonth(1);
                return primerDiaMesPasado.withDayOfMonth(primerDiaMesPasado.lengthOfMonth());
            case "PERSONALIZADO":
                return dpFechaFin != null && dpFechaFin.getValue() != null ? dpFechaFin.getValue() : hoy;
            default: // "HOY"
                return hoy;
        }
    }
}