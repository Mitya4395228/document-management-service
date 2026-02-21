package com.example.management.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.example.management.dto.DocumentFilter;
import com.example.management.dto.DocumentReadDTO;

public interface DocumentRepositoryCustom {

    PagedModel<DocumentReadDTO> finadAllByFilter(DocumentFilter filter, Pageable pageable);

}
