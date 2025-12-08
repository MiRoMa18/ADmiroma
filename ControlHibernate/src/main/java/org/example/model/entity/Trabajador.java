package org.example.model.entity;

import jakarta.persistence.*;
import org.example.model.enums.Rol;

import java.time.LocalDate;

@Entity
@Table(name = "trabajador")
public class Trabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "numero_tarjeta", unique = true, nullable = false, length = 20)
    private String numeroTarjeta;

    @Column(nullable = false, length = 100)
    private String pin;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol = Rol.TRABAJADOR;

    @Column(name = "fecha_alta")
    private LocalDate fechaAlta;

    public Trabajador() {
    }
    public Trabajador(String numeroTarjeta, String pin, String nombre, String apellidos,
                      String email, Rol rol, LocalDate fechaAlta) {
        this.numeroTarjeta = numeroTarjeta;
        this.pin = pin;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.rol = rol;
        this.fechaAlta = fechaAlta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    @Override
    public String toString() {
        return "Trabajador{" +
                "id=" + id +
                ", numeroTarjeta='" + numeroTarjeta + '\'' +
                ", nombre='" + nombre + '\'' +
                ", rol=" + rol +
                '}';
    }
}