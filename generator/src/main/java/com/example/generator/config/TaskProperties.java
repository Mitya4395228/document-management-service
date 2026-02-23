package com.example.generator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record TaskProperties(int batchSize, int generatingSize, String baseUrl, String creatingUrl, String submitUrl,
        String approvalUrl, long submitInitialDelay, long submitDelay, long approvalInitialDelay, long approvalDelay) {

}
