package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.Trabajador;
import org.hibernate.Session;
import org.hibernate.query.Query;

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
}