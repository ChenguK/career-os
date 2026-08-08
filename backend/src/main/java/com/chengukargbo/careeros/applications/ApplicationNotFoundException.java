package com.chengukargbo.careeros.applications;

public class ApplicationNotFoundException
    extends RuntimeException {

    public ApplicationNotFoundException(Long id) {
        super("Application not found with id: " + id);
    }
}