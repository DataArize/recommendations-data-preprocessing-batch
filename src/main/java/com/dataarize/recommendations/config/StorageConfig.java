package com.dataarize.recommendations.config;

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.threeten.bp.Duration;

@Configuration
public class StorageConfig {

    @Bean
    public Storage storage() {
        return StorageOptions.newBuilder()
                .setRetrySettings(RetrySettings.newBuilder()
                        .setMaxAttempts(5)
                        .setInitialRetryDelay(Duration.ofSeconds(1))
                        .setMaxRetryDelay(Duration.ofSeconds(10))
                        .setRetryDelayMultiplier(1.5)
                        .build())
                .build()
                .getService();
    }

}
