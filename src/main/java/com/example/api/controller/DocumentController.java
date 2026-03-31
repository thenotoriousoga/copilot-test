package com.example.api.controller;

import com.example.api.entity.Document;
import com.example.api.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping
    public List<Document> list() {
        return documentService.findAll();
    }

    @PostMapping
    public Document upload(@RequestParam String title,
                           @RequestParam MultipartFile file,
                           @RequestParam Long uploadedBy) throws IOException {
        return documentService.upload(
                title,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getBytes(),
                uploadedBy
        );
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) throws IOException {
        Document document = documentService.findById(id);
        byte[] content = documentService.loadContent(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getTitle() + "\"")
                .contentType(MediaType.parseMediaType(document.getContentType()))
                .body(content);
    }
}
