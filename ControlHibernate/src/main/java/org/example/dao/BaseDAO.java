package org.example.dao;

import org.example.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

public abstract class BaseDAO<T> {
    protected final Class<T> entityClass;

    protected BaseDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<T> buscarPorId(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorId: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean guardar(T entity) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.persist(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en guardar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(T entity) {
        String nombreClase = entityClass.getSimpleName();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(entity);
            tx.commit();
            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en actualizar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(Integer id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            T entity = session.get(entityClass, id);

            if (entity != null) {
                session.remove(entity);
                tx.commit();
                return true;
            } else {
                tx.rollback();
                return false;
            }

        } catch (Exception e) {
            System.err.println("   💥 ERROR en eliminar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<T> obtenerTodos() {
        String nombreClase = entityClass.getSimpleName();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            List<T> resultado = session.createQuery(hql, entityClass).list();
            return resultado;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerTodos: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public long contar() {
        String nombreClase = entityClass.getSimpleName();

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            Long count = session.createQuery(hql, Long.class).uniqueResult();

            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("   💥 ERROR en contar: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
}