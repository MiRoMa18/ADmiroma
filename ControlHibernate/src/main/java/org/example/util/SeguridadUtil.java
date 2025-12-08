package org.example.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidades de seguridad para hash de contraseñas.
 * NOTA: Requiere dependencia jbcrypt en pom.xml
 */
public class SeguridadUtil {

    // Nivel de complejidad del hash (10 = bueno, 12 = muy seguro pero más lento)
    private static final int WORKLOAD = 10;

    private SeguridadUtil() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }

    /**
     * Genera un hash BCrypt de un PIN en texto plano.
     *
     * @param pinPlano PIN sin hashear
     * @return Hash del PIN
     */
    public static String hashearPin(String pinPlano) {
        if (pinPlano == null || pinPlano.isEmpty()) {
            throw new IllegalArgumentException("El PIN no puede estar vacío");
        }
        return BCrypt.hashpw(pinPlano, BCrypt.gensalt(WORKLOAD));
    }

    /**
     * Verifica si un PIN en texto plano coincide con su hash.
     *
     * @param pinPlano PIN a verificar
     * @param pinHasheado Hash almacenado en BD
     * @return true si el PIN es correcto
     */
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