package org.example.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidadorUtil {

    private ValidadorUtil() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    public static boolean esNumeroTarjetaValido(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.trim().isEmpty()) {
            return false;
        }
        return numeroTarjeta.matches("\\d{4,20}");
    }

    public static boolean esPinValido(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        return pin.length() >= 4 &&
                pin.length() <= 10 &&
                pin.matches("\\d+");
    }

    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean esFechaValida(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !fecha.isAfter(hoy);
    }

    public static boolean esHoraValida(String hora) {
        if (hora == null || hora.trim().isEmpty()) {
            return false;
        }

        try {
            LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return nombre.length() >= 2 && nombre.length() <= 100;
    }

    public static boolean esRangoFechasValido(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            return false;
        }
        return !inicio.isAfter(fin);
    }
}