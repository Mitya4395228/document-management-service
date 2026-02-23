package com.example.common.dto;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.common.dto.enums.ResultType;
import com.example.common.dto.enums.DocumentStatus;

public record DocumentPersistentApprovalResult(UUID id, Map<ResultType, AtomicInteger> results, DocumentStatus status) {

}
