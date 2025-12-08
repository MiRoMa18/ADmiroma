package org.example.model.dto.estadisticas;

public class EstadisticaSemanaDTO {

    private Integer numeroSemana;
    private String rangoFechas;
    private Integer diasTrabajados;
    private Double totalHoras;
    private Double promedio;

    public EstadisticaSemanaDTO() {
    }
    public EstadisticaSemanaDTO(Integer numeroSemana, String rangoFechas,
                                Integer diasTrabajados, Double totalHoras, Double promedio) {
        this.numeroSemana = numeroSemana;
        this.rangoFechas = rangoFechas;
        this.diasTrabajados = diasTrabajados;
        this.totalHoras = totalHoras;
        this.promedio = promedio;
    }

    public Integer getNumeroSemana() {
        return numeroSemana;
    }

    public void setNumeroSemana(Integer numeroSemana) {
        this.numeroSemana = numeroSemana;
    }

    public String getRangoFechas() {
        return rangoFechas;
    }

    public void setRangoFechas(String rangoFechas) {
        this.rangoFechas = rangoFechas;
    }

    public Integer getDiasTrabajados() {
        return diasTrabajados;
    }

    public void setDiasTrabajados(Integer diasTrabajados) {
        this.diasTrabajados = diasTrabajados;
    }

    public Double getTotalHoras() {
        return totalHoras;
    }

    public void setTotalHoras(Double totalHoras) {
        this.totalHoras = totalHoras;
    }

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    @Override
    public String toString() {
        return "EstadisticaSemanaDTO{" +
                "semana=" + numeroSemana +
                ", totalHoras=" + totalHoras +
                ", diasTrabajados=" + diasTrabajados +
                '}';
    }
}