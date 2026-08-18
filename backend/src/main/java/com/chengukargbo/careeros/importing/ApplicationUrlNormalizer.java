package com.chengukargbo.careeros.importing;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import com.chengukargbo.careeros.common.url.ApplicationUrlService;

@Component
public class ApplicationUrlNormalizer {

    private final ApplicationUrlService urlService;

    public ApplicationUrlNormalizer() {
        this(new ApplicationUrlService());
    }

    @Autowired
    public ApplicationUrlNormalizer(ApplicationUrlService urlService) {
        this.urlService = urlService;
    }

    public String normalize(String value) {
        return urlService.normalize(value);
    }
}
