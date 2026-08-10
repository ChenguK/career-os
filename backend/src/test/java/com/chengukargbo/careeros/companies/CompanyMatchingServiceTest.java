package com.chengukargbo.careeros.companies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class CompanyMatchingServiceTest {

    private final CompanyRepository repository = mock(CompanyRepository.class);
    private final CompanyMatchingService service =
        new CompanyMatchingService(repository);

    @Test
    void performsTrimmedCaseInsensitiveLookup() {
        Company company = company("Acme");
        when(repository.findFirstByNameIgnoreCase("ACME"))
            .thenReturn(Optional.of(company));

        assertThat(service.findNormalizedMatch("  ACME  ")).contains(company);
        verify(repository).findFirstByNameIgnoreCase("ACME");
        assertThat(service.normalizeForComparison("  ACME  "))
            .isEqualTo("acme");
    }

    @Test
    void returnsEmptyForNoMatchAndBlankInput() {
        when(repository.findFirstByNameIgnoreCase("Missing"))
            .thenReturn(Optional.empty());
        assertThat(service.findNormalizedMatch("Missing")).isEmpty();

        CompanyRepository untouched = mock(CompanyRepository.class);
        CompanyMatchingService blankService = new CompanyMatchingService(untouched);
        assertThat(blankService.findNormalizedMatch("   ")).isEmpty();
        verifyNoInteractions(untouched);
    }

    private Company company(String name) {
        return new Company(
            name, null, null, null, null, null, null, null, null,
            null, null, false
        );
    }
}
