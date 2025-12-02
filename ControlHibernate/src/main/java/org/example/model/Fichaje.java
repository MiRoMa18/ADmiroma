package org.example.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fichaje")
public class Fichaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajador_id", nullable = false)
    private Trabajador trabajador;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoFichaje tipo;

    @Column(length = 50)
    private String clima;

    @Column(columnDefinition = "TEXT")
    private String notas;

    public Fichaje() {
    }

    public Fichaje(Trabajador trabajador, LocalDateTime fechaHora, TipoFichaje tipo,
                   String clima, String notas) {
        this.trabajador = trabajador;
        this.fechaHora = fechaHora;
        this.tipo = tipo;
        this.clima = clima;
        this.notas = notas;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Trabajador getTrabajador() {
        return trabajador;
    }

    public void setTrabajador(Trabajador trabajador) {
        this.trabajador = trabajador;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoFichaje getTipo() {
        return tipo;
    }

    public void setTipo(TipoFichaje tipo) {
        this.tipo = tipo;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    @Override
    public String toString() {
        return "Fichaje{" +
                "id=" + id +
                ", trabajador=" + (trabajador != null ? trabajador.getNombre() : "null") +
                ", fechaHora=" + fechaHora +
                ", tipo=" + tipo +
                '}';
    }
}