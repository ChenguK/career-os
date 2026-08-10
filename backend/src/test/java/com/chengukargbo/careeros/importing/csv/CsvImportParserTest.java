package com.chengukargbo.careeros.importing.csv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;

class CsvImportParserTest {

    private final CsvImportParser parser = new CsvImportParser(
        new ImportHeaderMapper()
    );

    @Test
    void parsesStandardCsvBomCrLfBlankCellsAndColumnOrder() {
        CsvParseResult result = parse(
            "\uFEFFCompany,Priority,Job Title,Location\r\n"
                + "Acme,,Engineer,Remote\r\n"
        );

        assertThat(result.rows()).singleElement().satisfies(row -> {
            assertThat(row.rowNumber()).isEqualTo(2);
            assertThat(row.fields()).containsEntry("Company", "Acme")
                .containsEntry("Priority", "")
                .containsEntry("Job Title", "Engineer");
        });
    }

    @Test
    void parsesQuotedCommasEscapedQuotesAndMultilineCellsWithLogicalRows() {
        CsvParseResult result = parse(
            "Job Title,Company,Job Notes\n"
                + "\"Engineer, Platform\",Acme,\"Said \"\"hello\"\"\nNext line\"\n"
                + "Designer,Other,Plain\n"
        );

        assertThat(result.rows()).hasSize(2);
        assertThat(result.rows().get(0).rowNumber()).isEqualTo(2);
        assertThat(result.rows().get(0).fields().get("Job Notes"))
            .isEqualTo("Said \"hello\"\nNext line");
        assertThat(result.rows().get(1).rowNumber()).isEqualTo(3);
    }

    @Test
    void preservesUnknownHeaderAsFileWarning() {
        CsvParseResult result = parse("Job Title,Mystery\nEngineer,value\n");
        assertThat(result.fileWarnings()).singleElement()
            .satisfies(issue -> assertThat(issue.field()).isEqualTo("Mystery"));
    }

    @Test
    void rejectsDuplicateMappedAndMissingRequiredHeaders() {
        assertThatThrownBy(() -> parse(
            "Company,Company Name,Job Title\nA,A,Engineer\n"
        )).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("company_name");
        assertThatThrownBy(() -> parse("Company,Location\nA,Remote\n"))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("position_title");
    }

    @Test
    void rejectsEmptyMissingHeaderMalformedQuotingAndMalformedRows() {
        assertThatThrownBy(() -> parser.parse(new byte[0]))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("empty");
        assertThatThrownBy(() -> parse("\n"))
            .isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> parse("Job Title,Notes\nEngineer,\"open\n"))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("malformed");
        assertThatThrownBy(() -> parse(
            "Job Title,Company\nEngineer,Acme,Extra\n"
        )).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("unexpected number");
    }

    @Test
    void enforcesFileRowAndColumnLimits() {
        assertThatThrownBy(() -> parser.parse(
            new byte[CsvImportParser.MAX_FILE_SIZE_BYTES + 1]
        )).isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("2 MB");

        StringBuilder tooManyRows = new StringBuilder("Job Title\n");
        for (int index = 0; index <= CsvImportParser.MAX_ROWS; index++) {
            tooManyRows.append("Engineer\n");
        }
        assertThatThrownBy(() -> parse(tooManyRows.toString()))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("5000 row");

        String headers = java.util.stream.IntStream
            .range(0, CsvImportParser.MAX_COLUMNS + 1)
            .mapToObj(index -> index == 0 ? "Job Title" : "Column " + index)
            .collect(java.util.stream.Collectors.joining(","));
        assertThatThrownBy(() -> parse(headers + "\nEngineer\n"))
            .isInstanceOf(BusinessValidationException.class)
            .hasMessageContaining("100 column");
    }

    private CsvParseResult parse(String content) {
        return parser.parse(content.getBytes(StandardCharsets.UTF_8));
    }
}
