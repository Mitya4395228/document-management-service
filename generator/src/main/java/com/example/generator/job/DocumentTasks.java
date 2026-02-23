package com.example.generator.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.common.dto.DocumentApproval;
import com.example.common.dto.DocumentApprovalResult;
import com.example.common.dto.DocumentCreateDTO;
import com.example.common.dto.DocumentSubmit;
import com.example.common.dto.DocumentSubmitResult;
import com.example.common.dto.enums.DocumentStatus;
import com.example.common.dto.enums.ResultType;
import com.example.generator.config.TaskProperties;
import com.example.generator.service.DocumentApiClient;
import com.example.generator.service.DocumentService;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

/**
 * Tasks for generating and processing documents.
 */
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class DocumentTasks {

    DocumentService service;

    DocumentApiClient client;

    TaskProperties taskProperties;

    public DocumentTasks(DocumentService service, DocumentApiClient client, TaskProperties taskProperties) {
        this.service = service;
        this.client = client;
        this.taskProperties = taskProperties;
    }

    /**
     * Generates a specified number of documents by calling an external client API.
     * The process is logged with progress updates and final statistics including
     * the total number of documents created, successful creations, errors, and duration.
     *
     * This method retrieves the total number of documents to generate from task properties,
     * iterates through the creation process, and logs progress at intervals. It also tracks
     * the number of successful and failed document creations.
     */
    @Async
    public void generateDocuments() {
    
            // Retrieve the total number of documents to generate and calculate the logging step interval
            var total = taskProperties.generatingSize();
            var step = total / 10;
    
            // Log the start of the document generation process
            log.info("Start creating {} documents", total);
    
            // Initialize timing and counters for tracking success and error counts
            var start = System.currentTimeMillis();
            var success = 0;
            var error = 0;
    
            // Iterate through the total number of documents to be generated
            for (int i = 0; i < total; i++) {
    
                // Create a document creation request with a unique title
                var request = new DocumentCreateDTO("Generator", "Title - " + i);
                
                // Send the request to the client and check the response status
                var response = client.createDocument(request);
                if (response.getStatusCode().is2xxSuccessful()) {
                    success++; // Increment success counter on successful response
                } else {
                    error++; // Increment error counter on unsuccessful response
                }
    
                // Log progress at regular intervals based on the calculated step
                if (i % step == 0) {
                    log.info("Created documents {} from {}. Duration: {} milliseconds.", i, total, duration(start));
                }
            }
    
            // Log the completion of the document generation process with final statistics
            log.info(
                    "Generating task finished. Total created documents {}. Success: {}. Error: {}. Duration: {} milliseconds.",
                    total, success, error, duration(start));
        }

    /**
     * This method is scheduled to run periodically to submit draft documents for processing in batches.
     * 
     * This method retrieves the total number of draft documents and processes them in batches
     * based on the configured batch size. Each batch is submitted to a client service, and
     * the results are aggregated and logged. The method ensures that all draft documents are
     * processed and provides detailed logging of the submission progress and final results.
     * 
     */
    @Scheduled(initialDelayString = "${app.submit-initial-delay}", fixedDelayString = "${app.submit-delay}")
    public void submitWorker() {
    
        // Retrieve the total count of draft documents
        var total = service.count(DocumentStatus.DRAFT);
    
        // If no documents are found, log and exit
        if (total == 0L) {
            log.info("No documents to submit");
        } else {
    
            // Log the start of the submission process
            log.info("Start submitting {} documents", total);
    
            // Initialize variables for tracking progress and results
            var start = System.currentTimeMillis();
            var count = 0;
            var result = new HashMap<ResultType, Integer>();
    
            // Initialize result map with all ResultType values except REGISTRY_REGISTRATION_ERROR
            Stream.of(ResultType.values()).filter(t -> !t.equals(ResultType.REGISTRY_REGISTRATION_ERROR))
                    .forEach(t -> result.put(t, 0));
    
            // Process documents in batches until all are submitted
            while (count < total) {
    
                // Determine the size of the current batch
                var size = total - count > taskProperties.batchSize() ? taskProperties.batchSize() : total - count;
    
                // Retrieve IDs of draft documents for the current batch
                var ids = service.findIds(DocumentStatus.DRAFT, (int) size);
                if (ids.isEmpty()) {
                    break; // Exit loop if no more documents are found
                }
                count += ids.size();
    
                // Create and submit the document submission request
                var request = new DocumentSubmit(ids, "Generator", "Generator");
                var response = client.submitDocument(request);
    
                // Aggregate results from the response
                countResult(result, response.stream().map(DocumentSubmitResult::message).toList());
    
                // Log the progress of the submission
                log.info("Submited documents {} from {}. Duration: {} milliseconds.", count, total, duration(start));
            }
    
            // Log the completion of the submission task with final statistics
            log.info("Submit task finished. Total processed documents {}. {}. Duration: {} milliseconds.", total,
                    resultToString(result), duration(start));
        }
    }

    /**
     * This method is scheduled to run periodically to approve submitted documents.
     * It retrieves documents with the status 'SUBMITTED', processes them in batches,
     * and sends approval requests to a client service. The results are logged along
     * with performance metrics such as duration and counts of different result types.
     *
     */
    @Scheduled(initialDelayString = "${app.approval-initial-delay}", fixedDelayString = "${app.approval-delay}")
    public void approveWorker() {
    
        // Count the total number of documents with status 'SUBMITTED'
        var total = service.count(DocumentStatus.SUBMITTED);
    
        // If no documents are found, log and exit
        if (total == 0L) {
            log.info("No documents to approve");
        } else {
    
            // Log the start of the approval process
            log.info("Start approving {} documents", total);
    
            // Initialize variables for tracking progress and results
            var start = System.currentTimeMillis();
            var count = 0;
            var result = new HashMap<ResultType, Integer>();
            Stream.of(ResultType.values()).forEach(t -> result.put(t, 0));
    
            // Process documents in batches until all are handled
            while (count < total) {
    
                // Determine the batch size for the current iteration
                var size = total - count > taskProperties.batchSize() ? taskProperties.batchSize() : total - count;
    
                // Retrieve IDs of documents to process in this batch
                var ids = service.findIds(DocumentStatus.SUBMITTED, (int) size);
                if (ids.isEmpty()) {
                    break; // Exit loop if no more IDs are found
                }
                count += ids.size();
    
                // Create an approval request and send it to the client service
                var request = new DocumentApproval(ids, "Generator", "Generator");
                var response = client.approveDocument(request);
    
                // Update result counts based on the response
                countResult(result, response.stream().map(DocumentApprovalResult::message).toList());
    
                // Log progress after each batch
                log.info("Approved documents {} from {}. Duration: {} milliseconds.", count, total, duration(start));
            }
    
            // Log final summary of the approval task
            log.info("Approval task finished. Total processed documents {}. {}. Duration: {} milliseconds.", total,
                    resultToString(result), duration(start));
        }
    }

    private String resultToString(Map<ResultType, Integer> result) {
        return result.entrySet().stream()
                .map(e -> String.format("%s: %d", e.getKey(), e.getValue()))
                .collect(Collectors.joining(", "));
    }

    private void countResult(Map<ResultType, Integer> result, List<ResultType> response) {
        for (var r : response) {
            var v = result.get(r);
            if (v != null) {
                result.put(r, v + 1);
            } else {
                result.put(r, 1);
            }
        }
    }

    private long duration(long start) {
        return System.currentTimeMillis() - start;
    }

}
