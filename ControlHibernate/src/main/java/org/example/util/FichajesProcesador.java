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

public class FichajesProcesador {

    private FichajesProcesador() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    public static List<FichajeDiaDTO> agruparFichajesPorDia(
            List<Fichaje> fichajes,
            boolean incluirDatosEmpleado) {

        if (fichajes == null || fichajes.isEmpty()) {
            return new ArrayList<>();
        }

        Map<ClaveEmpleadoDia, List<Fichaje>> fichajesPorEmpleadoYDia = fichajes.stream()
                .collect(Collectors.groupingBy(
                        f -> new ClaveEmpleadoDia(
                                f.getFechaHora().toLocalDate(),
                                f.getTrabajador().getId()
                        ),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<FichajeDiaDTO> resultado = new ArrayList<>();

        for (Map.Entry<ClaveEmpleadoDia, List<Fichaje>> entry : fichajesPorEmpleadoYDia.entrySet()) {
            LocalDate fecha = entry.getKey().fecha;
            List<Fichaje> fichajesEmpleadoDia = entry.getValue();

            fichajesEmpleadoDia.sort(Comparator.comparing(Fichaje::getFechaHora));

            FichajeDiaDTO dto = crearDTODia(fecha, fichajesEmpleadoDia, incluirDatosEmpleado);
            resultado.add(dto);
        }

        return resultado;
    }

    private static FichajeDiaDTO crearDTODia(
            LocalDate fecha,
            List<Fichaje> fichajes,
            boolean incluirDatosEmpleado) {

        FichajeDiaDTO dto = new FichajeDiaDTO();
        dto.setFecha(fecha);

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

        asignarHorarios(dto, entradas, salidas);

        double horasTotales = calcularHorasTotales(entradas, salidas);
        dto.setHorasTotales(horasTotales);

        String estado = entradas.size() == salidas.size() ? "✅ Completo" : "⚠️ Incompleto";
        dto.setEstado(estado);

        dto.setNotas(String.join("; ", notas));
        dto.setClima(obtenerClimaMasComun(climas));

        // ✨ IMPORTANTE: Establecer el trabajadorId SIEMPRE (no solo cuando incluirDatosEmpleado)
        if (!fichajes.isEmpty()) {
            Trabajador t = fichajes.get(0).getTrabajador();

            // ✨ NUEVO: Establecer el trabajadorId en el DTO
            dto.setTrabajadorId(t.getId());

            if (incluirDatosEmpleado) {
                dto.setNombreEmpleado(t.getNombreCompleto());
                dto.setNumeroTarjeta(t.getNumeroTarjeta());
            }
        }

        return dto;
    }

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

    private static double calcularHorasTotales(
            List<LocalTime> entradas,
            List<LocalTime> salidas) {

        double total = 0.0;
        int pares = Math.min(entradas.size(), salidas.size());

        for (int i = 0; i < pares; i++) {
            LocalDateTime entrada = LocalDate.now().atTime(entradas.get(i));
            LocalDateTime salida = LocalDate.now().atTime(salidas.get(i));

            if (salida.isBefore(entrada)) {
                salida = salida.plusDays(1);
            }

            Duration duracion = Duration.between(entrada, salida);
            total += duracion.toMinutes() / 60.0;
        }

        return total;
    }

    private static String obtenerClimaMasComun(List<String> climas) {
        if (climas.isEmpty()) {
            return "";
        }

        Map<String, Long> frecuencias = climas.stream()
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        return frecuencias.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(climas.get(0));
    }

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

    /**
     * Clase interna para agrupar fichajes por empleado y día
     */
    private static class ClaveEmpleadoDia implements Comparable<ClaveEmpleadoDia> {
        private final LocalDate fecha;
        private final Integer trabajadorId;

        public ClaveEmpleadoDia(LocalDate fecha, Integer trabajadorId) {
            this.fecha = fecha;
            this.trabajadorId = trabajadorId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClaveEmpleadoDia that = (ClaveEmpleadoDia) o;
            return Objects.equals(fecha, that.fecha) &&
                    Objects.equals(trabajadorId, that.trabajadorId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fecha, trabajadorId);
        }

        @Override
        public int compareTo(ClaveEmpleadoDia otro) {
            int comparacionFecha = this.fecha.compareTo(otro.fecha);
            if (comparacionFecha != 0) {
                return comparacionFecha;
            }
            return this.trabajadorId.compareTo(otro.trabajadorId);
        }
    }
}