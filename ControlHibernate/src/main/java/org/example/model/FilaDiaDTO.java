package org.example.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class FilaDiaDTO {
    private LocalDate fecha;
    private LocalTime primeraEntrada;
    private LocalTime ultimaSalida;
    private Double totalHoras;
    private String clima;
    private boolean fichajeIncompleto;

    public FilaDiaDTO() {
    }

    public FilaDiaDTO(LocalDate fecha, LocalTime primeraEntrada, LocalTime ultimaSalida,
                      Double totalHoras, String clima, boolean fichajeIncompleto) {
        this.fecha = fecha;
        this.primeraEntrada = primeraEntrada;
        this.ultimaSalida = ultimaSalida;
        this.totalHoras = totalHoras;
        this.clima = clima;
        this.fichajeIncompleto = fichajeIncompleto;
    }

    // Getters y Setters
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getPrimeraEntrada() {
        return primeraEntrada;
    }

    public void setPrimeraEntrada(LocalTime primeraEntrada) {
        this.primeraEntrada = primeraEntrada;
    }

    public LocalTime getUltimaSalida() {
        return ultimaSalida;
    }

    public void setUltimaSalida(LocalTime ultimaSalida) {
        this.ultimaSalida = ultimaSalida;
    }

    public Double getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(Double totalHoras) {
        this.totalHoras = totalHoras;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public boolean isFichajeIncompleto() {
        return fichajeIncompleto;
    }

    public void setFichajeIncompleto(boolean fichajeIncompleto) {
        this.fichajeIncompleto = fichajeIncompleto;
    }

    @Override
    public String toString() {
        return "FilaDiaDTO{" +
                "fecha=" + fecha +
                ", totalHoras=" + totalHoras +
                ", fichajeIncompleto=" + fichajeIncompleto +
                '}';
    }
}