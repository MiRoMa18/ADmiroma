package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.entity.Fichaje;
import org.example.model.enums.TipoFichaje;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FichajeDAO extends BaseDAO<Fichaje> {

    public FichajeDAO() {
        super(Fichaje.class);
    }

    /**
     * Busca fichajes por trabajador y rango de fechas
     * ✨ CORREGIDO: Incluye JOIN FETCH para cargar el trabajador de forma eager
     */
    public List<Fichaje> buscarPorTrabajadorYRango(int trabajadorId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);

            // ✨ JOIN FETCH para cargar el trabajador inmediatamente
            String hql = "FROM Fichaje f " +
                    "LEFT JOIN FETCH f.trabajador " +
                    "WHERE f.trabajador.id = :trabajadorId " +
                    "AND f.fechaHora BETWEEN :inicio AND :fin " +
                    "ORDER BY f.fechaHora ASC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("trabajadorId", trabajadorId);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);

            List<Fichaje> resultado = query.list();
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorTrabajadorYRango: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene el último fichaje de un trabajador
     * ✨ CORREGIDO: Incluye JOIN FETCH para cargar el trabajador de forma eager
     */
    public Optional<Fichaje> obtenerUltimoFichaje(Integer trabajadorId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // ✨ JOIN FETCH para cargar el trabajador inmediatamente
            String hql = "FROM Fichaje f " +
                    "LEFT JOIN FETCH f.trabajador " +
                    "WHERE f.trabajador.id = :id " +
                    "ORDER BY f.fechaHora DESC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("id", trabajadorId);
            query.setMaxResults(1);

            Optional<Fichaje> resultado = query.uniqueResultOptional();
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerUltimoFichaje: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los fichajes con sus trabajadores
     * ✨ YA TIENE JOIN FETCH - Sin cambios
     */
    public List<Fichaje> obtenerTodosConTrabajador() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f " +
                    "LEFT JOIN FETCH f.trabajador " +
                    "ORDER BY f.fechaHora DESC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            List<Fichaje> resultado = query.list();
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerTodosConTrabajador: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Busca fichajes con filtros
     * ✨ YA TIENE JOIN FETCH - Sin cambios
     */
    public List<Fichaje> buscar(Integer trabajadorId, LocalDate fechaInicio,
                                LocalDate fechaFin, String tipo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Fichaje f ");
            hql.append("LEFT JOIN FETCH f.trabajador ");
            hql.append("WHERE 1=1 ");

            if (trabajadorId != null) {
                hql.append("AND f.trabajador.id = :trabajadorId ");
            }

            if (fechaInicio != null && fechaFin != null) {
                hql.append("AND f.fechaHora BETWEEN :inicio AND :fin ");
            }

            if (tipo != null && !tipo.equals("TODOS")) {
                hql.append("AND f.tipo = :tipo ");
            }

            hql.append("ORDER BY f.fechaHora DESC");
            Query<Fichaje> query = session.createQuery(hql.toString(), Fichaje.class);

            if (trabajadorId != null) {
                query.setParameter("trabajadorId", trabajadorId);
            }

            if (fechaInicio != null && fechaFin != null) {
                query.setParameter("inicio", fechaInicio.atStartOfDay());
                query.setParameter("fin", fechaFin.atTime(23, 59, 59));
            }

            if (tipo != null && !tipo.equals("TODOS")) {
                query.setParameter("tipo", TipoFichaje.valueOf(tipo));
            }

            List<Fichaje> resultado = query.list();
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscar: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cuenta fichajes por trabajador y rango de fechas
     */
    public long contarPorTrabajadorYRango(int trabajadorId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);

            String hql = "SELECT COUNT(f) FROM Fichaje f " +
                    "WHERE f.trabajador.id = :trabajadorId " +
                    "AND f.fechaHora BETWEEN :inicio AND :fin";

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("trabajadorId", trabajadorId);
            query.setParameter("inicio", inicio);
            query.setParameter("fin", fin);

            Long count = query.uniqueResult();
            long resultado = count != null ? count : 0;
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en contarPorTrabajadorYRango: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Verifica si un trabajador tiene fichajes
     */
    public boolean tieneFichajes(Integer trabajadorId) {
        System.out.println("🗄️ FichajeDAO.tieneFichajes(" + trabajadorId + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Fichaje f WHERE f.trabajador.id = :id";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("id", trabajadorId);

            Long count = query.uniqueResult();
            boolean tiene = count != null && count > 0;
            return tiene;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en tieneFichajes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sobrescribe el método buscarPorId de BaseDAO para incluir JOIN FETCH
     * ✨ NUEVO: Versión que carga el trabajador de forma eager
     */
    @Override
    public Optional<Fichaje> buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // ✨ JOIN FETCH para cargar el trabajador inmediatamente
            String hql = "FROM Fichaje f LEFT JOIN FETCH f.trabajador WHERE f.id = :id";
            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("id", id);

            Fichaje entity = query.uniqueResult();
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }
}