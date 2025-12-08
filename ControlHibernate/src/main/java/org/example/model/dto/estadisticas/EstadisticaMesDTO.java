package org.example.model.dto.estadisticas;

public class EstadisticaMesDTO {

    private String mes;
    private Integer anio;
    private Integer diasTrabajados;
    private Double totalHoras;
    private Double promedio;
    private Integer fichajesIncompletos;

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