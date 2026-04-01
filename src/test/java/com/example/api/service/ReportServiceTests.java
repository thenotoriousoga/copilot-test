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

class ReportServiceTests {

    private ReportService reportService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reportService = new ReportService();
        ReflectionTestUtils.setField(reportService, "reportDir", tempDir.toString());
    }

    @Nested
    @DisplayName("loadReport")
    class loadReportTests {

        @Test
        @DisplayName("正常系：ベースディレクトリ直下のファイルを読み込めること")
        void 正常系_ベースディレクトリ直下のファイルを読み込める() throws IOException {
            byte[] content = "test content".getBytes();
            Files.write(tempDir.resolve("report.pdf"), content);

            byte[] result = reportService.loadReport("report.pdf");

            assertArrayEquals(content, result);
        }

        @Test
        @DisplayName("正常系：サブディレクトリ内のファイルを読み込めること")
        void 正常系_サブディレクトリ内のファイルを読み込める() throws IOException {
            Path subDir = Files.createDirectory(tempDir.resolve("2024"));
            byte[] content = "sub content".getBytes();
            Files.write(subDir.resolve("monthly.pdf"), content);

            byte[] result = reportService.loadReport("2024/monthly.pdf");

            assertArrayEquals(content, result);
        }

        @Test
        @DisplayName("異常系：ファイルパスがnullの場合にIllegalArgumentExceptionがスローされること")
        void 異常系_nullパスで例外() {
            assertThrows(IllegalArgumentException.class, () -> reportService.loadReport(null));
        }

        @Test
        @DisplayName("異常系：ファイルパスが空文字の場合にIllegalArgumentExceptionがスローされること")
        void 異常系_空文字パスで例外() {
            assertThrows(IllegalArgumentException.class, () -> reportService.loadReport(""));
        }

        @Test
        @DisplayName("異常系：ファイルパスが空白のみの場合にIllegalArgumentExceptionがスローされること")
        void 異常系_空白パスで例外() {
            assertThrows(IllegalArgumentException.class, () -> reportService.loadReport("   "));
        }

        @Test
        @DisplayName("異常系：トラバーサルパスの場合にSecurityViolationExceptionがスローされること")
        void 異常系_トラバーサルパスで例外() {
            assertThrows(SecurityViolationException.class, () -> reportService.loadReport("../etc/passwd"));
        }

        @Test
        @DisplayName("異常系：絶対パスでベースディレクトリ外を指す場合にSecurityViolationExceptionがスローされること")
        void 異常系_ベースディレクトリ外パスで例外() {
            assertThrows(SecurityViolationException.class, () -> reportService.loadReport("/etc/passwd"));
        }

        @Test
        @DisplayName("異常系：ディレクトリを指すパスの場合にIllegalArgumentExceptionがスローされること")
        void 異常系_ディレクトリパスで例外() throws IOException {
            Files.createDirectory(tempDir.resolve("subdir"));

            assertThrows(IllegalArgumentException.class, () -> reportService.loadReport("subdir"));
        }

        @Test
        @DisplayName("異常系：存在しないファイルの場合にIOExceptionがスローされること")
        void 異常系_存在しないファイルで例外() {
            assertThrows(IOException.class, () -> reportService.loadReport("nonexistent.pdf"));
        }
    }
}
