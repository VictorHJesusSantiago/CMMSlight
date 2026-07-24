package com.cmmslight.cmmsapi.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties({FileStorageProperties.class, BackupProperties.class})
@EnableScheduling
public class AppConfig {
}
