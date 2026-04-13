package com.example.api.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイル操作API
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_UPLOAD_DIR = "uploads";

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
}
