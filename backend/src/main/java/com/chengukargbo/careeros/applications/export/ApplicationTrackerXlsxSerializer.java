package com.chengukargbo.careeros.applications.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.chengukargbo.careeros.applications.ApplicationStatus;
import com.chengukargbo.careeros.applications.dto.ApplicationTrackerResponse;

@Component
public class ApplicationTrackerXlsxSerializer {

    public byte[] serialize(List<ApplicationTrackerResponse> rows) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Applications");
            CellStyle headerStyle = headerStyle(workbook);
            CellStyle dateStyle = dateStyle(workbook);
            CellStyle wrappedStyle = wrappedStyle(workbook);

            Row header = sheet.createRow(0);
            for (int index = 0; index < ApplicationTrackerCsvSerializer.HEADERS.length; index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(ApplicationTrackerCsvSerializer.HEADERS[index]);
                cell.setCellStyle(headerStyle);
            }
            for (int index = 0; index < rows.size(); index++) {
                writeRow(sheet.createRow(index + 1), rows.get(index), dateStyle, wrappedStyle);
            }

            sheet.createFreezePane(0, 1);
            sheet.setAutoFilter(new CellRangeAddress(
                0, Math.max(rows.size(), 1), 0,
                ApplicationTrackerCsvSerializer.HEADERS.length - 1
            ));
            setWidths(sheet);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create tracker XLSX", exception);
        }
    }

    private void writeRow(
        Row target,
        ApplicationTrackerResponse row,
        CellStyle dateStyle,
        CellStyle wrappedStyle
    ) {
        int cell = 0;
        number(target, cell++, row.jobOpportunityId());
        number(target, cell++, row.companyId());
        text(target, cell++, row.companyName());
        text(target, cell++, row.positionTitle());
        text(target, cell++, row.department());
        text(target, cell++, row.location());
        text(target, cell++, name(row.remoteType()));
        text(target, cell++, row.employmentType());
        number(target, cell++, row.salaryMin());
        number(target, cell++, row.salaryMax());
        text(target, cell++, row.salaryCurrency());
        wrapped(target, cell++, row.salaryNotes(), wrappedStyle);
        text(target, cell++, row.applicationUrl());
        text(target, cell++, row.source());
        date(target, cell++, row.datePosted(), dateStyle);
        date(target, cell++, row.closingDate(), dateStyle);
        number(target, cell++, row.priority());
        number(target, cell++, row.matchScore());
        wrapped(target, cell++, row.jobDescription(), wrappedStyle);
        wrapped(target, cell++, row.jobNotes(), wrappedStyle);
        text(target, cell++, datetime(row.jobCreatedAt()));
        text(target, cell++, datetime(row.jobUpdatedAt()));
        number(target, cell++, row.applicationId());
        text(target, cell++, name(
            row.status() == null ? ApplicationStatus.SAVED : row.status()
        ));
        text(target, cell++, row.resumeVersion());
        bool(target, cell++, row.coverLetterNeeded());
        text(target, cell++, row.portfolioLink());
        text(target, cell++, row.githubLink());
        wrapped(target, cell++, row.projectsToHighlight(), wrappedStyle);
        wrapped(target, cell++, row.skillsToEmphasize(), wrappedStyle);
        wrapped(target, cell++, row.interviewTopics(), wrappedStyle);
        text(target, cell++, row.recruiterName());
        text(target, cell++, row.recruiterEmail());
        date(target, cell++, row.applicationDate(), dateStyle);
        date(target, cell++, row.followUpDate(), dateStyle);
        text(target, cell++, datetime(row.phoneScreenAt()));
        text(target, cell++, datetime(row.interviewOneAt()));
        text(target, cell++, datetime(row.interviewTwoAt()));
        text(target, cell++, datetime(row.offerAt()));
        text(target, cell++, datetime(row.rejectedAt()));
        wrapped(target, cell++, row.applicationNotes(), wrappedStyle);
        text(target, cell++, datetime(row.applicationCreatedAt()));
        text(target, cell, datetime(row.applicationUpdatedAt()));
    }

    private void text(Row row, int index, String value) {
        if (value != null) {
            row.createCell(index).setCellValue(value);
        }
    }

    private void wrapped(Row row, int index, String value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(index);
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private void number(Row row, int index, Number value) {
        if (value != null) {
            row.createCell(index).setCellValue(value.doubleValue());
        }
    }

    private void number(Row row, int index, BigDecimal value) {
        if (value != null) {
            row.createCell(index).setCellValue(value.doubleValue());
        }
    }

    private void bool(Row row, int index, Boolean value) {
        if (value != null) {
            row.createCell(index).setCellValue(value);
        }
    }

    private void date(Row row, int index, LocalDate value, CellStyle style) {
        if (value != null) {
            Cell cell = row.createCell(index);
            cell.setCellValue(value);
            cell.setCellStyle(style);
        }
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private String datetime(OffsetDateTime value) {
        return value == null
            ? null
            : value.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle dateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.getCreationHelper()
            .createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private CellStyle wrappedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        return style;
    }

    private void setWidths(Sheet sheet) {
        for (int index = 0; index < ApplicationTrackerCsvSerializer.HEADERS.length; index++) {
            int width = switch (index) {
                case 18, 19, 28, 29, 30, 40 -> 50;
                case 12, 26, 27 -> 40;
                default -> 20;
            };
            sheet.setColumnWidth(index, width * 256);
        }
    }
}
