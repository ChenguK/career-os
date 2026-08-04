package com.chengukargbo.careeros.companies;

public class CompanyNotFoundException extends RuntimeException {

    public CompanyNotFoundException(Long id) {
        super("Company not found with id: " + id);
    }
}