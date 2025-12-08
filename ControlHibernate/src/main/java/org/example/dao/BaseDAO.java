package org.example.dao;

import org.example.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * Clase base para todos los DAOs con operaciones CRUD genéricas.
 * Reduce código duplicado y asegura manejo consistente de sesiones.
 *
 * @param <T> Tipo de la entidad que maneja este DAO
 */
public abstract class BaseDAO<T> {

    protected final Class<T> entityClass;

    /**
     * Constructor que requiere la clase de la entidad.
     *
     * @param entityClass Clase de la entidad (ej: Trabajador.class)
     */
    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Busca una entidad por su ID.
     *
     * @param id ID de la entidad
     * @return Optional con la entidad si existe
     */
    public Optional<T> buscarPorId(Integer id) {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.buscarPorId(" + id + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);

            System.out.println("   ✅ Resultado: " +
                    (entity != null ? "Encontrado" : "No encontrado"));

            return Optional.ofNullable(entity);

        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Guarda una nueva entidad en la base de datos.
     *
     * @param entity Entidad a guardar
     * @return true si se guardó correctamente
     */
    public boolean guardar(T entity) {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.guardar()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();

            System.out.println("   ✅ Entidad guardada correctamente");
            return true;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en guardar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza una entidad existente.
     *
     * @param entity Entidad a actualizar
     * @return true si se actualizó correctamente
     */
    public boolean actualizar(T entity) {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.actualizar()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();

            System.out.println("   ✅ Entidad actualizada correctamente");
            return true;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una entidad por su ID.
     *
     * @param id ID de la entidad a eliminar
     * @return true si se eliminó correctamente
     */
    public boolean eliminar(Integer id) {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.eliminar(" + id + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            T entity = session.get(entityClass, id);

            if (entity != null) {
                session.remove(entity);
                tx.commit();
                System.out.println("   ✅ Entidad eliminada correctamente");
                return true;
            } else {
                tx.rollback();
                System.out.println("   ⚠️ Entidad no encontrada");
                return false;
            }

        } catch (Exception e) {
            System.err.println("   💥 ERROR en eliminar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todas las entidades de este tipo.
     *
     * @return Lista de todas las entidades
     */
    public List<T> obtenerTodos() {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.obtenerTodos()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            List<T> resultado = session.createQuery(hql, entityClass).list();

            System.out.println("   ✅ Encontrados " + resultado.size() + " registros");
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerTodos: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Cuenta el total de entidades de este tipo.
     *
     * @return Número total de registros
     */
    public long contar() {
        String nombreClase = entityClass.getSimpleName();
        System.out.println("🗄️  " + nombreClase + "DAO.contar()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            Long count = session.createQuery(hql, Long.class).uniqueResult();

            System.out.println("   ✅ Total: " + count);
            return count != null ? count : 0;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en contar: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}