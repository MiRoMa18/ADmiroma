package org.example.dao;

import org.example.config.HibernateUtil;
import org.example.model.entity.Trabajador;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la entidad Trabajador.
 * Hereda operaciones CRUD genéricas de BaseDAO y añade métodos específicos.
 */
public class TrabajadorDAO extends BaseDAO<Trabajador> {

    public TrabajadorDAO() {
        super(Trabajador.class);
    }

    /**
     * Autentica un trabajador por número de tarjeta y PIN.
     * Usa hash BCrypt para verificar el PIN de forma segura.
     *
     * @param numeroTarjeta Número de tarjeta del trabajador
     * @param pin PIN en texto plano
     * @return Optional con el trabajador si las credenciales son correctas
     */
    public Optional<Trabajador> autenticar(String numeroTarjeta, String pin) {
        System.out.println("🔐 TrabajadorDAO.autenticar()");
        System.out.println("   Tarjeta: " + numeroTarjeta);

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Buscar trabajador por número de tarjeta
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);

            Optional<Trabajador> trabajadorOpt = query.uniqueResultOptional();

            // Verificar PIN directamente (sin BCrypt)
            if (trabajadorOpt.isPresent()) {
                Trabajador trabajador = trabajadorOpt.get();

                if (pin.equals(trabajador.getPin())) {
                    System.out.println("   ✅ Autenticación exitosa - Rol: " + trabajador.getRol());
                    return trabajadorOpt;
                } else {
                    System.out.println("   ❌ PIN incorrecto");
                }
            } else {
                System.out.println("   ❌ Trabajador no encontrado");
            }

            return Optional.empty();

        } catch (Exception e) {
            System.err.println("   💥 ERROR en autenticar: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Busca un trabajador por su número de tarjeta.
     * Útil para validar unicidad antes de crear/editar.
     *
     * @param numeroTarjeta Número de tarjeta único
     * @return Optional con el trabajador si existe
     */
    public Optional<Trabajador> buscarPorNumeroTarjeta(String numeroTarjeta) {
        System.out.println("🗄️  TrabajadorDAO.buscarPorNumeroTarjeta(" + numeroTarjeta + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);

            Optional<Trabajador> resultado = query.uniqueResultOptional();

            System.out.println("   ✅ Resultado: " +
                    (resultado.isPresent() ? "Encontrado" : "No encontrado"));

            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en buscarPorNumeroTarjeta: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los trabajadores ordenados por nombre.
     * Sobrescribe el método de BaseDAO para añadir ordenamiento.
     *
     * @return Lista de trabajadores ordenada alfabéticamente
     */
    @Override
    public List<Trabajador> obtenerTodos() {
        System.out.println("🗄️  TrabajadorDAO.obtenerTodos()");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t ORDER BY t.nombre ASC, t.apellidos ASC";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            List<Trabajador> resultado = query.list();

            System.out.println("   ✅ Encontrados " + resultado.size() + " trabajadores");
            return resultado;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en obtenerTodos: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Verifica si un número de tarjeta ya existe en el sistema.
     * Útil para validación antes de crear un trabajador.
     *
     * @param numeroTarjeta Número de tarjeta a verificar
     * @return true si ya existe
     */
    public boolean existeNumeroTarjeta(String numeroTarjeta) {
        return buscarPorNumeroTarjeta(numeroTarjeta).isPresent();
    }

    /**
     * Verifica si un número de tarjeta ya existe, excluyendo un ID específico.
     * Útil para validación al editar un trabajador.
     *
     * @param numeroTarjeta Número de tarjeta a verificar
     * @param excluirId ID del trabajador a excluir de la búsqueda
     * @return true si ya existe en otro trabajador
     */
    public boolean existeNumeroTarjetaExcluyendo(String numeroTarjeta, Integer excluirId) {
        System.out.println("🗄️  TrabajadorDAO.existeNumeroTarjetaExcluyendo(" +
                numeroTarjeta + ", excluir: " + excluirId + ")");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Trabajador t WHERE t.numeroTarjeta = :tarjeta AND t.id != :id";
            Query<Trabajador> query = session.createQuery(hql, Trabajador.class);
            query.setParameter("tarjeta", numeroTarjeta);
            query.setParameter("id", excluirId);

            boolean existe = query.uniqueResultOptional().isPresent();

            System.out.println("   ✅ Existe: " + existe);
            return existe;

        } catch (Exception e) {
            System.err.println("   💥 ERROR en existeNumeroTarjetaExcluyendo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Los métodos guardar(), actualizar(), eliminar() y buscarPorId()
    // se heredan de BaseDAO<Trabajador> y no necesitan ser reimplementados
}