package com.example.api.controller;

import com.example.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public List<String> list() throws IOException {
        return reportService.listReports();
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam String path) throws IOException {
        byte[] content = reportService.loadReport(path);

        String filename = path.contains("/")
                ? path.substring(path.lastIndexOf("/") + 1)
                : path;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
