package com.example.management.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record DocumentBatchReceipt(@NotEmpty Set<UUID> ids) {

}
