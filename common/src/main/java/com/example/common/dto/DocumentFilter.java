package com.example.common.dto;

import java.time.LocalDateTime;

import com.example.common.dto.enums.DocumentStatus;

public record DocumentFilter(String author, String title, DocumentStatus status, LocalDateTime createdFrom,
                LocalDateTime createdTo) {

}
