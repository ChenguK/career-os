package com.chengukargbo.careeros.importing.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.DuplicateHeaderMode;
import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.HeaderMappingResult;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.ImportIssue;
import com.chengukargbo.careeros.importing.RawImportRow;

@Component
public class CsvImportParser {

    public static final int MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024;
    public static final int MAX_ROWS = 5_000;
    public static final int MAX_COLUMNS = 100;

    private final ImportHeaderMapper headerMapper;

    public CsvImportParser(ImportHeaderMapper headerMapper) {
        this.headerMapper = headerMapper;
    }

    public CsvParseResult parse(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            throw validation("CSV file is empty");
        }
        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            throw validation("CSV file exceeds the 2 MB limit");
        }

        String content = decodeUtf8(bytes);
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }
        if (content.isBlank()) {
            throw validation("CSV file is empty");
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL)
            .setIgnoreEmptyLines(true)
            .get();

        try (CSVParser parser = format.parse(new StringReader(content))) {
            List<String> headers = parser.getHeaderNames();
            validateHeaders(headers);

            HeaderMappingResult headerResult = validateMappedHeaders(headers);
            if (!headerResult.fields().containsKey("position_title")) {
                throw validation(
                    "CSV must include a position_title or Job Title header"
                );
            }

            List<RawImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (rows.size() >= MAX_ROWS) {
                    throw validation("CSV exceeds the 5000 row limit");
                }
                if (!record.isConsistent()) {
                    throw validation(
                        "CSV row " + (record.getRecordNumber() + 1)
                            + " has an unexpected number of columns"
                    );
                }

                Map<String, String> fields = new LinkedHashMap<>();
                for (int index = 0; index < headers.size(); index++) {
                    fields.put(headers.get(index), record.get(index));
                }
                rows.add(new RawImportRow(
                    Math.toIntExact(record.getRecordNumber() + 1), fields
                ));
            }

            if (rows.isEmpty()) {
                throw validation("CSV contains no data rows");
            }
            return new CsvParseResult(rows, headerResult.warnings());
        } catch (BusinessValidationException exception) {
            throw exception;
        } catch (
            IOException | IllegalArgumentException | UncheckedIOException exception
        ) {
            throw validation("CSV is malformed: " + safeMessage(exception));
        }
    }

    private void validateHeaders(List<String> headers) {
        if (headers.isEmpty()
            || (headers.size() == 1 && headers.getFirst().isBlank())) {
            throw validation("CSV is missing a header row");
        }
        if (headers.size() > MAX_COLUMNS) {
            throw validation("CSV exceeds the 100 column limit");
        }
        if (headers.stream().anyMatch(String::isBlank)) {
            throw validation("CSV contains a blank header");
        }
    }

    private HeaderMappingResult validateMappedHeaders(List<String> headers) {
        Map<String, String> mappedFields = new LinkedHashMap<>();
        List<ImportIssue> warnings = new ArrayList<>();
        Set<String> seenCanonicalFields = new HashSet<>();

        for (String header : headers) {
            HeaderMappingResult single = headerMapper.map(Map.of(header, ""));
            warnings.addAll(single.warnings());
            for (String canonical : single.fields().keySet()) {
                if (!seenCanonicalFields.add(canonical)) {
                    throw validation(
                        "Multiple CSV headers map to " + canonical
                    );
                }
                mappedFields.put(canonical, "");
            }
        }
        return new HeaderMappingResult(mappedFields, List.of(), warnings);
    }

    private String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        } catch (CharacterCodingException exception) {
            throw validation("CSV must be valid UTF-8 text");
        }
    }

    private String safeMessage(Exception exception) {
        return exception.getMessage() == null
            ? "invalid CSV structure"
            : exception.getMessage();
    }

    private BusinessValidationException validation(String message) {
        return new BusinessValidationException(message);
    }
}
