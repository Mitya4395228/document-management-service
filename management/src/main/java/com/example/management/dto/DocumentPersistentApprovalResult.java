package com.example.management.dto;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.management.dto.enums.ResultType;
import com.example.management.entity.enums.DocumentStatus;

public record DocumentPersistentApprovalResult(UUID id, Map<ResultType, AtomicInteger> results, DocumentStatus status) {

}
