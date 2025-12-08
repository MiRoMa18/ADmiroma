package org.example.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class HorasFormateador {

    private HorasFormateador() {
        // Clase de utilidad, no instanciable
        throw new UnsupportedOperationException("Clase de utilidad");
    }

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

    public static String formatearHorasDecimal(Double horas) {
        if (horas == null || horas == 0.0) {
            return "0.0h";
        }
        return String.format("%.1fh", horas);
    }

    public static double calcularHoras(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return 0.0;
        }

        Duration duracion = Duration.between(inicio, fin);
        return duracion.toMinutes() / 60.0;
    }

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

    public static boolean esHorasValidas(Double horas) {
        return horas != null && horas >= 0 && horas <= 24;
    }
}