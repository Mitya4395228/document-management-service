package com.example.common.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record DocumentBatchRequest(@NotEmpty Set<UUID> ids) {

}
