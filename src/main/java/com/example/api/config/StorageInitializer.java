package com.example.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class StorageInitializer {

    @Value("${app.storage.document-dir:./storage/documents}")
    private String documentDir;

    @Value("${app.storage.export-dir:./storage/exports}")
    private String exportDir;

    @Value("${app.storage.report-dir:./storage/reports}")
    private String reportDir;

    @Bean
    CommandLineRunner initStorageDirectories() {
        return args -> {
            Files.createDirectories(Paths.get(documentDir));
            Files.createDirectories(Paths.get(exportDir));
            Files.createDirectories(Paths.get(reportDir));
        };
    }
}
