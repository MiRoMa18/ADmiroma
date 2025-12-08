package org.example.model.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class FichajeDiaDTO {

    public static final int MAX_PARES_ENTRADA_SALIDA = 5;

    private Integer trabajadorId;  // ✨ NUEVO CAMPO
    private LocalDate fecha;

    private LocalTime entrada1;
    private LocalTime salida1;
    private LocalTime entrada2;
    private LocalTime salida2;
    private LocalTime entrada3;
    private LocalTime salida3;
    private LocalTime entrada4;
    private LocalTime salida4;
    private LocalTime entrada5;
    private LocalTime salida5;

    private String notas;
    private String clima;
    private Double horasTotales;
    private String estado;

    private String nombreEmpleado;
    private String numeroTarjeta;

    public FichajeDiaDTO() {
    }

    public FichajeDiaDTO(LocalDate fecha, Double horasTotales, String estado) {
        this.fecha = fecha;
        this.horasTotales = horasTotales;
        this.estado = estado;
    }

    public FichajeDiaDTO(LocalDate fecha, String nombreEmpleado, String numeroTarjeta,
                         Double horasTotales, String estado) {
        this.fecha = fecha;
        this.nombreEmpleado = nombreEmpleado;
        this.numeroTarjeta = numeroTarjeta;
        this.horasTotales = horasTotales;
        this.estado = estado;
    }

    // ✨ NUEVO: Getter y Setter para trabajadorId
    public Integer getTrabajadorId() {
        return trabajadorId;
    }

    public void setTrabajadorId(Integer trabajadorId) {
        this.trabajadorId = trabajadorId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getEntrada1() {
        return entrada1;
    }

    public void setEntrada1(LocalTime entrada1) {
        this.entrada1 = entrada1;
    }

    public LocalTime getSalida1() {
        return salida1;
    }

    public void setSalida1(LocalTime salida1) {
        this.salida1 = salida1;
    }

    public LocalTime getEntrada2() {
        return entrada2;
    }

    public void setEntrada2(LocalTime entrada2) {
        this.entrada2 = entrada2;
    }

    public LocalTime getSalida2() {
        return salida2;
    }

    public void setSalida2(LocalTime salida2) {
        this.salida2 = salida2;
    }

    public LocalTime getEntrada3() {
        return entrada3;
    }

    public void setEntrada3(LocalTime entrada3) {
        this.entrada3 = entrada3;
    }

    public LocalTime getSalida3() {
        return salida3;
    }

    public void setSalida3(LocalTime salida3) {
        this.salida3 = salida3;
    }

    public LocalTime getEntrada4() {
        return entrada4;
    }

    public void setEntrada4(LocalTime entrada4) {
        this.entrada4 = entrada4;
    }

    public LocalTime getSalida4() {
        return salida4;
    }

    public void setSalida4(LocalTime salida4) {
        this.salida4 = salida4;
    }

    public LocalTime getEntrada5() {
        return entrada5;
    }

    public void setEntrada5(LocalTime entrada5) {
        this.entrada5 = entrada5;
    }

    public LocalTime getSalida5() {
        return salida5;
    }

    public void setSalida5(LocalTime salida5) {
        this.salida5 = salida5;
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

    public Double getHorasTotales() {
        return horasTotales;
    }

    public void setHorasTotales(Double horasTotales) {
        this.horasTotales = horasTotales;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public void setNombreEmpleado(String nombreEmpleado) {
        this.nombreEmpleado = nombreEmpleado;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    @Override
    public String toString() {
        return "FichajeDiaDTO{" +
                "trabajadorId=" + trabajadorId +
                ", fecha=" + fecha +
                ", nombreEmpleado='" + nombreEmpleado + '\'' +
                ", numeroTarjeta='" + numeroTarjeta + '\'' +
                ", horasTotales=" + horasTotales +
                ", estado='" + estado + '\'' +
                '}';
    }
}