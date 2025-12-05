package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.Trabajador;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class TrabajadorDAO {

    public Optional<Trabajador> autenticar(String numeroTarjeta, String pin) {

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta AND t.pin = :pin";

            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);
            query.setParameter("pin", pin);

            Optional<Trabajador> resultado = query.uniqueResultOptional();

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.autenticar(): " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Obtener todos los trabajadores ordenados por nombre
     */
    public List<Trabajador> obtenerTodos() {
        System.out.println("🗄️  TrabajadorDAO.obtenerTodos()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t ORDER BY t.nombre ASC";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            List<Trabajador> resultado = query.list();

            System.out.println("   ✅ Encontrados " + resultado.size() + " trabajadores");
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.obtenerTodos(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}