package com.example.common.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record DocumentApproval(@Size(min = 1, max = 1000) Set<UUID> ids, @NotEmpty String initiator, String comment) {

}
