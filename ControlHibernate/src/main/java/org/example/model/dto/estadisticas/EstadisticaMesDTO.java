package org.example.model.dto.estadisticas;

/**
 * DTO para estadísticas de un mes en la vista de trabajador.
 */
public class EstadisticaMesDTO {

    private String mes;             // Nombre del mes (Ej: "Enero")
    private Integer anio;           // Año
    private Integer diasTrabajados; // Días trabajados en el mes
    private Double totalHoras;      // Total de horas trabajadas
    private Double promedio;        // Promedio diario
    private Integer fichajesIncompletos; // Días con fichaje incompleto

    // Constructores
    public EstadisticaMesDTO() {
    }

    public EstadisticaMesDTO(String mes, Integer anio, Integer diasTrabajados,
                             Double totalHoras, Double promedio, Integer fichajesIncompletos) {
        this.mes = mes;
        this.anio = anio;
        this.diasTrabajados = diasTrabajados;
        this.totalHoras = totalHoras;
        this.promedio = promedio;
        this.fichajesIncompletos = fichajesIncompletos;
    }

    // Getters y Setters
    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
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

    public Integer getFichajesIncompletos() {
        return fichajesIncompletos;
    }

    public void setFichajesIncompletos(Integer fichajesIncompletos) {
        this.fichajesIncompletos = fichajesIncompletos;
    }

    @Override
    public String toString() {
        return "EstadisticaMesDTO{" +
                "mes='" + mes + '\'' +
                ", anio=" + anio +
                ", totalHoras=" + totalHoras +
                '}';
    }
}