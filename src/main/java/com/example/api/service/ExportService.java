package com.example.api.service;

import com.example.api.domain.SafeFilePath;
import com.example.api.util.FileStorageUtil;
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
     * SafeFilePath によるトラバーサル検証・パス正規化・ベースディレクトリ検証を行う。
     */
    public byte[] loadExportFile(String filename) throws IOException {
        SafeFilePath filePath = new SafeFilePath(exportDir, filename);
        return FileStorageUtil.load(filePath);
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
