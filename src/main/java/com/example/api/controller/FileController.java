package com.example.api.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * ファイル操作API
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_UPLOAD_DIR = "uploads";

    /** ベースディレクトリの正規化済み絶対パス */
    private final Path baseDir = Paths.get(BASE_UPLOAD_DIR).toAbsolutePath().normalize();

    /**
     * 指定されたファイルをダウンロードする
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filename) throws IOException {
        Path filePath = Paths.get(BASE_UPLOAD_DIR, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }

    /**
     * 指定されたファイルの内容をテキストとして返す
     */
    @GetMapping("/read/{*filepath}")
    public ResponseEntity<String> readFile(@PathVariable String filepath) throws IOException {
        Path filePath = Paths.get(BASE_UPLOAD_DIR + filepath);

        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        String content = Files.readString(filePath);
        return ResponseEntity.ok(content);
    }

    /**
     * 指定されたディレクトリ内のファイル一覧を返す
     */
    @GetMapping("/list")
    public ResponseEntity<String[]> listFiles(@RequestParam(defaultValue = "") String directory) {
        File dir = new File(BASE_UPLOAD_DIR, directory);

        if (!dir.exists() || !dir.isDirectory()) {
            return ResponseEntity.notFound().build();
        }

        String[] files = dir.list();
        return ResponseEntity.ok(files);
    }

    // ========================================
    // セキュアなファイル操作API
    // ========================================

    /**
     * セキュアなファイルダウンロード
     */
    @GetMapping("/secure/download")
    public ResponseEntity<Resource> secureDownloadFile(@RequestParam String filename) throws IOException {
        Path resolved = resolveSecurePath(filename);
        if (resolved == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(resolved.toFile());
        String contentType = Files.probeContentType(resolved);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // ファイル名のみを取得してContent-Dispositionに設定
        String safeFilename = resolved.getFileName().toString();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                .body(resource);
    }

    /**
     * セキュアなファイル読み取り
     */
    @GetMapping("/secure/read")
    public ResponseEntity<String> secureReadFile(@RequestParam String filename) throws IOException {
        Path resolved = resolveSecurePath(filename);
        if (resolved == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(resolved) || !Files.isRegularFile(resolved)) {
            return ResponseEntity.notFound().build();
        }

        String content = Files.readString(resolved);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(content);
    }

    /**
     * セキュアなファイル一覧取得
     */
    @GetMapping("/secure/list")
    public ResponseEntity<String[]> secureListFiles(@RequestParam(defaultValue = "") String directory) {
        Path resolved = resolveSecurePath(directory);
        if (resolved == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(resolved) || !Files.isDirectory(resolved)) {
            return ResponseEntity.notFound().build();
        }

        String[] files = resolved.toFile().list();
        return ResponseEntity.ok(files);
    }

    /**
     * セキュアなファイルアップロード
     */
    @PostMapping("/secure/upload")
    public ResponseEntity<String> secureUploadFile(@RequestParam MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("ファイルが空です");
        }

        // UUIDで一意なファイル名を生成する
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String safeFilename = UUID.randomUUID() + extension;

        Path targetPath = baseDir.resolve(safeFilename).normalize();

        // ベースディレクトリ配下であることを検証
        if (!targetPath.startsWith(baseDir)) {
            return ResponseEntity.badRequest().body("不正なファイルパスです");
        }

        // ディレクトリが存在しない場合は作成
        Files.createDirectories(targetPath.getParent());
        Files.copy(file.getInputStream(), targetPath);

        return ResponseEntity.ok("アップロード成功: " + safeFilename);
    }

    /**
     * パスを安全に解決するヘルパーメソッド
     *
     * @param userInput 外部入力のファイル名またはパス
     * @return 安全に解決されたパス。不正な場合はnull
     */
    private Path resolveSecurePath(String userInput) {
        if (userInput == null || userInput.isEmpty()) {
            return baseDir;
        }

        // トラバーサル文字列のチェック
        if (userInput.contains("..") || userInput.contains("%2e") || userInput.contains("%2E")) {
            return null;
        }

        // ファイル名のみを取り出す
        Path fileName = Paths.get(userInput).getFileName();
        if (fileName == null) {
            return null;
        }

        // 固定ディレクトリと結合し正規化
        Path resolved = baseDir.resolve(fileName).normalize();

        // ベースディレクトリ配下であることを検証
        if (!resolved.startsWith(baseDir)) {
            return null;
        }

        return resolved;
    }
}
