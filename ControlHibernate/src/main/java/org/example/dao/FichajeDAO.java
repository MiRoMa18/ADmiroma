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

/**
 * DAO para la entidad Fichaje.
 * Hereda operaciones CRUD genéricas de BaseDAO y añade métodos específicos.
 */
public class FichajeDAO extends BaseDAO<Fichaje> {

    public FichajeDAO() {
        super(Fichaje.class);
    }

    /**
     * Busca fichajes de un trabajador en un rango de fechas.
     *
     * @param trabajadorId ID del trabajador
     * @param fechaInicio Fecha de inicio (inclusive)
     * @param fechaFin Fecha de fin (inclusive)
     * @return Lista de fichajes ordenados por fecha/hora ascendente
     */
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
            System.err.println("   💥 ERROR en buscarPorTrabajadorYRango: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Obtiene el último fichaje de un trabajador.
     * Útil para detectar si la próxima acción debe ser ENTRADA o SALIDA.
     *
     * @param trabajadorId ID del trabajador
     * @return Optional con el último fichaje si existe
     */
    public Optional<Fichaje> obtenerUltimoFichaje(Integer trabajadorId) {
        System.out.println("🗄️  FichajeDAO.obtenerUltimoFichaje(" + trabajadorId + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f WHERE f.trabajador.id = :id ORDER BY f.fechaHora DESC";
            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            query.setParameter("id", trabajadorId);
            query.setMaxResults(1);

            Optional<Fichaje> resultado = query.uniqueResultOptional();

            if (resultado.isPresent()) {
                System.out.println("   ✅ Último fichaje: " + resultado.get().getTipo() +
                        " a las " + resultado.get().getFechaHora().toLocalTime());
            } else {
                System.out.println("   ℹ️  No hay fichajes previos");
            }

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerUltimoFichaje: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Obtiene TODOS los fichajes con información del trabajador (EAGER fetch).
     * Ordenados por fecha descendente (más recientes primero).
     *
     * @return Lista de todos los fichajes del sistema
     */
    public List<Fichaje> obtenerTodosConTrabajador() {
        System.out.println("🗄️  FichajeDAO.obtenerTodosConTrabajador()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Fichaje f " +
                    "LEFT JOIN FETCH f.trabajador " +
                    "ORDER BY f.fechaHora DESC";

            Query<Fichaje> query = session.createQuery(hql, Fichaje.class);
            List<Fichaje> resultado = query.list();

            System.out.println("   ✅ Encontrados " + resultado.size() + " fichajes");
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerTodosConTrabajador: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Busca fichajes aplicando filtros opcionales.
     *
     * @param trabajadorId ID del trabajador (null para todos)
     * @param fechaInicio Fecha de inicio (null para sin límite)
     * @param fechaFin Fecha de fin (null para sin límite)
     * @param tipo Tipo de fichaje: "ENTRADA", "SALIDA" o null para todos
     * @return Lista de fichajes que cumplen los criterios
     */
    public List<Fichaje> buscar(Integer trabajadorId, LocalDate fechaInicio,
                                LocalDate fechaFin, String tipo) {
        System.out.println("🗄️  FichajeDAO.buscar() con filtros");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder("FROM Fichaje f ");
            hql.append("LEFT JOIN FETCH f.trabajador ");
            hql.append("WHERE 1=1 ");

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

            // Asignar parámetros
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
            System.out.println("   ✅ Encontrados " + resultado.size() + " fichajes con filtros");

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscar: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cuenta los fichajes de un trabajador en un rango de fechas.
     *
     * @param trabajadorId ID del trabajador
     * @param fechaInicio Fecha de inicio
     * @param fechaFin Fecha de fin
     * @return Número de fichajes
     */
    public long contarPorTrabajadorYRango(int trabajadorId, LocalDate fechaInicio, LocalDate fechaFin) {
        System.out.println("🗄️  FichajeDAO.contarPorTrabajadorYRango()");

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

            System.out.println("   ✅ Total: " + resultado);
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en contarPorTrabajadorYRango: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Verifica si un trabajador tiene fichajes registrados.
     * Útil antes de eliminar un trabajador.
     *
     * @param trabajadorId ID del trabajador
     * @return true si tiene fichajes
     */
    public boolean tieneFichajes(Integer trabajadorId) {
        System.out.println("🗄️  FichajeDAO.tieneFichajes(" + trabajadorId + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(f) FROM Fichaje f WHERE f.trabajador.id = :id";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("id", trabajadorId);

            Long count = query.uniqueResult();
            boolean tiene = count != null && count > 0;

            System.out.println("   ✅ Tiene fichajes: " + tiene + " (" + count + ")");
            return tiene;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en tieneFichajes: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Los métodos guardar(), actualizar(), eliminar() y buscarPorId()
    // se heredan de BaseDAO<Fichaje> y no necesitan ser reimplementados
}