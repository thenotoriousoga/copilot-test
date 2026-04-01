package com.example.api.service;

import com.example.api.domain.SafeFilePath;
import com.example.api.entity.Document;
import com.example.api.repository.DocumentRepository;
import com.example.api.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Value("${app.storage.document-dir:./storage/documents}")
    private String documentDir;

    public List<Document> findAll() {
        return documentRepository.findAll();
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ドキュメントが見つかりません: " + id));
    }

    /**
     * ドキュメントをアップロードする。
     * IDベースでファイルを管理し、元のファイル名はDBに保存する。
     */
    public Document upload(String title, String originalFilename, String contentType,
                           byte[] data, Long uploadedBy) throws IOException {
        String storedFilename = UUID.randomUUID().toString();
        SafeFilePath filePath = new SafeFilePath(documentDir, storedFilename);
        FileStorageUtil.store(filePath, data);

        Document document = new Document(title, storedFilename, contentType, uploadedBy);
        return documentRepository.save(document);
    }

    /**
     * ドキュメントIDからファイルを読み込む。
     * DBに保存されたファイル名を使用するため、外部入力がファイルパスに直接影響しない。
     */
    public byte[] loadContent(Long documentId) throws IOException {
        Document document = findById(documentId);
        SafeFilePath filePath = new SafeFilePath(documentDir, document.getStoredFilename());
        return FileStorageUtil.load(filePath);
    }
}
