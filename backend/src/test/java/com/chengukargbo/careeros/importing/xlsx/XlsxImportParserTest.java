package com.chengukargbo.careeros.importing.xlsx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.csv.CsvParseResult;

class XlsxImportParserTest {

    private final XlsxImportParser parser = new XlsxImportParser(
        new ImportHeaderMapper()
    );

    @Test
    void parsesFirstVisibleSheetWithTypedCellsAndPhysicalRowNumbers() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet empty = workbook.createSheet("Empty");
            workbook.setSheetHidden(workbook.getSheetIndex(empty), true);
            Sheet sheet = workbook.createSheet("Jobs");
            Row header = sheet.createRow(1);
            header.createCell(0).setCellValue("Company");
            header.createCell(1).setCellValue("Job Title");
            header.createCell(2).setCellValue("Match Score");
            header.createCell(3).setCellValue("Cover Letter Needed");
            header.createCell(4).setCellValue("Date Posted");
            header.createCell(5).setCellValue("Notes");
            Row data = sheet.createRow(3);
            data.createCell(0).setCellValue("Café 東京");
            data.createCell(1).setCellValue("Engineer");
            data.createCell(2).setCellValue(88);
            data.createCell(3).setCellValue(true);
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
            data.createCell(4).setCellValue(LocalDate.of(2026, 8, 13));
            data.getCell(4).setCellStyle(dateStyle);
            data.createCell(5).setCellFormula("\"cached\"");
            workbook.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(data.getCell(5));

            CsvParseResult result = parser.parse(bytes(workbook));

            assertThat(result.rows()).hasSize(1);
            assertThat(result.rows().getFirst().rowNumber()).isEqualTo(4);
            assertThat(result.rows().getFirst().fields())
                .containsEntry("Company", "Café 東京")
                .containsEntry("Job Title", "Engineer")
                .containsEntry("Match Score", "88")
                .containsEntry("Cover Letter Needed", "true")
                .containsEntry("Date Posted", "2026-08-13")
                .containsEntry("Notes", "cached");
        }
    }

    @Test
    void warnsAndUsesFirstWhenMultipleVisibleSheetsContainData() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            addJob(workbook.createSheet("First"), "One");
            addJob(workbook.createSheet("Second"), "Two");

            CsvParseResult result = parser.parse(bytes(workbook));

            assertThat(result.rows().getFirst().fields()).containsEntry("Job Title", "One");
            assertThat(result.fileWarnings()).extracting(issue -> issue.message())
                .anyMatch(message -> message.contains("using First"));
        }
    }

    @Test
    void rejectsDuplicateCanonicalHeadersAndCorruptOrEmptyWorkbooks() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Job Title");
            header.createCell(1).setCellValue("position_title");
            Row data = sheet.createRow(1);
            data.createCell(0).setCellValue("Engineer");
            assertThatThrownBy(() -> parser.parse(bytes(workbook)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("map to position_title");
        }
        assertThatThrownBy(() -> parser.parse(new byte[0]))
            .hasMessageContaining("empty");
        assertThatThrownBy(() -> parser.parse("not a workbook".getBytes()))
            .hasMessageContaining("corrupted or unsupported");
    }

    @Test
    void enforcesColumnAndFileLimits() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            Row header = sheet.createRow(0);
            for (int index = 0; index <= XlsxImportParser.MAX_COLUMNS; index++) {
                header.createCell(index).setCellValue(index == 0 ? "Job Title" : "Unknown " + index);
            }
            assertThatThrownBy(() -> parser.parse(bytes(workbook)))
                .hasMessageContaining("100 column limit");
        }
        assertThatThrownBy(() -> parser.parse(
            new byte[XlsxImportParser.MAX_FILE_SIZE_BYTES + 1]
        )).hasMessageContaining("5 MB limit");
    }

    @Test
    void enforcesRowLimit() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet();
            sheet.createRow(0).createCell(0).setCellValue("Job Title");
            for (int index = 1; index <= XlsxImportParser.MAX_ROWS + 1; index++) {
                sheet.createRow(index).createCell(0).setCellValue("Job " + index);
            }
            assertThatThrownBy(() -> parser.parse(bytes(workbook)))
                .hasMessageContaining("5000 row limit");
        }
    }

    private void addJob(Sheet sheet, String title) {
        sheet.createRow(0).createCell(0).setCellValue("Job Title");
        sheet.createRow(1).createCell(0).setCellValue(title);
    }

    private byte[] bytes(XSSFWorkbook workbook) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        workbook.write(output);
        return output.toByteArray();
    }
}
