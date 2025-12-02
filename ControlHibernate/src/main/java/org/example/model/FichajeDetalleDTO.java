package org.example.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para representar UNA línea de fichaje individual en la tabla detallada
 */
public class FichajeDetalleDTO {
    private LocalDate fecha;
    private LocalTime hora;
    private String tipo; // "ENTRADA" o "SALIDA"
    private String notas;
    private String clima;
    private Double horasSegmento; // Horas desde última entrada (si es SALIDA)
    private boolean esResumenDia; // true para fila de TOTAL DÍA

    public FichajeDetalleDTO() {
    }

    // Getters y Setters
    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getClima() {
        return clima;
    }

    public void setClima(String clima) {
        this.clima = clima;
    }

    public Double getHorasSegmento() {
        return horasSegmento;
    }

    public void setHorasSegmento(Double horasSegmento) {
        this.horasSegmento = horasSegmento;
    }

    public boolean isEsResumenDia() {
        return esResumenDia;
    }

    public void setEsResumenDia(boolean esResumenDia) {
        this.esResumenDia = esResumenDia;
    }

    @Override
    public String toString() {
        return "FichajeDetalleDTO{" +
                "fecha=" + fecha +
                ", hora=" + hora +
                ", tipo='" + tipo + '\'' +
                ", horasSegmento=" + horasSegmento +
                ", esResumenDia=" + esResumenDia +
                '}';
    }
}