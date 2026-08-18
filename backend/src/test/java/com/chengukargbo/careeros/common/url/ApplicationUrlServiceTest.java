package com.chengukargbo.careeros.common.url;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;

class ApplicationUrlServiceTest {
    private final ApplicationUrlService service = new ApplicationUrlService();

    @Test
    void normalizesPublicHttpUrlsDeterministically() {
        assertEquals(
            "https://jobs.example.com/apply?id=7",
            service.normalize(" HTTPS://Jobs.Example.com:443/apply?id=7#fragment ")
        );
    }

    @Test
    void rejectsMalformedAndUnsupportedUrls() {
        assertAll(
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("not a URL")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("file:///tmp/application")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("ftp://example.com/job")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("https://user:secret@example.com/job"))
        );
    }

    @Test
    void rejectsLocalAndPrivateAddressesIncludingRedirectTargets() {
        assertAll(
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("http://localhost/apply")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("http://127.0.0.1/apply")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("http://10.2.3.4/apply")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.normalize("http://192.168.1.2/apply")),
            () -> assertThrows(BusinessValidationException.class,
                () -> service.validateRedirect(
                    "https://example.com/apply", "http://169.254.169.254/latest"
                ))
        );
    }
}
