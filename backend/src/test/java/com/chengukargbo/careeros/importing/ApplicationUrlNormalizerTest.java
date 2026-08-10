package com.chengukargbo.careeros.importing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationUrlNormalizerTest {

    private final ApplicationUrlNormalizer normalizer =
        new ApplicationUrlNormalizer();

    @Test
    void normalizesOnlyConservativeUrlComponents() {
        assertThat(normalizer.normalize(
            " HTTPS://EXAMPLE.COM:443/jobs/One?source=abc#apply "
        )).isEqualTo("https://example.com/jobs/One?source=abc");
        assertThat(normalizer.normalize("https://example.com/jobs/One"))
            .isNotEqualTo(normalizer.normalize("https://example.com/jobs/Two"));
        assertThat(normalizer.normalize("https://example.com/job?a=1"))
            .isNotEqualTo(normalizer.normalize("https://example.com/job?a=2"));
        assertThat(normalizer.normalize("http://EXAMPLE.com:80/job#top"))
            .isEqualTo("http://example.com/job");
        assertThat(normalizer.normalize("   ")).isNull();
    }
}
