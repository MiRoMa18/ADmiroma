package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.entity.Trabajador;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class TrabajadorDAO extends BaseDAO<Trabajador> {

    public TrabajadorDAO() {
        super(Trabajador.class);
    }

    public Optional<Trabajador> autenticar(String numeroTarjeta, String pin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Buscar trabajador por número de tarjeta
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);

            Optional<Trabajador> trabajadorOpt = query.uniqueResultOptional();

            // Verificar PIN directamente
            if (trabajadorOpt.isPresent()) {
                Trabajador trabajador = trabajadorOpt.get();
                if (pin.equals(trabajador.getPin())) {
                    return trabajadorOpt;
                }
            }
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("   💥 ERROR en autenticar: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<Trabajador> buscarPorNumeroTarjeta(String numeroTarjeta) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);

            Optional<Trabajador> resultado = query.uniqueResultOptional();
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorNumeroTarjeta: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<Trabajador> obtenerTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t ORDER BY t.nombre ASC, t.apellidos ASC";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            List<Trabajador> resultado = query.list();
            return resultado;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean existeNumeroTarjeta(String numeroTarjeta) {
        return buscarPorNumeroTarjeta(numeroTarjeta).isPresent();
    }

    public boolean existeNumeroTarjetaExcluyendo(String numeroTarjeta, Integer excluirId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta AND t.id != :id";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);
            query.setParameter("id", excluirId);
            boolean existe = query.uniqueResultOptional().isPresent();
            return existe;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en existeNumeroTarjetaExcluyendo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}