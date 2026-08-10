package com.chengukargbo.careeros.importing.history;

public class ImportBatchNotFoundException extends RuntimeException {
    public ImportBatchNotFoundException(Long id) {
        super("Import batch not found with id: " + id);
    }
}
