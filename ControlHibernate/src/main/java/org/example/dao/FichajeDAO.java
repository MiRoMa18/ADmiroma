package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.Fichaje;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FichajeDAO {

    public List<Fichaje> buscarPorTrabajadorYRango(int trabajadorId, LocalDate fechaInicio, LocalDate fechaFin) {
        System.out.println("🗄️  FichajeDAO.buscarPorTrabajadorYRango()");
        System.out.println("   Trabajador ID: " + trabajadorId);
        System.out.println("   Rango: " + fechaInicio + " - " + fechaFin);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);

            String hql = "FROM Fichaje f WHERE f.trabajador.id = :trabajadorId " +
                    "AND f.fechaHora BETWEEN :inicio AND :fin " +
                    "ORDER BY f.fechaHora ASC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("trabajadorId", trabajadorId);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);

            List<Fichaje> resultado = query.list();
            System.out.println("   ✅ Encontrados " + resultado.size() + " fichajes");

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.buscarPorTrabajadorYRango(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public Optional<Fichaje> obtenerUltimoFichaje(Integer trabajadorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f WHERE f.trabajador.id = :id ORDER BY f.fechaHora DESC";
            Query<Fichaje> q = session.createQuery(hql, Fichaje.class);
            q.setParameter("id", trabajadorId);
            q.setMaxResults(1);
            return q.uniqueResultOptional();
        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.obtenerUltimoFichaje(): " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Método para guardar un fichaje
    public boolean guardar(Fichaje fichaje) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.persist(fichaje);
            tx.commit();
            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.guardar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}