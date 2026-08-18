package com.chengukargbo.careeros.answers;

public class ApprovedAnswerNotFoundException extends RuntimeException {
    public ApprovedAnswerNotFoundException(Long id) {
        super("Approved answer " + id + " was not found");
    }
}
