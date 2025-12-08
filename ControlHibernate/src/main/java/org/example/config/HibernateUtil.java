package org.example.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utilidad para gestionar la SessionFactory de Hibernate.
 * Patrón Singleton para garantizar una única instancia.
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            System.out.println("🔧 Inicializando Hibernate...");

            // Crear SessionFactory desde hibernate.cfg.xml
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();

            System.out.println("✅ Hibernate inicializado correctamente");

        } catch (Throwable ex) {
            System.err.println("💥 ERROR: Fallo al crear SessionFactory");
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Obtiene la SessionFactory de Hibernate.
     *
     * @return SessionFactory única del sistema
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Cierra la SessionFactory al finalizar la aplicación.
     * Debe llamarse al cerrar el programa.
     */
    public static void shutdown() {
        if (sessionFactory != null) {
            System.out.println("🔧 Cerrando Hibernate...");
            sessionFactory.close();
            System.out.println("✅ Hibernate cerrado correctamente");
        }
    }
}