package org.example.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.dto.FichajeDiaDTO;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExportador {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private ExcelExportador() {
        throw new UnsupportedOperationException("Clase de utilidad");
    }
    public static boolean exportar(
            List<FichajeDiaDTO> fichajes,
            File archivo,
            String nombreEmpleado,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Fichajes");

            // Estilos
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloNormal = crearEstiloNormal(workbook);
            CellStyle estiloHora = crearEstiloHora(workbook);
            CellStyle estiloNumero = crearEstiloNumero(workbook);
            CellStyle estiloTotal = crearEstiloTotal(workbook);

            int filaActual = 0;

            // ============ TÍTULO ============
            Row filaTitulo = hoja.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("REGISTRO DE FICHAJES");
            celdaTitulo.setCellStyle(estiloTitulo);

            // ============ INFORMACIÓN DEL PERIODO ============
            filaActual++;

            Row filaPeriodo = hoja.createRow(filaActual++);
            Cell celdaPeriodo = filaPeriodo.createCell(0);
            celdaPeriodo.setCellValue("Periodo:");
            celdaPeriodo.setCellStyle(estiloNormal);

            Cell celdaPeriodoValor = filaPeriodo.createCell(1);
            String periodo = fechaInicio.format(FORMATO_FECHA) + " - " + fechaFin.format(FORMATO_FECHA);
            celdaPeriodoValor.setCellValue(periodo);
            celdaPeriodoValor.setCellStyle(estiloNormal);

            if (nombreEmpleado != null) {
                Row filaEmpleado = hoja.createRow(filaActual++);
                Cell celdaEmpleado = filaEmpleado.createCell(0);
                celdaEmpleado.setCellValue("Empleado:");
                celdaEmpleado.setCellStyle(estiloNormal);

                Cell celdaEmpleadoValor = filaEmpleado.createCell(1);
                celdaEmpleadoValor.setCellValue(nombreEmpleado);
                celdaEmpleadoValor.setCellStyle(estiloNormal);
            }

            Row filaFecha = hoja.createRow(filaActual++);
            Cell celdaFechaGen = filaFecha.createCell(0);
            celdaFechaGen.setCellValue("Fecha de generación:");
            celdaFechaGen.setCellStyle(estiloNormal);

            Cell celdaFechaGenValor = filaFecha.createCell(1);
            celdaFechaGenValor.setCellValue(LocalDate.now().format(FORMATO_FECHA));
            celdaFechaGenValor.setCellStyle(estiloNormal);

            filaActual++; // Espacio en blanco

            // ============ ENCABEZADOS DE LA TABLA ============
            Row filaEncabezado = hoja.createRow(filaActual++);
            int columna = 0;

            if (nombreEmpleado == null) {
                crearCeldaEncabezado(filaEncabezado, columna++, "Empleado", estiloEncabezado);
                crearCeldaEncabezado(filaEncabezado, columna++, "Tarjeta", estiloEncabezado);
            }

            crearCeldaEncabezado(filaEncabezado, columna++, "Fecha", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Entrada 1", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Salida 1", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Entrada 2", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Salida 2", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Entrada 3", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Salida 3", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Horas Totales", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Estado", estiloEncabezado);
            crearCeldaEncabezado(filaEncabezado, columna++, "Notas", estiloEncabezado);

            // ============ DATOS ============
            double totalHoras = 0.0;
            int diasTrabajados = 0;

            for (FichajeDiaDTO fichaje : fichajes) {
                Row filaDatos = hoja.createRow(filaActual++);
                columna = 0;

                if (nombreEmpleado == null) {
                    crearCeldaNormal(filaDatos, columna++, fichaje.getNombreEmpleado(), estiloNormal);
                    crearCeldaNormal(filaDatos, columna++, fichaje.getNumeroTarjeta(), estiloNormal);
                }

                crearCeldaNormal(filaDatos, columna++, fichaje.getFecha().format(FORMATO_FECHA), estiloNormal);
                crearCeldaHora(filaDatos, columna++, fichaje.getEntrada1(), estiloHora);
                crearCeldaHora(filaDatos, columna++, fichaje.getSalida1(), estiloHora);
                crearCeldaHora(filaDatos, columna++, fichaje.getEntrada2(), estiloHora);
                crearCeldaHora(filaDatos, columna++, fichaje.getSalida2(), estiloHora);
                crearCeldaHora(filaDatos, columna++, fichaje.getEntrada3(), estiloHora);
                crearCeldaHora(filaDatos, columna++, fichaje.getSalida3(), estiloHora);
                crearCeldaNumero(filaDatos, columna++, fichaje.getHorasTotales(), estiloNumero);
                crearCeldaNormal(filaDatos, columna++, fichaje.getEstado(), estiloNormal);
                crearCeldaNormal(filaDatos, columna++, fichaje.getNotas(), estiloNormal);

                if (fichaje.getHorasTotales() != null) {
                    totalHoras += fichaje.getHorasTotales();
                    diasTrabajados++;
                }
            }

            // ============ TOTALES ============
            filaActual++; // Espacio en blanco

            Row filaTotal = hoja.createRow(filaActual++);
            int colTotal = (nombreEmpleado == null) ? 9 : 7;

            Cell celdaTotalLabel = filaTotal.createCell(colTotal);
            celdaTotalLabel.setCellValue("TOTAL HORAS:");
            celdaTotalLabel.setCellStyle(estiloTotal);

            Cell celdaTotalValor = filaTotal.createCell(colTotal + 1);
            celdaTotalValor.setCellValue(HorasFormateador.formatearHoras(totalHoras));
            celdaTotalValor.setCellStyle(estiloTotal);

            Row filaDias = hoja.createRow(filaActual++);
            Cell celdaDiasLabel = filaDias.createCell(colTotal);
            celdaDiasLabel.setCellValue("DÍAS TRABAJADOS:");
            celdaDiasLabel.setCellStyle(estiloTotal);

            Cell celdaDiasValor = filaDias.createCell(colTotal + 1);
            celdaDiasValor.setCellValue(diasTrabajados);
            celdaDiasValor.setCellStyle(estiloTotal);

            if (diasTrabajados > 0) {
                double promedio = totalHoras / diasTrabajados;
                Row filaPromedio = hoja.createRow(filaActual++);
                Cell celdaPromedioLabel = filaPromedio.createCell(colTotal);
                celdaPromedioLabel.setCellValue("PROMEDIO DIARIO:");
                celdaPromedioLabel.setCellStyle(estiloTotal);

                Cell celdaPromedioValor = filaPromedio.createCell(colTotal + 1);
                celdaPromedioValor.setCellValue(HorasFormateador.formatearHoras(promedio));
                celdaPromedioValor.setCellStyle(estiloTotal);
            }

            // ============ AJUSTAR ANCHOS DE COLUMNA ============
            for (int i = 0; i < columna; i++) {
                hoja.autoSizeColumn(i);
            }

            // ============ GUARDAR ARCHIVO ============
            try (FileOutputStream fos = new FileOutputStream(archivo)) {
                workbook.write(fos);
            }

            return true;
        } catch (Exception e) {
            System.err.println("   💥 ERROR al generar Excel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ============ MÉTODOS AUXILIARES PARA CREAR CELDAS ============
    private static void crearCeldaEncabezado(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private static void crearCeldaNormal(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor != null && !valor.isEmpty()) {
            celda.setCellValue(valor);
        }
        celda.setCellStyle(estilo);
    }

    private static void crearCeldaHora(Row fila, int columna, java.time.LocalTime hora, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (hora != null) {
            celda.setCellValue(hora.format(FORMATO_HORA));
        }
        celda.setCellStyle(estilo);
    }

    private static void crearCeldaNumero(Row fila, int columna, Double valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        if (valor != null) {
            celda.setCellValue(HorasFormateador.formatearHoras(valor));
        }
        celda.setCellStyle(estilo);
    }

    // ============ ESTILOS ============
    private static CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private static CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 11);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }

    private static CellStyle crearEstiloNormal(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    private static CellStyle crearEstiloHora(Workbook workbook) {
        CellStyle estilo = crearEstiloNormal(workbook);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private static CellStyle crearEstiloNumero(Workbook workbook) {
        CellStyle estilo = crearEstiloNormal(workbook);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }

    private static CellStyle crearEstiloTotal(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 11);
        estilo.setFont(fuente);
        estilo.setBorderTop(BorderStyle.DOUBLE);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }
}