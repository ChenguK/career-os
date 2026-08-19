package com.chengukargbo.careeros.applications;

import java.time.LocalDate;

import com.chengukargbo.careeros.applications.dto.ApplicationResponse;
import com.chengukargbo.careeros.applications.lock.ApplicationLockDtos;

import jakarta.validation.constraints.NotNull;

public final class ManualSubmissionDtos {
    private ManualSubmissionDtos() {}

    public record Request(@NotNull LocalDate applicationDate) {}

    public record Response(
        ApplicationResponse application,
        ApplicationLockDtos.Response lock
    ) {}
}
