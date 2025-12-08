package org.example.model.dto.estadisticas;

public class EstadisticaDiaDTO {

    private String fecha;
    private String diaSemana;
    private Double horas;
    private String estado;

    public EstadisticaDiaDTO() {
    }
    public EstadisticaDiaDTO(String fecha, String diaSemana, Double horas, String estado) {
        this.fecha = fecha;
        this.diaSemana = diaSemana;
        this.horas = horas;
        this.estado = estado;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public Double getHoras() {
        return horas;
    }

    public void setHoras(Double horas) {
        this.horas = horas;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "EstadisticaDiaDTO{" +
                "fecha='" + fecha + '\'' +
                ", horas=" + horas +
                ", estado='" + estado + '\'' +
                '}';
    }
}