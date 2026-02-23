package com.example.generator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import com.example.common.dto.DocumentApproval;
import com.example.common.dto.DocumentApprovalResult;
import com.example.common.dto.DocumentCreateDTO;
import com.example.common.dto.DocumentSubmit;
import com.example.generator.config.TaskProperties;
import com.example.generator.job.DocumentTasks;
import com.example.generator.job.GeneratorRunner;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RestClientTest(DocumentApiClient.class)
public class DocumentApiClientTest {

    @MockitoBean
    DocumentTasks tasks;

    @MockitoBean
    GeneratorRunner runner;

    @Autowired
    MockRestServiceServer mockServer;

    @Autowired
    DocumentApiClient client;

    @Autowired
    TaskProperties taskProperties;

    static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateDocument() {

        var document = Instancio.create(DocumentCreateDTO.class);

        mockServer.expect(requestTo(taskProperties.baseUrl() + taskProperties.creatingUrl()))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        var result = client.createDocument(document);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void testApproveDocument() throws JsonProcessingException {

        var document = Instancio.create(DocumentApproval.class);
        var expected = Instancio.ofList(DocumentApprovalResult.class).size(1).create();

        mockServer.expect(requestTo(taskProperties.baseUrl() + taskProperties.approvalUrl()))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        var result = client.approveDocument(document);
        Assertions.assertThat(expected).usingRecursiveFieldByFieldElementComparator().isEqualTo(result);
    }

    @Test
    void testSubmitDocument() throws JsonProcessingException {

        var document = Instancio.create(DocumentSubmit.class);
        var expected = Instancio.ofList(DocumentApprovalResult.class).size(1).create();

        mockServer.expect(requestTo(taskProperties.baseUrl() + taskProperties.submitUrl()))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected), MediaType.APPLICATION_JSON));

        var result = client.submitDocument(document);
        Assertions.assertThat(expected).usingRecursiveFieldByFieldElementComparator().isEqualTo(result);
    }
}
