package com.example.common.dto;

import java.util.UUID;

import com.example.common.dto.enums.ResultType;

public record DocumentApprovalResult(UUID id, ResultType message) {

}
