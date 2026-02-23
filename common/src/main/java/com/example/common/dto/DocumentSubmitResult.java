package com.example.common.dto;

import java.util.UUID;

import com.example.common.dto.enums.ResultType;

public record DocumentSubmitResult(UUID id, ResultType message) {

}
