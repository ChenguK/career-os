package com.chengukargbo.careeros.common.url;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.chengukargbo.careeros.common.exception.BusinessValidationException;

@Service
public class ApplicationUrlService {

    private static final Set<String> LOCAL_NAMES = Set.of(
        "localhost", "localhost.localdomain", "ip6-localhost"
    );

    public String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(value.trim());
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw invalid();
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new BusinessValidationException(
                    "Application URL must use HTTP or HTTPS"
                );
            }
            if (uri.getUserInfo() != null) {
                throw new BusinessValidationException(
                    "Application URL must not contain embedded credentials"
                );
            }
            String host = uri.getHost().toLowerCase(Locale.ROOT);
            if (host.startsWith("[") && host.endsWith("]")) {
                host = host.substring(1, host.length() - 1);
            }
            rejectPrivateHost(host);
            int port = uri.getPort();
            if ((scheme.equals("http") && port == 80)
                || (scheme.equals("https") && port == 443)) {
                port = -1;
            }
            return new URI(
                scheme, null, host, port, emptyPath(uri.getRawPath()),
                uri.getRawQuery(), null
            ).toASCIIString();
        } catch (URISyntaxException exception) {
            throw invalid();
        }
    }

    public String validateRedirect(String currentUrl, String redirectUrl) {
        String current = normalize(currentUrl);
        String redirect = normalize(redirectUrl);
        if (current == null || redirect == null) {
            throw invalid();
        }
        return redirect;
    }

    private void rejectPrivateHost(String host) {
        if (LOCAL_NAMES.contains(host) || host.endsWith(".localhost")
            || host.equals("0.0.0.0") || host.equals("::")
            || host.equals("::1") || privateIpv4(host)
            || host.startsWith("fc") || host.startsWith("fd")
            || host.startsWith("fe8") || host.startsWith("fe9")
            || host.startsWith("fea") || host.startsWith("feb")) {
            throw new BusinessValidationException(
                "Application URL must use a public network address"
            );
        }
    }

    private boolean privateIpv4(String host) {
        String[] parts = host.split("\\.");
        if (parts.length != 4) {
            return false;
        }
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            return first == 10 || first == 127 || first == 0
                || (first == 169 && second == 254)
                || (first == 172 && second >= 16 && second <= 31)
                || (first == 192 && second == 168)
                || (first == 100 && second >= 64 && second <= 127)
                || first >= 224;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String emptyPath(String path) {
        return path == null || path.isEmpty() ? "/" : path;
    }

    private BusinessValidationException invalid() {
        return new BusinessValidationException(
            "Application URL must be a valid public HTTP or HTTPS URL"
        );
    }
}
