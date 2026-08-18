package com.chengukargbo.careeros.applications.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;
import com.chengukargbo.careeros.jobs.RemoteType;
import com.chengukargbo.careeros.importing.ImportHeaderMapper;
import com.chengukargbo.careeros.importing.xlsx.XlsxImportParser;

class ApplicationTrackerXlsxSerializerTest {

    private final ApplicationTrackerXlsxSerializer serializer =
        new ApplicationTrackerXlsxSerializer();

    @Test
    void writesCanonicalFormattedTypedAndFormulaSafeWorkbook() throws Exception {
        byte[] content = serializer.serialize(List.of(row()));

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheet("Applications");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getPaneInformation().isFreezePane()).isTrue();
            assertThat(sheet.getCTWorksheet().isSetAutoFilter()).isTrue();
            assertThat(sheet.getRow(0).getLastCellNum())
                .isEqualTo((short) ApplicationTrackerCsvSerializer.HEADERS.length);
            Row data = sheet.getRow(1);
            assertThat(data.getCell(2).getCellType()).isEqualTo(CellType.STRING);
            assertThat(data.getCell(2).getStringCellValue()).isEqualTo("=Unsafe Co");
            assertThat(data.getCell(8).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(data.getCell(8).getNumericCellValue()).isEqualTo(100000d);
            assertThat(data.getCell(15).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(DateUtil.isCellDateFormatted(data.getCell(15))).isTrue();
            assertThat(data.getCell(25).getCellType()).isEqualTo(CellType.BOOLEAN);
            assertThat(data.getCell(19).getStringCellValue()).isEqualTo("@job note");
            assertThat(data.getCell(40).getStringCellValue()).isEqualTo("+application note");
            assertThat(data.getCell(20).getStringCellValue()).isEqualTo("2026-08-01T10:00:00Z");
        }
    }

    @Test
    void writesSavedAndBlankApplicationCellsForJobOnlyRows() throws Exception {
        ApplicationTrackerResponse source = row();
        ApplicationTrackerResponse jobOnly = new ApplicationTrackerResponse(
            source.jobOpportunityId(), null, null, source.positionTitle(), null,
            null, null, null, null, null, null, null, null, null, null, null,
            (short) 0, null, null, null, source.jobCreatedAt(), source.jobUpdatedAt(),
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null, null
        );
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(
            serializer.serialize(List.of(jobOnly))
        ))) {
            Row data = workbook.getSheet("Applications").getRow(1);
            assertThat(data.getCell(22)).isNull();
            assertThat(data.getCell(23).getStringCellValue()).isEqualTo("SAVED");
            assertThat(data.getCell(40)).isNull();
        }
    }

    @Test
    void exportedWorkbookRoundTripsThroughTheCanonicalHeaderAdapter() {
        byte[] content = serializer.serialize(List.of(row()));

        var parsed = new XlsxImportParser(new ImportHeaderMapper()).parse(content);

        assertThat(parsed.rows()).hasSize(1);
        assertThat(parsed.rows().getFirst().fields())
            .containsEntry("position_title", "Engineer")
            .containsEntry("company_name", "=Unsafe Co");
        assertThat(parsed.fileWarnings()).isEmpty();
    }

    private ApplicationTrackerResponse row() {
        OffsetDateTime created = OffsetDateTime.parse("2026-08-01T10:00:00Z");
        return new ApplicationTrackerResponse(
            1L, 2L, "=Unsafe Co", "Engineer", "Platform", "New York",
            RemoteType.REMOTE, "FULL_TIME", new BigDecimal("100000"),
            new BigDecimal("125000"), "USD", "salary", "https://example.com",
            "Referral", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1),
            (short) 2, new BigDecimal("91.5"), "description", "@job note",
            created, created, 3L, ApplicationStatus.APPLIED, "resume-v2", true,
            "https://portfolio.example", "https://github.example", "project",
            "java", "systems", "Recruiter", "r@example.com",
            LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 9), created,
            created, created, created, null, "+application note", created, created
        );
    }
}
