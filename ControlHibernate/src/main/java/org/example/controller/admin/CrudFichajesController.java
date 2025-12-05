package org.example.controller.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.dao.FichajeDAO;
import org.example.dao.TrabajadorDAO;
import org.example.model.Fichaje;
import org.example.model.FichajeDiaDTO;
import org.example.model.Trabajador;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CrudFichajesController {

    // === FILTROS ===
    @FXML private ComboBox<Trabajador> cbEmpleado;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private ComboBox<String> cbTipo;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiar;

    // === BOTONES DE ACCIÓN ===
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;
    @FXML private Button btnRefrescar;
    @FXML private Button btnVolver;

    // === TABLA ===
    @FXML private TableView<FichajeDiaDTO> tableFichajes;
    @FXML private TableColumn<FichajeDiaDTO, String> colEmpleado;
    @FXML private TableColumn<FichajeDiaDTO, String> colTarjeta;
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

    @FXML private Label lblContador;

    private Trabajador adminActual;
    private FichajeDAO fichajeDAO = new FichajeDAO();
    private TrabajadorDAO trabajadorDAO = new TrabajadorDAO();
    private List<Trabajador> todosTrabajadores;

    // Mapa: clave = "trabajadorId_fecha" -> lista de IDs de fichajes de ese día
    private Map<String, List<Integer>> fichajesPorDiaMap = new HashMap<>();

    public void inicializar(Trabajador admin) {
        this.adminActual = admin;

        todosTrabajadores = trabajadorDAO.obtenerTodos();

        // Configurar ComboBox empleados
        ObservableList<Trabajador> trabajadores = FXCollections.observableArrayList(todosTrabajadores);
        cbEmpleado.setItems(trabajadores);
        cbEmpleado.setPromptText("Todos los empleados");

        cbEmpleado.setConverter(new javafx.util.StringConverter<Trabajador>() {
            @Override
            public String toString(Trabajador trabajador) {
                return trabajador == null ? "" : trabajador.getNombreCompleto();
            }

            @Override
            public Trabajador fromString(String string) {
                return null;
            }
        });

        // Configurar ComboBox tipo
        cbTipo.setItems(FXCollections.observableArrayList("TODOS", "ENTRADA", "SALIDA"));
        cbTipo.setValue("TODOS");

        // ESTABLECER FECHA ACTUAL POR DEFECTO
        LocalDate hoy = LocalDate.now();
        dpFechaInicio.setValue(hoy);
        dpFechaFin.setValue(hoy);

        // Configurar tabla
        configurarTabla();

        // Deshabilitar botones editar/eliminar hasta seleccionar
        btnEditar.setDisable(true);
        btnEliminar.setDisable(true);

        // Listener para selección
        tableFichajes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean haySeleccion = newSel != null;
            btnEditar.setDisable(!haySeleccion);
            btnEliminar.setDisable(!haySeleccion);
        });

        // Cargar fichajes
        cargarFichajes();

        System.out.println("✅ Vista 'CRUD Fichajes' cargada para ADMIN: " + admin.getNombre());
    }

    private void configurarTabla() {
        colEmpleado.setCellValueFactory(new PropertyValueFactory<>("nombreEmpleado"));
        colTarjeta.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
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

        // Formatear columnas de hora
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

        // Formatear horas totales
        colHoras.setCellFactory(col -> new TableCell<FichajeDiaDTO, Double>() {
            @Override
            protected void updateItem(Double horas, boolean empty) {
                super.updateItem(horas, empty);
                if (empty || horas == null || horas == 0) {
                    setText(null);
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
    }

    private void formatearColumnaHora(TableColumn<FichajeDiaDTO, LocalTime> columna) {
        columna.setCellFactory(col -> new TableCell<FichajeDiaDTO, LocalTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            @Override
            protected void updateItem(LocalTime hora, boolean empty) {
                super.updateItem(hora, empty);
                if (empty || hora == null) {
                    setText(null);
                } else {
                    setText(hora.format(formatter));
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
        cargarFichajes();
    }

    @FXML
    private void handleLimpiar() {
        cbEmpleado.setValue(null);
        dpFechaInicio.setValue(null);
        dpFechaFin.setValue(null);
        cbTipo.setValue("TODOS");
        cargarFichajes();
    }

    private void cargarFichajes() {
        Trabajador trabajadorSeleccionado = cbEmpleado.getValue();
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();
        String tipo = cbTipo.getValue();

        Integer trabajadorId = trabajadorSeleccionado != null ? trabajadorSeleccionado.getId() : null;

        // Buscar fichajes
        List<Fichaje> fichajes;
        if (trabajadorId == null && fechaInicio == null && fechaFin == null && tipo.equals("TODOS")) {
            fichajes = fichajeDAO.obtenerTodos();
        } else {
            fichajes = fichajeDAO.buscarConFiltros(trabajadorId, fechaInicio, fechaFin, tipo);
        }

        // Agrupar por empleado y día
        Map<String, List<Fichaje>> fichajesPorEmpleadoYDia = new HashMap<>();
        fichajesPorDiaMap.clear(); // Limpiar mapa de IDs

        for (Fichaje f : fichajes) {
            String clave = f.getTrabajador().getId() + "_" + f.getFechaHora().toLocalDate().toString();
            fichajesPorEmpleadoYDia.computeIfAbsent(clave, k -> new ArrayList<>()).add(f);
        }

        // Crear filas
        ObservableList<FichajeDiaDTO> items = FXCollections.observableArrayList();

        for (Map.Entry<String, List<Fichaje>> entry : fichajesPorEmpleadoYDia.entrySet()) {
            List<Fichaje> fichajesDia = entry.getValue();
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            Fichaje primero = fichajesDia.get(0);
            FichajeDiaDTO dto = new FichajeDiaDTO();
            dto.setFecha(primero.getFechaHora().toLocalDate());
            dto.setNombreEmpleado(primero.getTrabajador().getNombreCompleto());
            dto.setNumeroTarjeta(primero.getTrabajador().getNumeroTarjeta());

            // Guardar IDs de fichajes para poder editar/eliminar
            String clave = primero.getTrabajador().getId() + "_" + primero.getFechaHora().toLocalDate().toString();
            List<Integer> ids = new ArrayList<>();
            for (Fichaje f : fichajesDia) {
                ids.add(f.getId());
            }
            fichajesPorDiaMap.put(clave, ids); // Guardar en el mapa

            // Procesar fichajes
            double totalDia = 0.0;
            LocalDateTime ultimaEntrada = null;
            int parIndex = 1;
            List<String> notasList = new ArrayList<>();
            Set<String> climaSet = new HashSet<>();

            for (Fichaje f : fichajesDia) {
                LocalTime hora = f.getFechaHora().toLocalTime();

                if (f.getTipo().name().equals("ENTRADA")) {
                    ultimaEntrada = f.getFechaHora();
                    switch (parIndex) {
                        case 1: dto.setEntrada1(hora); break;
                        case 2: dto.setEntrada2(hora); break;
                        case 3: dto.setEntrada3(hora); break;
                        case 4: dto.setEntrada4(hora); break;
                        case 5: dto.setEntrada5(hora); break;
                    }
                } else if (f.getTipo().name().equals("SALIDA") && ultimaEntrada != null) {
                    Duration duracion = Duration.between(ultimaEntrada, f.getFechaHora());
                    double horas = duracion.toMinutes() / 60.0;
                    totalDia += horas;

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

                if (f.getNotas() != null && !f.getNotas().isEmpty()) {
                    notasList.add(f.getNotas());
                }
                if (f.getClima() != null && !f.getClima().isEmpty()) {
                    climaSet.add(f.getClima());
                }
            }

            dto.setNotas(String.join("; ", notasList));
            dto.setClima(String.join(", ", climaSet));
            dto.setHorasTotales(totalDia);

            items.add(dto);
        }

        // Ordenar por fecha descendente
        items.sort((a, b) -> b.getFecha().compareTo(a.getFecha()));

        tableFichajes.setItems(items);
        lblContador.setText("Mostrando " + items.size() + " registro(s) agrupados por día");
    }

    private String formatearHoras(double horasDecimal) {
        int horas = (int) horasDecimal;
        int minutos = (int) Math.round((horasDecimal - horas) * 60);

        if (minutos == 60) {
            horas++;
            minutos = 0;
        }

        return minutos == 0 ? horas + "h" : horas + "h " + minutos + "m";
    }

    @FXML
    private void handleNuevo() {
        abrirDialogoFichaje(null);
    }

    @FXML
    private void handleEditar() {
        FichajeDiaDTO seleccionado = tableFichajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        // Obtener los IDs de fichajes de ese día
        String clave = obtenerClave(seleccionado);
        List<Integer> ids = fichajesPorDiaMap.get(clave);

        if (ids == null || ids.isEmpty()) {
            mostrarAlerta("Error", "No se encontraron fichajes para este día");
            return;
        }

        // Si solo hay un fichaje, editar directamente
        if (ids.size() == 1) {
            abrirDialogoFichaje(ids.get(0));
            return;
        }

        // Si hay múltiples fichajes, mostrar diálogo de selección
        Integer fichajeId = mostrarDialogoSeleccionFichaje(ids, "editar");
        if (fichajeId != null) {
            abrirDialogoFichaje(fichajeId);
        }
    }

    @FXML
    private void handleEliminar() {
        FichajeDiaDTO seleccionado = tableFichajes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) return;

        // Obtener los IDs de fichajes de ese día
        String clave = obtenerClave(seleccionado);
        List<Integer> ids = fichajesPorDiaMap.get(clave);

        if (ids == null || ids.isEmpty()) {
            mostrarAlerta("Error", "No se encontraron fichajes para este día");
            return;
        }

        // Si solo hay un fichaje, eliminar directamente con confirmación
        if (ids.size() == 1) {
            confirmarYEliminarFichaje(ids.get(0), seleccionado);
            return;
        }

        // Si hay múltiples fichajes, mostrar diálogo de selección
        Integer fichajeId = mostrarDialogoSeleccionFichaje(ids, "eliminar");
        if (fichajeId != null) {
            confirmarYEliminarFichaje(fichajeId, seleccionado);
        }
    }

    @FXML
    private void handleRefrescar() {
        cargarFichajes();
    }

    /**
     * Obtiene la clave del mapa a partir del DTO
     */
    private String obtenerClave(FichajeDiaDTO dto) {
        // Buscar el trabajador por número de tarjeta
        for (Trabajador t : todosTrabajadores) {
            if (t.getNumeroTarjeta().equals(dto.getNumeroTarjeta())) {
                return t.getId() + "_" + dto.getFecha().toString();
            }
        }
        return null;
    }

    /**
     * Muestra un diálogo para seleccionar cuál fichaje específico editar/eliminar
     */
    private Integer mostrarDialogoSeleccionFichaje(List<Integer> fichajeIds, String accion) {
        // Cargar información de cada fichaje
        List<FichajeOpcion> opciones = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Integer id : fichajeIds) {
            Optional<Fichaje> fichajeOpt = fichajeDAO.buscarPorId(id);
            if (fichajeOpt.isPresent()) {
                Fichaje f = fichajeOpt.get();
                String descripcion = f.getFechaHora().toLocalTime().format(formatter) +
                        " - " + f.getTipo().name();
                if (f.getNotas() != null && !f.getNotas().isEmpty()) {
                    descripcion += " (" + f.getNotas() + ")";
                }
                opciones.add(new FichajeOpcion(id, descripcion));
            }
        }

        if (opciones.isEmpty()) {
            mostrarAlerta("Error", "No se pudieron cargar los fichajes");
            return null;
        }

        // Mostrar ChoiceDialog
        ChoiceDialog<FichajeOpcion> dialog = new ChoiceDialog<>(opciones.get(0), opciones);
        dialog.setTitle("Seleccionar Fichaje");
        dialog.setHeaderText("Hay múltiples fichajes en este día");
        dialog.setContentText("Selecciona cuál deseas " + accion + ":");

        Optional<FichajeOpcion> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get().id;
        }

        return null;
    }

    /**
     * Confirma y elimina un fichaje específico
     */
    private void confirmarYEliminarFichaje(Integer fichajeId, FichajeDiaDTO diaDTO) {
        // Buscar fichaje para mostrar info
        Optional<Fichaje> fichajeOpt = fichajeDAO.buscarPorId(fichajeId);
        if (fichajeOpt.isEmpty()) {
            mostrarAlerta("Error", "No se encontró el fichaje");
            return;
        }

        Fichaje fichaje = fichajeOpt.get();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar este fichaje?");
        confirmacion.setContentText(
                "Empleado: " + diaDTO.getNombreEmpleado() + "\n" +
                        "Fecha/Hora: " + fichaje.getFechaHora().format(formatter) + "\n" +
                        "Tipo: " + fichaje.getTipo().name()
        );

        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean exito = fichajeDAO.eliminar(fichajeId);

            if (exito) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Eliminado");
                info.setHeaderText(null);
                info.setContentText("✅ Fichaje eliminado correctamente");
                info.showAndWait();

                cargarFichajes();
            } else {
                mostrarAlerta("Error", "❌ Error al eliminar el fichaje");
            }
        }
    }

    /**
     * Clase interna para las opciones del ChoiceDialog
     */
    private static class FichajeOpcion {
        private Integer id;
        private String descripcion;

        public FichajeOpcion(Integer id, String descripcion) {
            this.id = id;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return descripcion;
        }
    }

    private void abrirDialogoFichaje(Integer fichajeId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/dialogo_fichaje.fxml"));
            Parent root = loader.load();

            DialogoFichajeController controller = loader.getController();
            controller.inicializar(fichajeId, todosTrabajadores);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(fichajeId == null ? "Nuevo Fichaje" : "Editar Fichaje");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnNuevo.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            dialogStage.setOnHidden(e -> cargarFichajes());
            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("❌ Error al abrir diálogo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Parent root = loader.load();

            org.example.controller.DashboardController controller = loader.getController();
            controller.inicializar(adminActual);

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Control Horario - Dashboard");

        } catch (IOException e) {
            System.err.println("❌ Error al volver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}