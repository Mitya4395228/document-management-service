package com.example.management.controller;

import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.management.dto.DocumentApproval;
import com.example.management.dto.DocumentApprovalResult;
import com.example.management.dto.DocumentCreateDTO;
import com.example.management.dto.DocumentFilter;
import com.example.management.dto.DocumentBatchReceipt;
import com.example.management.dto.DocumentPersistentApproval;
import com.example.management.dto.DocumentPersistentApprovalResult;
import com.example.management.dto.DocumentReadDTO;
import com.example.management.dto.DocumentReadExtendedDTO;
import com.example.management.dto.DocumentSubmit;
import com.example.management.dto.DocumentSubmitResult;
import com.example.management.service.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@Tag(name = "DocumentController", description = "Document management")
@RestController
@RequestMapping(value = "/api/v1/documents", produces = "application/json")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentController {

    @Autowired
    DocumentService service;

    @Operation(summary = "Get document by id", description = "Get document with status history by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document found"),
            @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public DocumentReadExtendedDTO getDocument(@PathVariable UUID id) {
        return service.getDocument(id);
    }

    @Operation(summary = "Get documents", description = "Get documents by list of id. If document not found, it will be skipped.")
    @GetMapping("/package")
    public PagedModel<DocumentReadDTO> getDocuments(@ParameterObject @Valid DocumentBatchReceipt pack,
            @ParameterObject @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return service.getDocuments(pack, pageable);
    }

    @Operation(summary = "Get documents by filter", description = "Get documents by filter. If document not found, it will be skipped.")
    @GetMapping("/filter")
    public PagedModel<DocumentReadDTO> getDocumentsByFilter(@ParameterObject DocumentFilter filter,
            @ParameterObject @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return service.getAllByFilter(filter, pageable);
    }

    @Operation(summary = "Create document with draft status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Document created"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public DocumentReadDTO create(@Valid @RequestBody DocumentCreateDTO createDto) {
        return service.create(createDto);
    }

    @Operation(summary = "Submit documents", description = "Submit documents to the next approver. Minimum 1 and maximum 1000 documents can be submitted at a time.")
    @PatchMapping("/submit")
    public List<DocumentSubmitResult> submit(@Valid @RequestBody DocumentSubmit submit) {
        return service.submit(submit);
    }

    @Operation(summary = "Approve documents", description = "Approve documents. Minimum 1 and maximum 1000 documents can be approved at a time.")
    @PatchMapping("/approval")
    public List<DocumentApprovalResult> approve(@Valid @RequestBody DocumentApproval approval) {
        return service.approve(approval);
    }

    @Operation(summary = "Persistent approve documents", description = "Multiple attempts to approve document.")
    @ApiResponse(responseCode = "200", description = "Successful operation", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocumentPersistentApprovalResult.class), examples = @ExampleObject(value = """
            {
                "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                "results":
                    {
                        "SUCCESS": 1,
                        "CONFLICT": 50,
                        "DOCUMENT_NOT_FOUND": 47,
                        "REGISTRY_REGISTRATION_ERROR": 14,
                        "ERROR": 0
                    },
                "status": "APPROVED"
            }
            """)))
    @PatchMapping("/persistent-approval")
    public DocumentPersistentApprovalResult persistentApprove(@Valid @RequestBody DocumentPersistentApproval approval)
            throws InterruptedException {
        return service.persistentApprove(approval);
    }

}
