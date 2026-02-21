package com.example.management.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.management.entity.enums.Action;

public record DocumentStatusHistoryReadDTO(UUID id, Action action, String initiator, String comment,
                LocalDateTime createdAt) {

}
