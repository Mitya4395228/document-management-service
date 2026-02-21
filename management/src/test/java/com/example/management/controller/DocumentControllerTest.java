package com.example.management.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@WebMvcTest(DocumentController.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DocumentService service;

    static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    @Test
    void testGetDocument() throws Exception {

        var response = Instancio.create(DocumentReadExtendedDTO.class);
        var id = response.id();

        when(service.getDocument(any(UUID.class))).thenReturn(response);

        var resultJson = mockMvc.perform(get("/api/v1/documents/{id}", id))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, DocumentReadExtendedDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).getDocument(any(UUID.class));
    }

    @Test
    void testGetDocuments() throws Exception {

        var pack = new DocumentBatchReceipt(Set.of(UUID.randomUUID()));
        var pageable = PageRequest.of(0, 20);
        var content = Instancio.ofList(DocumentReadDTO.class).size(1).create();
        var response = new PagedModel<DocumentReadDTO>(new PageImpl<>(content, pageable, content.size()));

        when(service.getDocuments(pack, pageable)).thenReturn(response);

        var resultJson = mockMvc.perform(
                get("/api/v1/documents/package")
                        .param("ids", pack.ids().stream().map(UUID::toString).toArray(String[]::new))
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);
        var resultContent = objectMapper.convertValue(result.get("content"),
                new TypeReference<List<DocumentReadDTO>>() {
                });

        Assertions.assertThat(resultContent).isEqualTo(content);
        verify(service, times(1)).getDocuments(pack, pageable);
    }

    @Test
    void testGetDocuments_Validation() throws Exception {

        var pack = new DocumentBatchReceipt(Set.of());
        var pageable = PageRequest.of(0, 20);
        assertTrue(pack.ids().isEmpty());

        mockMvc.perform(
                get("/api/v1/documents/package")
                        .param("ids", pack.ids().stream().map(UUID::toString).toArray(String[]::new))
                        .param("page", String.valueOf(pageable.getPageNumber()))
                        .param("size", String.valueOf(pageable.getPageSize())))
                .andExpect(status().isBadRequest());

        verify(service, never()).getDocuments(pack, pageable);
    }

    @Test
    void testGetDocumentsByFilter() throws Exception {

        var filter = Instancio.create(DocumentFilter.class);
        var pageable = PageRequest.of(0, 20);
        var content = Instancio.ofList(DocumentReadDTO.class).size(3).create();
        var response = new PagedModel<DocumentReadDTO>(new PageImpl<>(content, pageable, content.size()));

        when(service.getAllByFilter(filter, pageable)).thenReturn(response);

        var resultJson = mockMvc.perform(
                get("/api/v1/documents/filter")
                        .param("author", filter.author() != null ? filter.author() : "")
                        .param("title", filter.title() != null ? filter.title() : "")
                        .param("status", filter.status() != null ? filter.status().toString() : "")
                        .param("createdFrom", filter.createdFrom() != null ? filter.createdFrom().toString() : "")
                        .param("createdTo", filter.createdTo() != null ? filter.createdTo().toString() : ""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> result = objectMapper.readValue(resultJson, Map.class);
        var resultContent = objectMapper.convertValue(result.get("content"),
                new TypeReference<List<DocumentReadDTO>>() {
                });

        Assertions.assertThat(resultContent).isEqualTo(content);
        verify(service, times(1)).getAllByFilter(filter, pageable);
    }

    @Test
    void testCreate() throws Exception {

        var request = Instancio.create(DocumentCreateDTO.class);
        var response = Instancio.create(DocumentReadDTO.class);

        when(service.create(request)).thenReturn(response);

        var resultJson = mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, DocumentReadDTO.class);
        assertEquals(response, result);
        verify(service, times(1)).create(request);
    }

    @Test
    void testCreate_Validation() throws Exception {

        var request = new DocumentCreateDTO("", "");

        mockMvc.perform(post("/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(request);
    }

    @Test
    void testApprove() throws Exception {

        var request = Instancio.create(DocumentSubmit.class);
        var response = Instancio.ofList(DocumentSubmitResult.class).size(5).create();

        when(service.submit(request)).thenReturn(response);

        var resultJson = mockMvc.perform(patch("/api/v1/documents/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, new TypeReference<List<DocumentSubmitResult>>() {
        });
        assertEquals(result, response);
        verify(service, times(1)).submit(request);
    }

    @Test
    void testApprove_Validation() throws Exception {

        var request = new DocumentSubmit(Set.of(), "", "");

        mockMvc.perform(patch("/api/v1/documents/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).submit(request);
    }

    @Test
    void testSubmit() throws Exception {

        var request = Instancio.create(DocumentApproval.class);
        var response = Instancio.ofList(DocumentApprovalResult.class).size(1).create();

        when(service.approve(request)).thenReturn(response);

        var resultJson = mockMvc.perform(patch("/api/v1/documents/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, new TypeReference<List<DocumentApprovalResult>>() {
        });
        assertEquals(response, result);
        verify(service, times(1)).approve(request);
    }

    @Test
    void testSubmit_Validation() throws Exception {

        var request = new DocumentApproval(Set.of(), "", "");

        mockMvc.perform(patch("/api/v1/documents/approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).approve(request);
    }

    @Test
    void testPersistentApprove() throws Exception {

        var request = Instancio.of(DocumentPersistentApproval.class)
                .generate(Select.field("threads"), gen -> gen.ints().min(1))
                .generate(Select.field("attempts"), gen -> gen.ints().min(1))
                .create();
        var response = Instancio.create(DocumentPersistentApprovalResult.class);

        when(service.persistentApprove(request)).thenReturn(response);

        var resultJson = mockMvc.perform(patch("/api/v1/documents/persistent-approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var result = objectMapper.readValue(resultJson, DocumentPersistentApprovalResult.class);
        Assertions.assertThat(response).usingRecursiveComparison().isEqualTo(result);
        verify(service, times(1)).persistentApprove(request);
    }

    @Test
    void testPersistentApprove_Validation() throws Exception {

        var request = new DocumentPersistentApproval(null, "", "", -5, -6);

        mockMvc.perform(patch("/api/v1/documents/persistent-approval")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(service, never()).persistentApprove(request);
    }

}
