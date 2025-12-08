package org.example.model.dto.estadisticas;

/**
 * DTO para estadísticas de una semana en la vista de trabajador.
 */
public class EstadisticaSemanaDTO {

    private Integer numeroSemana;   // Número de semana del año (1-52)
    private String rangoFechas;     // Ej: "01/01 - 07/01"
    private Integer diasTrabajados; // Días trabajados en la semana
    private Double totalHoras;      // Total de horas trabajadas
    private Double promedio;        // Promedio diario

    // Constructores
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

    // Getters y Setters
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