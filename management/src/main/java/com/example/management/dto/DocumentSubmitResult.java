package com.example.management.dto;

import java.util.UUID;

import com.example.management.dto.enums.ResultType;

public record DocumentSubmitResult(UUID id, ResultType message) {

}
