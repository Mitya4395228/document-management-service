package com.example.generator.service;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.common.dto.DocumentApproval;
import com.example.common.dto.DocumentApprovalResult;
import com.example.common.dto.DocumentCreateDTO;
import com.example.common.dto.DocumentSubmit;
import com.example.common.dto.DocumentSubmitResult;
import com.example.generator.config.TaskProperties;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

/**
 * Accesses the API to process documents.
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class DocumentApiClient {

    RestClient restClient;

    TaskProperties taskProperties;

    public DocumentApiClient(RestClient.Builder builder, TaskProperties taskProperties) {
        this.taskProperties = taskProperties;
        restClient = builder.baseUrl(taskProperties.baseUrl()).build();
    }

    /**
     * Creates a new document by sending a POST request to the specified URL.
     *
     * This method uses the REST client to send a POST request with the provided
     * document data in the request body. The response is expected to be empty,
     * indicating successful creation of the document.
     *
     * @param document {@link DocumentCreateDTO}
     * @return ResponseEntity<Void> an HTTP response entity with no body, indicating
     *         the request was processed successfully
     */
    public ResponseEntity<Void> createDocument(DocumentCreateDTO document) {
        return restClient.post()
                .uri(taskProperties.creatingUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Submits a document for processing and retrieves the results.
     *
     * This method sends a PATCH request to the configured submission URL with the
     * provided document data in JSON format. It then retrieves and returns a list
     * of results
     * indicating the status of the document submission.
     *
     * @param document The {@link DocumentSubmit} to be submitted,
     *                 containing the necessary data
     *                 for processing.
     * @return A list of {@link DocumentSubmitResult} objects representing the
     *         results of the submission.
     */
    public List<DocumentSubmitResult> submitDocument(DocumentSubmit document) {
        return restClient.patch()
                .uri(taskProperties.submitUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DocumentSubmitResult>>() {
                });
    }

    /**
     * Approves a document by sending a PATCH request to the configured approval
     * URL.
     *
     * This method sends a PATCH request with the provided document approval data in
     * JSON format to the approval endpoint. It retrieves and returns a list of results
     * indicating the status of the document approval process.
     *
     * @param document The {@link DocumentApproval} object containing the data
     *                 required for approval.
     * @return A list of {@link DocumentApprovalResult} objects representing the
     *         results of the approval process.
     */
    public List<DocumentApprovalResult> approveDocument(DocumentApproval document) {
        return restClient.patch()
                .uri(taskProperties.approvalUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .body(document)
                .retrieve()
                .body(new ParameterizedTypeReference<List<DocumentApprovalResult>>() {
                });
    }

}
