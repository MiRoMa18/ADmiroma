package org.example.util;

import org.example.model.dto.FichajeDiaDTO;
import org.example.model.entity.Fichaje;
import org.example.model.entity.Trabajador;
import org.example.model.enums.TipoFichaje;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Procesador de fichajes para agrupar y calcular estadísticas.
 * Centraliza la lógica de agrupación por día que antes estaba duplicada
 * en MisFichajesController y CrudFichajesController.
 */
public class FichajesProcesador {

    private FichajesProcesador() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Agrupa fichajes por día y crea DTOs con todas las entradas/salidas.
     *
     * @param fichajes Lista de fichajes a procesar
     * @param incluirDatosEmpleado Si debe incluir nombre y tarjeta del empleado
     * @return Lista de DTOs, uno por día
     */
    public static List<FichajeDiaDTO> agruparFichajesPorDia(
            List<Fichaje> fichajes,
            boolean incluirDatosEmpleado) {

        if (fichajes == null || fichajes.isEmpty()) {
            return new ArrayList<>();
        }

        // Agrupar fichajes por fecha
        Map<LocalDate, List<Fichaje>> fichajesPorDia = fichajes.stream()
                .collect(Collectors.groupingBy(
                        f -> f.getFechaHora().toLocalDate(),
                        TreeMap::new,  // Ordenado por fecha
                        Collectors.toList()
                ));

        // Convertir cada grupo a DTO
        List<FichajeDiaDTO> resultado = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Fichaje>> entry : fichajesPorDia.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<Fichaje> fichajesDia = entry.getValue();

            // Ordenar fichajes del día por hora
            fichajesDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            FichajeDiaDTO dto = crearDTODia(fecha, fichajesDia, incluirDatosEmpleado);
            resultado.add(dto);
        }

        return resultado;
    }

    /**
     * Crea un DTO para un día específico procesando todos sus fichajes.
     */
    private static FichajeDiaDTO crearDTODia(
            LocalDate fecha,
            List<Fichaje> fichajes,
            boolean incluirDatosEmpleado) {

        FichajeDiaDTO dto = new FichajeDiaDTO();
        dto.setFecha(fecha);

        // Separar entradas y salidas
        List<LocalTime> entradas = new ArrayList<>();
        List<LocalTime> salidas = new ArrayList<>();
        List<String> notas = new ArrayList<>();
        List<String> climas = new ArrayList<>();

        for (Fichaje f : fichajes) {
            LocalTime hora = f.getFechaHora().toLocalTime();

            if (f.getTipo() == TipoFichaje.ENTRADA) {
                entradas.add(hora);
            } else {
                salidas.add(hora);
            }

            if (f.getNotas() != null && !f.getNotas().trim().isEmpty()) {
                notas.add(f.getNotas());
            }

            if (f.getClima() != null && !f.getClima().trim().isEmpty()) {
                climas.add(f.getClima());
            }
        }

        // Asignar entradas/salidas (hasta 5 pares)
        asignarHorarios(dto, entradas, salidas);

        // Calcular horas totales
        double horasTotales = calcularHorasTotales(entradas, salidas);
        dto.setHorasTotales(horasTotales);

        // Determinar estado
        String estado = entradas.size() == salidas.size() ? "✅ Completo" : "⚠️ Incompleto";
        dto.setEstado(estado);

        // Notas y clima
        dto.setNotas(String.join("; ", notas));
        dto.setClima(obtenerClimaMasComun(climas));

        // Datos del empleado (si se requiere)
        if (incluirDatosEmpleado && !fichajes.isEmpty()) {
            Trabajador t = fichajes.get(0).getTrabajador();
            dto.setNombreEmpleado(t.getNombreCompleto());
            dto.setNumeroTarjeta(t.getNumeroTarjeta());
        }

        return dto;
    }

    /**
     * Asigna las horas de entrada y salida al DTO (máximo 5 pares).
     */
    private static void asignarHorarios(
            FichajeDiaDTO dto,
            List<LocalTime> entradas,
            List<LocalTime> salidas) {

        if (entradas.size() > 0) dto.setEntrada1(entradas.get(0));
        if (salidas.size() > 0) dto.setSalida1(salidas.get(0));

        if (entradas.size() > 1) dto.setEntrada2(entradas.get(1));
        if (salidas.size() > 1) dto.setSalida2(salidas.get(1));

        if (entradas.size() > 2) dto.setEntrada3(entradas.get(2));
        if (salidas.size() > 2) dto.setSalida3(salidas.get(2));

        if (entradas.size() > 3) dto.setEntrada4(entradas.get(3));
        if (salidas.size() > 3) dto.setSalida4(salidas.get(3));

        if (entradas.size() > 4) dto.setEntrada5(entradas.get(4));
        if (salidas.size() > 4) dto.setSalida5(salidas.get(4));
    }

    /**
     * Calcula las horas totales trabajadas sumando todos los pares entrada-salida.
     */
    private static double calcularHorasTotales(
            List<LocalTime> entradas,
            List<LocalTime> salidas) {

        double total = 0.0;
        int pares = Math.min(entradas.size(), salidas.size());

        for (int i = 0; i < pares; i++) {
            LocalDateTime entrada = LocalDate.now().atTime(entradas.get(i));
            LocalDateTime salida = LocalDate.now().atTime(salidas.get(i));

            // Manejar caso de salida al día siguiente
            if (salida.isBefore(entrada)) {
                salida = salida.plusDays(1);
            }

            Duration duracion = Duration.between(entrada, salida);
            total += duracion.toMinutes() / 60.0;
        }

        return total;
    }

    /**
     * Obtiene el clima más frecuente del día (moda estadística).
     */
    private static String obtenerClimaMasComun(List<String> climas) {
        if (climas.isEmpty()) {
            return "";
        }

        // Contar frecuencia de cada clima
        Map<String, Long> frecuencias = climas.stream()
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        // Devolver el más frecuente
        return frecuencias.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(climas.get(0));
    }

    /**
     * Cuenta cuántos días tienen fichajes incompletos (entrada sin salida).
     */
    public static int contarDiasIncompletos(List<Fichaje> fichajes) {
        Map<LocalDate, List<Fichaje>> porDia = fichajes.stream()
                .collect(Collectors.groupingBy(f -> f.getFechaHora().toLocalDate()));

        int incompletos = 0;

        for (List<Fichaje> fichajesDia : porDia.values()) {
            long entradas = fichajesDia.stream()
                    .filter(f -> f.getTipo() == TipoFichaje.ENTRADA)
                    .count();

            long salidas = fichajesDia.stream()
                    .filter(f -> f.getTipo() == TipoFichaje.SALIDA)
                    .count();

            if (entradas != salidas) {
                incompletos++;
            }
        }

        return incompletos;
    }
}