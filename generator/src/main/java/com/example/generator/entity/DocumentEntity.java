package com.example.generator.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.common.dto.enums.DocumentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "documents", schema = "document_flow")
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false, unique = true, insertable = false, updatable = false)
    Long number;

    String author;

    String title;

    @Enumerated(EnumType.STRING)
    DocumentStatus status;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    @Version
    Integer version;

}
