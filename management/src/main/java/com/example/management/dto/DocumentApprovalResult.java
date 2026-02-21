package com.example.management.dto;

import java.util.UUID;

import com.example.management.dto.enums.ResultType;

public record DocumentApprovalResult(UUID id, ResultType message) {

}
