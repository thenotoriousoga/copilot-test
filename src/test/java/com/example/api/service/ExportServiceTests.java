package com.example.api.service;

import com.example.api.exception.SecurityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTests {

    private ExportService exportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exportService = new ExportService();
        ReflectionTestUtils.setField(exportService, "exportDir", tempDir.toString());
    }

    @Nested
    @DisplayName("loadExportFile")
    class loadExportFileTests {

        @Test
        @DisplayName("正常系：有効なファイル名でエクスポートファイルを読み込めること")
        void 正常系_有効なファイル名でファイルを読み込める() throws IOException {
            byte[] content = "export data".getBytes();
            Files.write(tempDir.resolve("export.csv"), content);

            byte[] result = exportService.loadExportFile("export.csv");

            assertArrayEquals(content, result);
        }

        @Test
        @DisplayName("異常系：トラバーサルパターンを含むファイル名でSecurityViolationExceptionがスローされること")
        void 異常系_トラバーサルパターンで例外() {
            assertThrows(SecurityViolationException.class, () -> exportService.loadExportFile("../secret.csv"));
        }

        @Test
        @DisplayName("異常系：ファイル名にパス区切り文字を含む場合にIllegalArgumentExceptionがスローされること")
        void 異常系_パス区切り文字で例外() {
            assertThrows(IllegalArgumentException.class, () -> exportService.loadExportFile("sub/export.csv"));
        }

        @Test
        @DisplayName("異常系：ファイル名がnullの場合にIllegalArgumentExceptionがスローされること")
        void 異常系_nullファイル名で例外() {
            assertThrows(IllegalArgumentException.class, () -> exportService.loadExportFile(null));
        }

        @Test
        @DisplayName("異常系：ファイル名が空文字の場合にIllegalArgumentExceptionがスローされること")
        void 異常系_空文字ファイル名で例外() {
            assertThrows(IllegalArgumentException.class, () -> exportService.loadExportFile(""));
        }

        @Test
        @DisplayName("異常系：存在しないファイルの場合にIOExceptionがスローされること")
        void 異常系_存在しないファイルで例外() {
            assertThrows(IOException.class, () -> exportService.loadExportFile("nonexistent.csv"));
        }
    }
}
