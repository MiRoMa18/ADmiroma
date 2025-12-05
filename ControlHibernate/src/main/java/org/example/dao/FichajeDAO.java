package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.Fichaje;
import org.example.model.Trabajador;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FichajeDAO {

    // ========== MÉTODOS EXISTENTES ==========

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

    public boolean guardar(Fichaje fichaje) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(fichaje);
            tx.commit();
            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.guardar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ========== NUEVOS MÉTODOS CRUD PARA ADMIN ==========

    /**
     * Obtener TODOS los fichajes con información del trabajador (EAGER fetch)
     * Ordenados por fecha descendente
     */
    public List<Fichaje> obtenerTodos() {
        System.out.println("🗄️  FichajeDAO.obtenerTodos()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f " +
                    "LEFT JOIN FETCH f.trabajador " +
                    "ORDER BY f.fechaHora DESC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            List<Fichaje> resultado = query.list();

            System.out.println("   ✅ Encontrados " + resultado.size() + " fichajes");
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.obtenerTodos(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Buscar fichajes con filtros opcionales
     * @param trabajadorId ID del trabajador (null = todos)
     * @param fechaInicio Fecha inicio (null = sin filtro)
     * @param fechaFin Fecha fin (null = sin filtro)
     * @param tipo Tipo de fichaje: "ENTRADA", "SALIDA", null = todos
     */
    public List<Fichaje> buscarConFiltros(Integer trabajadorId, LocalDate fechaInicio,
                                          LocalDate fechaFin, String tipo) {
        System.out.println("🗄️  FichajeDAO.buscarConFiltros()");
        System.out.println("   Filtros: trabajador=" + trabajadorId + ", fechas=" + fechaInicio +
                " a " + fechaFin + ", tipo=" + tipo);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Fichaje f LEFT JOIN FETCH f.trabajador WHERE 1=1 ");

            // Filtro por trabajador
            if (trabajadorId != null) {
                hql.append("AND f.trabajador.id = :trabajadorId ");
            }

            // Filtro por rango de fechas
            if (fechaInicio != null && fechaFin != null) {
                hql.append("AND f.fechaHora BETWEEN :inicio AND :fin ");
            }

            // Filtro por tipo
            if (tipo != null && !tipo.equals("TODOS")) {
                hql.append("AND f.tipo = :tipo ");
            }

            hql.append("ORDER BY f.fechaHora DESC");

            Query<Fichaje> query = session.createQuery(hql.toString(), Fichaje.class);

            // Setear parámetros
            if (trabajadorId != null) {
                query.setParameter("trabajadorId", trabajadorId);
            }

            if (fechaInicio != null && fechaFin != null) {
                LocalDateTime inicio = fechaInicio.atStartOfDay();
                LocalDateTime fin = fechaFin.atTime(23, 59, 59);
                query.setParameter("inicio", inicio);
                query.setParameter("fin", fin);
            }

            if (tipo != null && !tipo.equals("TODOS")) {
                query.setParameter("tipo", org.example.model.TipoFichaje.valueOf(tipo));
            }

            List<Fichaje> resultado = query.list();
            System.out.println("   ✅ Encontrados " + resultado.size() + " fichajes con filtros");

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.buscarConFiltros(): " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Buscar un fichaje por su ID
     */
    public Optional<Fichaje> buscarPorId(Integer id) {
        System.out.println("🗄️  FichajeDAO.buscarPorId(" + id + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f LEFT JOIN FETCH f.trabajador WHERE f.id = :id";
            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("id", id);

            Optional<Fichaje> resultado = query.uniqueResultOptional();
            if (resultado.isPresent()) {
                System.out.println("   ✅ Fichaje encontrado: " + resultado.get());
            } else {
                System.out.println("   ⚠️ No se encontró fichaje con ID " + id);
            }

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Actualizar un fichaje existente
     */
    public boolean actualizar(Fichaje fichaje) {
        System.out.println("🗄️  FichajeDAO.actualizar() - ID: " + fichaje.getId());

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.merge(fichaje);

            tx.commit();
            System.out.println("   ✅ Fichaje actualizado correctamente");
            return true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("   💥 ERROR en FichajeDAO.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar un fichaje por su ID
     */
    public boolean eliminar(Integer id) {
        System.out.println("🗄️  FichajeDAO.eliminar() - ID: " + id);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Fichaje fichaje = session.get(Fichaje.class, id);
            if (fichaje != null) {
                session.remove(fichaje);
                tx.commit();
                System.out.println("   ✅ Fichaje eliminado correctamente");
                return true;
            } else {
                System.out.println("   ⚠️ No se encontró fichaje con ID " + id);
                return false;
            }

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            System.err.println("   💥 ERROR en FichajeDAO.eliminar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Verificar si existe un fichaje duplicado
     * (mismo trabajador, misma fecha/hora exacta)
     */
    public boolean existeDuplicado(Integer trabajadorId, LocalDateTime fechaHora, Integer excluirId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Fichaje f " +
                    "WHERE f.trabajador.id = :trabajadorId " +
                    "AND f.fechaHora = :fechaHora ";

            if (excluirId != null) {
                hql += "AND f.id != :excluirId";
            }

            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("trabajadorId", trabajadorId);
            query.setParameter("fechaHora", fechaHora);

            if (excluirId != null) {
                query.setParameter("excluirId", excluirId);
            }

            Long count = query.uniqueResult();
            return count != null && count > 0;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en FichajeDAO.existeDuplicado(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}