package org.example.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.model.dto.FichajeDiaDTO;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PDFExportador {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DeviceRgb COLOR_AZUL_OSCURO = new DeviceRgb(25, 25, 112);

    private PDFExportador() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }
    public static boolean exportar(
            List<FichajeDiaDTO> fichajes,
            File archivo,
            String nombreEmpleado,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        try {
            // Crear documento PDF
            PdfWriter writer = new PdfWriter(archivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            // ============ TÍTULO ============
            Paragraph titulo = new Paragraph("REGISTRO DE FICHAJES")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20);
            documento.add(titulo);

            // ============ INFORMACIÓN DEL PERIODO ============
            String periodo = fechaInicio.format(FORMATO_FECHA) + " - " + fechaFin.format(FORMATO_FECHA);
            documento.add(new Paragraph("Periodo: " + periodo).setFontSize(12).setBold());

            if (nombreEmpleado != null) {
                documento.add(new Paragraph("Empleado: " + nombreEmpleado).setFontSize(12).setBold());
            }

            documento.add(new Paragraph("Fecha de generación: " + LocalDate.now().format(FORMATO_FECHA))
                    .setFontSize(10)
                    .setMarginBottom(20));

            // ============ TABLA DE DATOS ============
            float[] anchoColumnas;
            if (nombreEmpleado == null) { // Vista ADMIN
                anchoColumnas = new float[]{80, 60, 60, 50, 50, 50, 50, 50, 50, 60, 60, 100};
            } else { // Vista TRABAJADOR
                anchoColumnas = new float[]{80, 50, 50, 50, 50, 50, 50, 60, 60, 120};
            }

            Table tabla = new Table(UnitValue.createPercentArray(anchoColumnas));
            tabla.setWidth(UnitValue.createPercentValue(100));

            // ENCABEZADOS
            if (nombreEmpleado == null) { // Vista ADMIN
                agregarCeldaEncabezado(tabla, "Empleado");
                agregarCeldaEncabezado(tabla, "Tarjeta");
            }

            agregarCeldaEncabezado(tabla, "Fecha");
            agregarCeldaEncabezado(tabla, "Entrada 1");
            agregarCeldaEncabezado(tabla, "Salida 1");
            agregarCeldaEncabezado(tabla, "Entrada 2");
            agregarCeldaEncabezado(tabla, "Salida 2");
            agregarCeldaEncabezado(tabla, "Entrada 3");
            agregarCeldaEncabezado(tabla, "Salida 3");
            agregarCeldaEncabezado(tabla, "Horas Totales");
            agregarCeldaEncabezado(tabla, "Estado");
            agregarCeldaEncabezado(tabla, "Notas");

            // DATOS
            double totalHoras = 0.0;
            int diasTrabajados = 0;

            for (FichajeDiaDTO fichaje : fichajes) {
                if (nombreEmpleado == null) {
                    agregarCeldaDato(tabla, fichaje.getNombreEmpleado());
                    agregarCeldaDato(tabla, fichaje.getNumeroTarjeta());
                }

                agregarCeldaDato(tabla, fichaje.getFecha().format(FORMATO_FECHA));
                agregarCeldaHora(tabla, fichaje.getEntrada1());
                agregarCeldaHora(tabla, fichaje.getSalida1());
                agregarCeldaHora(tabla, fichaje.getEntrada2());
                agregarCeldaHora(tabla, fichaje.getSalida2());
                agregarCeldaHora(tabla, fichaje.getEntrada3());
                agregarCeldaHora(tabla, fichaje.getSalida3());
                agregarCeldaDato(tabla, fichaje.getHorasTotales() != null ?
                        HorasFormateador.formatearHoras(fichaje.getHorasTotales()) : "-");
                agregarCeldaDato(tabla, fichaje.getEstado());
                agregarCeldaDato(tabla, fichaje.getNotas() != null ? fichaje.getNotas() : "-");

                if (fichaje.getHorasTotales() != null) {
                    totalHoras += fichaje.getHorasTotales();
                    diasTrabajados++;
                }
            }

            documento.add(tabla);

            // ============ RESUMEN ============
            documento.add(new Paragraph("\n"));

            Table tablaResumen = new Table(2);
            tablaResumen.setWidth(UnitValue.createPercentValue(40));
            tablaResumen.setMarginTop(20);

            agregarCeldaTotal(tablaResumen, "TOTAL HORAS:");
            agregarCeldaTotal(tablaResumen, HorasFormateador.formatearHoras(totalHoras));

            agregarCeldaTotal(tablaResumen, "DÍAS TRABAJADOS:");
            agregarCeldaTotal(tablaResumen, String.valueOf(diasTrabajados));

            if (diasTrabajados > 0) {
                double promedio = totalHoras / diasTrabajados;
                agregarCeldaTotal(tablaResumen, "PROMEDIO DIARIO:");
                agregarCeldaTotal(tablaResumen, HorasFormateador.formatearHoras(promedio));
            }

            documento.add(tablaResumen);

            // ============ PIE DE PÁGINA ============
            Paragraph footer = new Paragraph("\n\nDocumento generado automáticamente por ControlHibernate")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            documento.add(footer);

            // Cerrar documento
            documento.close();
            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR al generar PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ============ MÉTODOS AUXILIARES ============

    private static void agregarCeldaEncabezado(Table tabla, String texto) {
        Cell celda = new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(9))
                .setBackgroundColor(COLOR_AZUL_OSCURO)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        tabla.addCell(celda);
    }

    private static void agregarCeldaDato(Table tabla, String texto) {
        Cell celda = new Cell()
                .add(new Paragraph(texto != null ? texto : "-").setFontSize(8))
                .setTextAlignment(TextAlignment.LEFT)
                .setPadding(3);
        tabla.addCell(celda);
    }

    private static void agregarCeldaHora(Table tabla, java.time.LocalTime hora) {
        String texto = hora != null ? hora.format(FORMATO_HORA) : "-";
        Cell celda = new Cell()
                .add(new Paragraph(texto).setFontSize(8))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(3);
        tabla.addCell(celda);
    }

    private static void agregarCeldaTotal(Table tabla, String texto) {
        Cell celda = new Cell()
                .add(new Paragraph(texto).setBold().setFontSize(10))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
        tabla.addCell(celda);
    }
}