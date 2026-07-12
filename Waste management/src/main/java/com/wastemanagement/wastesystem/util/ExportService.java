package com.wastemanagement.wastesystem.util;

import com.wastemanagement.wastesystem.exception.BadRequestException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Generic CSV and Excel (.xlsx) export utility — backs the "Export CSV" /
 * "Excel Export" features from the master spec for any listing screen
 * (ManageCitizens.js, ManageWorkers.js, ComplaintsView.js,
 * CollectionRecords reports, etc.).
 *
 * Rather than writing a dedicated exportCitizensToCsv(),
 * exportComplaintsToExcel(), and so on for every entity — which would
 * mean N export methods for N entity types, all structurally identical —
 * this class accepts a caller-supplied column definition (an ordered
 * LinkedHashMap of column header -> a function extracting that column's
 * value from one row object) and a list of row objects of any type T.
 * Each controller endpoint that wants an export simply describes its own
 * columns inline and calls one of the two methods below.
 */
@Component
public class ExportService {

    /**
     * Exports a list of objects to CSV bytes, ready to stream as
     * text/csv. columnExtractors is an ordered map: column header ->
     * function that extracts that column's display value from a single
     * row object. LinkedHashMap is required (not just Map) specifically
     * because column ORDER matters for a spreadsheet export — a caller
     * passing a HashMap would produce columns in unpredictable order.
     */
    public <T> byte[] exportToCsv(List<T> rows, LinkedHashMap<String, Function<T, Object>> columnExtractors) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder()
                     .setHeader(columnExtractors.keySet().toArray(new String[0]))
                     .build())) {

            for (T row : rows) {
                List<Object> values = columnExtractors.values().stream()
                        .map(extractor -> extractor.apply(row))
                        .map(this::nullSafe)
                        .toList();
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
        } catch (IOException ex) {
            throw new BadRequestException("Failed to generate CSV export");
        }

        return outputStream.toByteArray();
    }

    /**
     * Exports a list of objects to an .xlsx workbook's bytes, ready to
     * stream as application/vnd.openxmlformats-officedocument
     * .spreadsheetml.sheet. Same columnExtractors contract as
     * exportToCsv, so a caller can offer both formats for the same data
     * with zero duplicated column-definition logic.
     */
    public <T> byte[] exportToExcel(List<T> rows, LinkedHashMap<String, Function<T, Object>> columnExtractors,
                                    String sheetName) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            XSSFSheet sheet = workbook.createSheet(sheetName);
            CellStyle headerStyle = buildHeaderStyle(workbook);

            Row headerRow = sheet.createRow(0);
            int columnIndex = 0;
            for (String header : columnExtractors.keySet()) {
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue(header);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (T row : rows) {
                Row dataRow = sheet.createRow(rowIndex++);
                columnIndex = 0;
                for (Function<T, Object> extractor : columnExtractors.values()) {
                    Cell cell = dataRow.createCell(columnIndex++);
                    cell.setCellValue(String.valueOf(nullSafe(extractor.apply(row))));
                }
            }

            for (int i = 0; i < columnExtractors.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new BadRequestException("Failed to generate Excel export");
        }
    }

    private Object nullSafe(Object value) {
        return value != null ? value : "";
    }

    private CellStyle buildHeaderStyle(XSSFWorkbook workbook) {
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);

        CellStyle style = workbook.createCellStyle();
        style.setFont(boldFont);
        return style;
    }
}