package com.chengukargbo.careeros.jobs;

public class JobOpportunityNotFoundException
    extends RuntimeException {

    public JobOpportunityNotFoundException(Long id) {
        super("Job opportunity not found with id: " + id);
    }
}