package com.example.api.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ファイルダウンロードAPI
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String UPLOAD_DIR = "/uploads";

    /**
     * 指定されたファイルをダウンロードする
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String filename) throws IOException {
        String decoded = URLDecoder.decode(filename, StandardCharsets.UTF_8);
        if (decoded.contains("../") || decoded.contains("..\\")) {
            return ResponseEntity.badRequest().build();
        }

        Path filePath = Paths.get(UPLOAD_DIR).resolve(decoded).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
