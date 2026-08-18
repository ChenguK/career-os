package com.chengukargbo.careeros.importing.csv;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.chengukargbo.careeros.importing.persistence.ImportPersistenceRequest;
import com.chengukargbo.careeros.importing.persistence.ImportPersistenceResponse;
import com.chengukargbo.careeros.importing.persistence.ImportPersistenceService;
import com.chengukargbo.careeros.importing.xlsx.XlsxImportPreviewService;

@RestController
@RequestMapping("/api/applications/import")
public class CsvImportController {

    private final CsvImportPreviewService previewService;
    private final ImportPersistenceService persistenceService;
    private final XlsxImportPreviewService xlsxPreviewService;

    public CsvImportController(
        CsvImportPreviewService previewService,
        ImportPersistenceService persistenceService,
        XlsxImportPreviewService xlsxPreviewService
    ) {
        this.previewService = previewService;
        this.persistenceService = persistenceService;
        this.xlsxPreviewService = xlsxPreviewService;
    }

    @PostMapping(
        path = "/preview",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImportPreviewResponse preview(
        @RequestPart("file") MultipartFile file
    ) {
        String filename = file == null ? null : file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".xlsx")
            ? xlsxPreviewService.preview(file)
            : previewService.preview(file);
    }

    @PostMapping
    public ImportPersistenceResponse persist(
        @RequestBody ImportPersistenceRequest request
    ) {
        return persistenceService.persist(request);
    }
}
