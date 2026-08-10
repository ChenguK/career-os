package com.chengukargbo.careeros.importing;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class ApplicationUrlNormalizer {

    public String normalize(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            URI uri = new URI(trimmed);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return trimmed;
            }

            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            int port = uri.getPort();
            if ((scheme.equals("http") && port == 80)
                || (scheme.equals("https") && port == 443)) {
                port = -1;
            }

            return new URI(
                scheme,
                uri.getUserInfo(),
                host,
                port,
                uri.getRawPath(),
                uri.getRawQuery(),
                null
            ).toASCIIString();
        } catch (URISyntaxException exception) {
            return trimmed;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
