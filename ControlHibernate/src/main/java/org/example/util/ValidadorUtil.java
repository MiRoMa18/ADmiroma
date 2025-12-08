package org.example.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validaciones de datos de entrada del usuario.
 */
public class ValidadorUtil {

    private ValidadorUtil() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Valida que un número de tarjeta sea correcto.
     * Debe tener entre 4 y 20 dígitos.
     */
    public static boolean esNumeroTarjetaValido(String numeroTarjeta) {
        if (numeroTarjeta == null || numeroTarjeta.trim().isEmpty()) {
            return false;
        }
        return numeroTarjeta.matches("\\d{4,20}");
    }

    /**
     * Valida que un PIN sea correcto.
     * Debe tener entre 4 y 10 dígitos.
     */
    public static boolean esPinValido(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        return pin.length() >= 4 &&
                pin.length() <= 10 &&
                pin.matches("\\d+");
    }

    /**
     * Valida que un email tenga formato correcto.
     * Email es opcional, así que null/vacío es válido.
     */
    public static boolean esEmailValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email es opcional
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Valida que una fecha no sea futura.
     */
    public static boolean esFechaValida(LocalDate fecha) {
        if (fecha == null) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !fecha.isAfter(hoy);
    }

    /**
     * Valida que una string tenga formato de hora válido (HH:mm).
     */
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

    /**
     * Valida que un nombre no esté vacío y tenga longitud razonable.
     */
    public static boolean esNombreValido(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        return nombre.length() >= 2 && nombre.length() <= 100;
    }

    /**
     * Valida que un rango de fechas sea válido (inicio <= fin).
     */
    public static boolean esRangoFechasValido(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            return false;
        }
        return !inicio.isAfter(fin);
    }
}