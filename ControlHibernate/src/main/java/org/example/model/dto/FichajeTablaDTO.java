//package org.example.model.dto;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//
///**
// * DTO para mostrar fichajes en la tabla del CRUD de administrador
// */
//public class FichajeTablaDTO {
//    private Integer id;
//    private String nombreEmpleado;
//    private String numeroTarjeta;
//    private LocalDate fecha;
//    private LocalTime hora;
//    private String tipo;
//    private String clima;
//    private String notas;
//
//    // Constructor vacío
//    public FichajeTablaDTO() {
//    }
//
//    // Constructor completo
//    public FichajeTablaDTO(Integer id, String nombreEmpleado, String numeroTarjeta,
//                           LocalDate fecha, LocalTime hora, String tipo,
//                           String clima, String notas) {
//        this.id = id;
//        this.nombreEmpleado = nombreEmpleado;
//        this.numeroTarjeta = numeroTarjeta;
//        this.fecha = fecha;
//        this.hora = hora;
//        this.tipo = tipo;
//        this.clima = clima;
//        this.notas = notas;
//    }
//
//    // Getters y Setters
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getNombreEmpleado() {
//        return nombreEmpleado;
//    }
//
//    public void setNombreEmpleado(String nombreEmpleado) {
//        this.nombreEmpleado = nombreEmpleado;
//    }
//
//    public String getNumeroTarjeta() {
//        return numeroTarjeta;
//    }
//
//    public void setNumeroTarjeta(String numeroTarjeta) {
//        this.numeroTarjeta = numeroTarjeta;
//    }
//
//    public LocalDate getFecha() {
//        return fecha;
//    }
//
//    public void setFecha(LocalDate fecha) {
//        this.fecha = fecha;
//    }
//
//    public LocalTime getHora() {
//        return hora;
//    }
//
//    public void setHora(LocalTime hora) {
//        this.hora = hora;
//    }
//
//    public String getTipo() {
//        return tipo;
//    }
//
//    public void setTipo(String tipo) {
//        this.tipo = tipo;
//    }
//
//    public String getClima() {
//        return clima;
//    }
//
//    public void setClima(String clima) {
//        this.clima = clima;
//    }
//
//    public String getNotas() {
//        return notas;
//    }
//
//    public void setNotas(String notas) {
//        this.notas = notas;
//    }
//
//    @Override
//    public String toString() {
//        return "FichajeTablaDTO{" +
//                "id=" + id +
//                ", nombreEmpleado='" + nombreEmpleado + '\'' +
//                ", fecha=" + fecha +
//                ", tipo='" + tipo + '\'' +
//                '}';
//    }
//}