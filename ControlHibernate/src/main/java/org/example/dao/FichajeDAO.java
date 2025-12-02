package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.Fichaje;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * Buscar fichajes de un trabajador en un día específico
     */
    public List<Fichaje> buscarPorTrabajadorYFecha(int trabajadorId, LocalDate fecha) {
        return buscarPorTrabajadorYRango(trabajadorId, fecha, fecha);
    }
}