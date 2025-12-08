package org.example.model.dto;

/**
 * DTO para mostrar opciones de fichajes en diálogos de selección.
 * Usado cuando hay múltiples fichajes en un mismo día y el usuario
 * debe elegir cuál editar/eliminar.
 */
public class FichajeOpcionDTO {

    private Integer fichajeId;
    private String descripcion; // Ej: "08:30 - ENTRADA (Soleado)"

    // Constructores
    public FichajeOpcionDTO() {
    }

    public FichajeOpcionDTO(Integer fichajeId, String descripcion) {
        this.fichajeId = fichajeId;
        this.descripcion = descripcion;
    }

    // Getters y Setters
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
        return descripcion; // Para mostrar en ComboBox
    }
}