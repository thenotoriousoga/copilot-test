package com.example.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String storedFilename;

    private String contentType;

    private Long uploadedBy;

    public Document(String title, String storedFilename, String contentType, Long uploadedBy) {
        this.title = title;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.uploadedBy = uploadedBy;
    }
}
