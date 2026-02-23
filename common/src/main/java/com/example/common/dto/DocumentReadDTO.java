package com.example.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.common.dto.enums.DocumentStatus;

public record DocumentReadDTO(UUID id, Long number, String author, String title, DocumentStatus status,
                LocalDateTime createdAt, LocalDateTime updatedAt) {

}
