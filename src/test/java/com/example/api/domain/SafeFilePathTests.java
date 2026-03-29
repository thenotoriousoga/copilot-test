package com.example.api.domain;

import com.example.api.exception.SecurityViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SafeFilePathTests {

    @Nested
    @DisplayName("SafeFilePath コンストラクタ")
    class ConstructorTests {

        @TempDir
        Path tempDir;

        @ParameterizedTest(name = "ファイル名 [{0}] で正しく解決されること")
        @ValueSource(strings = {
                "report.pdf",           // 基本的なファイル名
                "my.report.2024.pdf",   // ドットを複数含むファイル名
                "file name.txt",        // スペースを含む
                "file#1.txt",           // 記号 #
                "file@2.txt",           // 記号 @
                "file!3.txt",           // 記号 !
                "file (copy).txt",      // 括弧とスペース
                "テスト.txt"             // マルチバイト文字
        })
        @DisplayName("正常系：有効なファイル名で正しく解決されること")
        void 正常系_有効なファイル名(String filename) {
            SafeFilePath filePath = new SafeFilePath(tempDir.toString(), filename);
            assertEquals(tempDir.toAbsolutePath().normalize().resolve(filename), filePath.getPath());
        }

        @Test
        @DisplayName("正常系：ベースディレクトリのパスが正規化されること")
        void 正常系_パスが正規化される() {
            String pathWithDot = tempDir.toString() + "/./";
            SafeFilePath filePath = new SafeFilePath(pathWithDot, "test.txt");
            assertEquals(tempDir.toAbsolutePath().normalize().resolve("test.txt"), filePath.getPath());
        }

        @ParameterizedTest(name = "ディレクトリが [{0}] の場合に例外がスローされること")
        @NullAndEmptySource
        @ValueSource(strings = {"   "}) // 空白のみ
        @DisplayName("異常系：ディレクトリがnull・空文字・空白の場合に例外がスローされること")
        void 異常系_ディレクトリが空系で例外(String baseDir) {
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath(baseDir, "test.txt"));
        }

        @Test
        @DisplayName("異常系：存在しないディレクトリの場合に例外がスローされること")
        void 異常系_存在しないディレクトリで例外() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath("/nonexistent/directory/path", "test.txt"));
        }

        @ParameterizedTest(name = "ディレクトリに相対パス [{0}] が含まれる場合に例外がスローされること")
        @ValueSource(strings = {
                "../uploads",           // Unix系の先頭トラバーサル
                "..\\uploads",          // Windows系の先頭トラバーサル
                "..",                   // 親ディレクトリ参照（単体）
                "/uploads/..",          // 末尾からの親ディレクトリ参照
                "uploads/../secret"     // 中間のトラバーサル
        })
        @DisplayName("異常系：ディレクトリに相対パスが含まれる場合にSecurityViolationExceptionがスローされること")
        void 異常系_ディレクトリにトラバーサル文字列で例外(String baseDir) {
            assertThrows(SecurityViolationException.class,
                    () -> new SafeFilePath(baseDir, "test.txt"));
        }

        @ParameterizedTest(name = "ファイル名が [{0}] の場合に例外がスローされること")
        @NullAndEmptySource
        @ValueSource(strings = {"   "}) // 空白のみ
        @DisplayName("異常系：ファイル名がnull・空文字・空白の場合に例外がスローされること")
        void 異常系_ファイル名が空系で例外(String filename) {
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath(tempDir.toString(), filename));
        }

        @Test
        @DisplayName("異常系：ドット単体のファイル名はディレクトリを指すため例外がスローされること")
        void 異常系_ドット単体のファイル名() {
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath(tempDir.toString(), "."));
        }

        @ParameterizedTest(name = "ファイル名にパス区切り文字 [{0}] が含まれる場合に例外がスローされること")
        @ValueSource(strings = {
                "sub/report.pdf",   // Unix系パス区切り
                "sub\\report.pdf"   // Windows系パス区切り
        })
        @DisplayName("異常系：ファイル名にパス区切り文字が含まれる場合に例外がスローされること")
        void 異常系_ファイル名にパス区切り文字で例外(String filename) {
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath(tempDir.toString(), filename));
        }

        @ParameterizedTest(name = "トラバーサル文字列 [{0}] がvalidateInputで弾かれること")
        @ValueSource(strings = {
                "../etc/passwd",            // Unix系トラバーサル
                "..\\windows\\system32",    // Windows系トラバーサル
                ".."                        // 親ディレクトリ参照（単体）
        })
        @DisplayName("異常系：トラバーサルパターンを含むファイル名でSecurityViolationExceptionがスローされること")
        void 異常系_トラバーサルパターンで例外(String filename) {
            assertThrows(SecurityViolationException.class,
                    () -> new SafeFilePath(tempDir.toString(), filename));
        }

        @ParameterizedTest(name = "パス区切り文字を含むトラバーサル [{0}] がvalidateInputで弾かれること")
        @ValueSource(strings = {
                "subdir/../../secret.txt",      // サブディレクトリ経由のUnix系トラバーサル（"../"にマッチ）
                "subdir\\..\\..\\secret.txt"    // サブディレクトリ経由のWindows系トラバーサル（"..\\"にマッチ）
        })
        @DisplayName("異常系：パス区切り文字を含むトラバーサルでSecurityViolationExceptionがスローされること")
        void 異常系_パス区切り文字を含むトラバーサルで例外(String filename) {
            assertThrows(SecurityViolationException.class,
                    () -> new SafeFilePath(tempDir.toString(), filename));
        }

        @Test
        @DisplayName("異常系：絶対パスのファイル名でIllegalArgumentExceptionがスローされること")
        void 異常系_絶対パスのファイル名で例外() {
            // "/etc/passwd" はトラバーサルパターンにはマッチしないが、パス区切り文字 "/" を含むため弾かれる
            assertThrows(IllegalArgumentException.class,
                    () -> new SafeFilePath(tempDir.toString(), "/etc/passwd"));
        }

        @ParameterizedTest(name = "URLエンコード済み文字列 [{0}] が「..」パターンで弾かれること")
        @ValueSource(strings = {
                "..%2Fetc%2Fpasswd",        // %2F = "/" のURLエンコード
                "..%5Cwindows%5Csystem32"   // %5C = "\" のURLエンコード
        })
        @DisplayName("異常系：URLエンコード済みトラバーサル文字列でSecurityViolationExceptionがスローされること")
        void 異常系_URLエンコード済みトラバーサルで例外(String filename) {
            // デコードされずリテラルとして扱われるが、先頭の「..」でトラバーサル検出される
            assertThrows(SecurityViolationException.class,
                    () -> new SafeFilePath(tempDir.toString(), filename));
        }

        @Test
        @DisplayName("異常系：「..」ファイル名はトラバーサルチェックでSecurityViolationExceptionがスローされること")
        void 異常系_ドットドットファイル名はトラバーサルチェックで弾かれる() throws IOException {
            // startsWithの最終防衛ラインではなく、手前のトラバーサルチェックで弾かれる
            Path subDir = Files.createDirectory(tempDir.resolve("sub"));
            assertThrows(SecurityViolationException.class,
                    () -> new SafeFilePath(subDir.toString(), ".."));
        }
    }
}
