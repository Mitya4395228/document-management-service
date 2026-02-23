package com.example.common.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.common.dto.enums.Action;

public record DocumentStatusHistoryReadDTO(UUID id, Action action, String initiator, String comment,
                LocalDateTime createdAt) {

}
