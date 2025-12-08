package org.example.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utilidad para formatear y calcular horas de trabajo.
 * Convierte entre formatos decimales (8.92) y legibles (8h 55m).
 */
public class HorasFormateador {

    private HorasFormateador() {
        // Clase de utilidad, no instanciable
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Convierte horas decimales a formato legible "Xh Ym".
     *
     * @param horas Horas en formato decimal (ej: 8.92 = 8 horas 55 minutos)
     * @return String formateado (ej: "8h 55m")
     */
    public static String formatearHoras(Double horas) {
        if (horas == null || horas == 0.0) {
            return "0h 0m";
        }

        int horasEnteras = horas.intValue();
        int minutos = (int) Math.round((horas - horasEnteras) * 60);

        // Manejar redondeo a 60 minutos
        if (minutos == 60) {
            horasEnteras++;
            minutos = 0;
        }

        return horasEnteras + "h " + minutos + "m";
    }

    /**
     * Formatea horas con un decimal (para promedios y estadísticas).
     *
     * @param horas Horas en formato decimal
     * @return String formateado (ej: "8.5h")
     */
    public static String formatearHorasDecimal(Double horas) {
        if (horas == null || horas == 0.0) {
            return "0.0h";
        }
        return String.format("%.1fh", horas);
    }

    /**
     * Calcula las horas entre dos momentos.
     *
     * @param inicio Momento de inicio
     * @param fin Momento de fin
     * @return Horas en formato decimal
     */
    public static double calcularHoras(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return 0.0;
        }

        Duration duracion = Duration.between(inicio, fin);
        return duracion.toMinutes() / 60.0;
    }

    /**
     * Convierte String de horas (ej: "8h 55m") a decimal.
     *
     * @param horasString String en formato "Xh Ym"
     * @return Horas en formato decimal
     */
    public static double parsearHoras(String horasString) {
        if (horasString == null || horasString.isEmpty()) {
            return 0.0;
        }

        try {
            String[] partes = horasString.replace("h", "").replace("m", "").trim().split(" ");
            int horas = Integer.parseInt(partes[0]);
            int minutos = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;

            return horas + (minutos / 60.0);

        } catch (Exception e) {
            System.err.println("Error parseando horas: " + horasString);
            return 0.0;
        }
    }

    /**
     * Valida si un valor de horas es razonable (0-24h por día).
     *
     * @param horas Horas a validar
     * @return true si está en rango válido
     */
    public static boolean esHorasValidas(Double horas) {
        return horas != null && horas >= 0 && horas <= 24;
    }
}