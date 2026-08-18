package com.chengukargbo.careeros.importing.xlsx;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.HeaderMappingResult;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.ImportIssue;
import com.chengukargbo.careeros.importing.RawImportRow;
import com.chengukargbo.careeros.importing.csv.CsvParseResult;

@Component
public class XlsxImportParser {

    public static final int MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    public static final int MAX_ROWS = 5_000;
    public static final int MAX_COLUMNS = 100;
    public static final int MAX_SHEETS = 20;

    private final ImportHeaderMapper headerMapper;

    public XlsxImportParser(ImportHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    public CsvParseResult parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw validation("XLSX file is empty");
        }
        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw validation("XLSX file exceeds the 5 MB limit");
        }

        try (Workbook workbook = WorkbookFactory.create(
            new ByteArrayInputStream(bytes)
        )) {
            if (workbook.getNumberOfSheets() > MAX_SHEETS) {
                throw validation("XLSX workbook exceeds the 20 sheet limit");
            }
            Sheet sheet = firstVisibleDataSheet(workbook);
            if (sheet == null) {
                throw validation("XLSX workbook contains no visible data sheet");
            }
            int headerIndex = firstNonEmptyRow(sheet);
            Row headerRow = sheet.getRow(headerIndex);
            int columns = lastMeaningfulCell(headerRow);
            if (columns == 0) {
                throw validation("XLSX sheet is missing a header row");
            }
            if (columns > MAX_COLUMNS) {
                throw validation("XLSX exceeds the 100 column limit");
            }

            List<String> headers = new ArrayList<>();
            for (int column = 0; column < columns; column++) {
                String header = cellText(headerRow.getCell(column));
                if (header.isBlank()) {
                    throw validation("XLSX contains a blank header");
                }
                headers.add(header);
            }
            HeaderMappingResult mapped = validateHeaders(headers);
            if (!mapped.fields().containsKey("position_title")) {
                throw validation(
                    "XLSX must include a position_title or Job Title header"
                );
            }

            List<RawImportRow> rows = new ArrayList<>();
            for (int index = headerIndex + 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null || isEmpty(row)) {
                    continue;
                }
                if (rows.size() >= MAX_ROWS) {
                    throw validation("XLSX exceeds the 5000 row limit");
                }
                if (lastMeaningfulCell(row) > columns) {
                    throw validation(
                        "XLSX row " + (index + 1) + " has unexpected extra columns"
                    );
                }
                Map<String, String> fields = new LinkedHashMap<>();
                for (int column = 0; column < columns; column++) {
                    fields.put(headers.get(column), cellText(row.getCell(column)));
                }
                rows.add(new RawImportRow(index + 1, fields));
            }
            if (rows.isEmpty()) {
                throw validation("XLSX sheet contains no data rows");
            }

            List<ImportIssue> warnings = new ArrayList<>(mapped.warnings());
            long populatedSheets = visibleDataSheetCount(workbook);
            if (populatedSheets > 1) {
                warnings.add(new ImportIssue(
                    "worksheet",
                    "Multiple populated worksheets found; using " + sheet.getSheetName()
                ));
            }
            return new CsvParseResult(rows, warnings);
        } catch (BusinessValidationException exception) {
            throw exception;
        } catch (EncryptedDocumentException exception) {
            throw validation("Encrypted XLSX workbooks are not supported");
        } catch (IOException | RuntimeException exception) {
            throw validation("XLSX workbook is corrupted or unsupported");
        }
    }

    private Sheet firstVisibleDataSheet(Workbook workbook) {
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            if (!workbook.isSheetHidden(index)
                && !workbook.isSheetVeryHidden(index)
                && firstNonEmptyRow(workbook.getSheetAt(index)) >= 0) {
                return workbook.getSheetAt(index);
            }
        }
        return null;
    }

    private long visibleDataSheetCount(Workbook workbook) {
        long count = 0;
        for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
            if (!workbook.isSheetHidden(index)
                && !workbook.isSheetVeryHidden(index)
                && firstNonEmptyRow(workbook.getSheetAt(index)) >= 0) {
                count++;
            }
        }
        return count;
    }

    private int firstNonEmptyRow(Sheet sheet) {
        for (int index = sheet.getFirstRowNum(); index <= sheet.getLastRowNum(); index++) {
            Row row = sheet.getRow(index);
            if (row != null && !isEmpty(row)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isEmpty(Row row) {
        return lastMeaningfulCell(row) == 0;
    }

    private int lastMeaningfulCell(Row row) {
        if (row == null) {
            return 0;
        }
        for (int index = Math.max(row.getLastCellNum(), 0) - 1; index >= 0; index--) {
            if (!cellText(row.getCell(index)).isBlank()) {
                return index + 1;
            }
        }
        return 0;
    }

    private HeaderMappingResult validateHeaders(List<String> headers) {
        Map<String, String> fields = new LinkedHashMap<>();
        List<ImportIssue> warnings = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String header : headers) {
            HeaderMappingResult result = headerMapper.map(Map.of(header, ""));
            warnings.addAll(result.warnings());
            for (String canonical : result.fields().keySet()) {
                if (!seen.add(canonical)) {
                    throw validation("Multiple XLSX headers map to " + canonical);
                }
                fields.put(canonical, "");
            }
        }
        return new HeaderMappingResult(fields, List.of(), warnings);
    }

    private String cellText(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA
            ? cell.getCachedFormulaResultType()
            : cell.getCellType();
        return switch (type) {
            case STRING -> cell.getStringCellValue();
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC -> numericText(cell);
            case ERROR -> throw validation(
                "XLSX contains a formula or cell error at " + cell.getAddress()
            );
            case BLANK, _NONE -> "";
            default -> "";
        };
    }

    private String numericText(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
            return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        return BigDecimal.valueOf(cell.getNumericCellValue())
            .stripTrailingZeros()
            .toPlainString();
    }

    private BusinessValidationException validation(String message) {
        return new BusinessValidationException(message);
    }
}
