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

    /**
     * Buscar trabajador por número de tarjeta
     */
    public Optional<Trabajador> buscarPorNumeroTarjeta(String numeroTarjeta) {
        System.out.println("🗄️  TrabajadorDAO.buscarPorNumeroTarjeta(" + numeroTarjeta + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);

            Optional<Trabajador> resultado = query.uniqueResultOptional();
            System.out.println("   ✅ Resultado: " + (resultado.isPresent() ? "Encontrado" : "No encontrado"));
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.buscarPorNumeroTarjeta(): " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Buscar trabajador por ID
     */
    public Optional<Trabajador> buscarPorId(Integer id) {
        System.out.println("🗄️  TrabajadorDAO.buscarPorId(" + id + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Trabajador trabajador = session.get(Trabajador.class, id);
            System.out.println("   ✅ Resultado: " + (trabajador != null ? "Encontrado" : "No encontrado"));
            return Optional.ofNullable(trabajador);

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.buscarPorId(): " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Crear nuevo trabajador
     */
    public boolean crear(Trabajador trabajador) {
        System.out.println("🗄️  TrabajadorDAO.crear()");
        System.out.println("   Tarjeta: " + trabajador.getNumeroTarjeta() + ", Nombre: " + trabajador.getNombre());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.persist(trabajador);
            tx.commit();

            System.out.println("   ✅ Trabajador creado con ID: " + trabajador.getId());
            return true;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.crear(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualizar trabajador existente
     */
    public boolean actualizar(Trabajador trabajador) {
        System.out.println("🗄️  TrabajadorDAO.actualizar()");
        System.out.println("   ID: " + trabajador.getId() + ", Nombre: " + trabajador.getNombre());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();
            session.merge(trabajador);
            tx.commit();

            System.out.println("   ✅ Trabajador actualizado correctamente");
            return true;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.actualizar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Eliminar trabajador por ID
     * NOTA: También eliminará todos sus fichajes (CASCADE en BD)
     */
    public boolean eliminar(Integer id) {
        System.out.println("🗄️  TrabajadorDAO.eliminar(" + id + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var tx = session.beginTransaction();

            Trabajador trabajador = session.get(Trabajador.class, id);
            if (trabajador != null) {
                session.remove(trabajador);
                tx.commit();
                System.out.println("   ✅ Trabajador eliminado correctamente");
                return true;
            } else {
                tx.rollback();
                System.out.println("   ⚠️ Trabajador no encontrado");
                return false;
            }

        } catch (Exception e) {
            System.err.println("   💥 ERROR en TrabajadorDAO.eliminar(): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}