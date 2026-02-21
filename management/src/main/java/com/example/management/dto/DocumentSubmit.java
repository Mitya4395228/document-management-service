package com.example.management.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record DocumentSubmit(@Size(min = 1, max = 1000) Set<UUID> ids, @NotEmpty String initiator, String comment) {

}
