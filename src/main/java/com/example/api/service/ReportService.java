package com.example.api.service;

import com.example.api.exception.SecurityViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ReportService {

    @Value("${app.storage.report-dir:./storage/reports}")
    private String reportDir;

    /**
     * 指定されたパスのレポートファイルを読み込む。
     * サブディレクトリを含むパス指定に対応している。
     */
    public byte[] loadReport(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("ファイルパスが空です");
        }

        Path base = Paths.get(reportDir).toAbsolutePath().normalize();
        Path resolved = base.resolve(filePath).normalize();

        if (!resolved.startsWith(base)) {
            throw new SecurityViolationException("不正なファイルパスです");
        }
        if (Files.isDirectory(resolved)) {
            throw new IllegalArgumentException("ファイルパスがディレクトリを指しています: " + filePath);
        }
        if (!Files.exists(resolved)) {
            throw new IOException("レポートファイルが見つかりません: " + filePath);
        }
        return Files.readAllBytes(resolved);
    }

    /**
     * レポートファイルの一覧を取得する。
     */
    public java.util.List<String> listReports() throws IOException {
        Path dir = Paths.get(reportDir);
        if (!Files.isDirectory(dir)) {
            return java.util.Collections.emptyList();
        }
        try (var stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString())
                    .toList();
        }
    }
}
