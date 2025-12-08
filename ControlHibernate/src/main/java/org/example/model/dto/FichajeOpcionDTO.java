package org.example.model.dto;

public class FichajeOpcionDTO {

    private Integer fichajeId;
    private String descripcion;

    public FichajeOpcionDTO() {
    }

    public FichajeOpcionDTO(Integer fichajeId, String descripcion) {
        this.fichajeId = fichajeId;
        this.descripcion = descripcion;
    }

    public Integer getFichajeId() {
        return fichajeId;
    }

    public void setFichajeId(Integer fichajeId) {
        this.fichajeId = fichajeId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}