package com.chengukargbo.careeros.importing.persistence;

public class CompanyResolutionRaceException extends RuntimeException {
    public CompanyResolutionRaceException(Throwable cause) {
        super("Company was created concurrently", cause);
    }
}
