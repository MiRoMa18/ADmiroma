package org.example.util;

import org.mindrot.jbcrypt.BCrypt;

public class SeguridadUtil {

    private static final int WORKLOAD = 10;

    private SeguridadUtil() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    public static String hashearPin(String pinPlano) {
        if (pinPlano == null || pinPlano.isEmpty()) {
            throw new IllegalArgumentException("El PIN no puede estar vacío");
        }
        return BCrypt.hashpw(pinPlano, BCrypt.gensalt(WORKLOAD));
    }

    public static boolean verificarPin(String pinPlano, String pinHasheado) {
        if (pinPlano == null || pinHasheado == null) {
            return false;
        }

        try {
            return BCrypt.checkpw(pinPlano, pinHasheado);
        } catch (Exception e) {
            System.err.println("Error verificando PIN: " + e.getMessage());
            return false;
        }
    }
}