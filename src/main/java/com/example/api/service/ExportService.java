package com.example.api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class ExportService {

    @Value("${app.storage.export-dir:./storage/exports}")
    private String exportDir;

    /**
     * 指定されたファイル名のエクスポートファイルを読み込む。
     * トラバーサル文字列のチェックを行い、不正なパスを拒否する。
     */
    public byte[] loadExportFile(String filename) throws IOException {
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("不正なファイル名です: " + filename);
        }

        Path filePath = Paths.get(exportDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IOException("エクスポートファイルが見つかりません: " + filename);
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * エクスポートファイルの一覧を取得する。
     */
    public java.util.List<String> listExportFiles() throws IOException {
        Path dir = Paths.get(exportDir);
        if (!Files.isDirectory(dir)) {
            return java.util.Collections.emptyList();
        }
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }
}
