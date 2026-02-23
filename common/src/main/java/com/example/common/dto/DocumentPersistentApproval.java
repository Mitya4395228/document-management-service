package com.example.common.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DocumentPersistentApproval(@NotNull UUID id, @NotEmpty String initiator, String comment,
        @Positive int threads, @Positive int attempts) {

}
