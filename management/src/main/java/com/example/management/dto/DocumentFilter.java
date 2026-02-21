package com.example.management.dto;

import java.time.LocalDateTime;

import com.example.management.entity.enums.DocumentStatus;

public record DocumentFilter(String author, String title, DocumentStatus status, LocalDateTime createdFrom,
                LocalDateTime createdTo) {

}
