package com.example.api.controller;

import com.example.api.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/exports")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping
    public List<String> list() throws IOException {
        return exportService.listExportFiles();
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String filename) throws IOException {
        byte[] content = exportService.loadExportFile(filename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
