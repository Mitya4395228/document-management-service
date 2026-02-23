package com.example.management.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.example.common.dto.DocumentFilter;
import com.example.common.dto.DocumentReadDTO;

public interface DocumentRepositoryCustom {

    PagedModel<DocumentReadDTO> finadAllByFilter(DocumentFilter filter, Pageable pageable);

}
