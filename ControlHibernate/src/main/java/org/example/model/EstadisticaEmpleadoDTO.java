package org.example.model;

/**
 * DTO para estadísticas de un empleado en el panel de administrador
 */
public class EstadisticaEmpleadoDTO {
    private Integer empleadoId;
    private String nombreCompleto;
    private String numeroTarjeta;
    private Integer diasTrabajados;
    private Double totalHoras;
    private Double promedioDiario;
    private Integer fichajesIncompletos;
    private String estado; // "✅ Normal", "⚠️ Bajo", "❌ Inactivo"

    public EstadisticaEmpleadoDTO() {
    }

    public EstadisticaEmpleadoDTO(Integer empleadoId, String nombreCompleto, String numeroTarjeta,
                                  Integer diasTrabajados, Double totalHoras, Double promedioDiario,
                                  Integer fichajesIncompletos, String estado) {
        this.empleadoId = empleadoId;
        this.nombreCompleto = nombreCompleto;
        this.numeroTarjeta = numeroTarjeta;
        this.diasTrabajados = diasTrabajados;
        this.totalHoras = totalHoras;
        this.promedioDiario = promedioDiario;
        this.fichajesIncompletos = fichajesIncompletos;
        this.estado = estado;
    }

    // Getters y Setters
    public Integer getEmpleadoId() {
        return empleadoId;
    }

    public void setEmpleadoId(Integer empleadoId) {
        this.empleadoId = empleadoId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
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

    public Double getPromedioDiario() {
        return promedioDiario;
    }

    public void setPromedioDiario(Double promedioDiario) {
        this.promedioDiario = promedioDiario;
    }

    public Integer getFichajesIncompletos() {
        return fichajesIncompletos;
    }

    public void setFichajesIncompletos(Integer fichajesIncompletos) {
        this.fichajesIncompletos = fichajesIncompletos;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "EstadisticaEmpleadoDTO{" +
                "nombreCompleto='" + nombreCompleto + '\'' +
                ", totalHoras=" + totalHoras +
                ", diasTrabajados=" + diasTrabajados +
                '}';
    }
}