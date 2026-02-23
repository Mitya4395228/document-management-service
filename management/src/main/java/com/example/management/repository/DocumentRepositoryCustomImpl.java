package com.example.management.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;

import com.example.common.dto.DocumentFilter;
import com.example.common.dto.DocumentReadDTO;
import com.example.management.entity.DocumentEntity;
import com.example.management.mapper.DocumentMapper;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DocumentRepositoryCustomImpl implements DocumentRepositoryCustom {

    @PersistenceContext
    EntityManager em;

    @Autowired
    DocumentMapper mapper;

    @Override
    public PagedModel<DocumentReadDTO> finadAllByFilter(DocumentFilter filter, Pageable pageable) {

        var countString = "SELECT COUNT(d) FROM DocumentEntity d WHERE 1=1";
        var queryString = "SELECT d FROM DocumentEntity d WHERE 1=1";
        var qlString = new StringBuilder();
        var parameters = new HashMap<String, Object>();

        if (filter.author() != null) {
            qlString.append(" AND d.author LIKE :author");
            parameters.put("author", "%" + filter.author() + "%");
        }
        if (filter.title() != null) {
            qlString.append(" AND d.title LIKE :title");
            parameters.put("title", "%" + filter.title() + "%");
        }
        if (filter.status() != null) {
            qlString.append(" AND d.status = :status");
            parameters.put("status", filter.status());
        }
        if (filter.createdFrom() != null) {
            qlString.append(" AND d.createdAt >= :createdFrom");
            parameters.put("createdFrom", filter.createdFrom());
        }
        if (filter.createdTo() != null) {
            qlString.append(" AND d.createdAt <= :createdTo");
            parameters.put("createdTo", filter.createdTo());
        }

        var totalElements = count(countString + qlString.toString(), parameters);

        if (pageable.getSort() != null && !pageable.getSort().isEmpty()) {
            qlString.append(" ORDER BY " + pageable.getSort().stream()
                    .map(order -> "d.%s %s".formatted(order.getProperty(), order.getDirection()))
                    .collect(Collectors.joining(", ")));
        }

        var query = em.createQuery(queryString + qlString.toString(), DocumentEntity.class);
        parameters.forEach(query::setParameter);

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize()).setMaxResults(pageable.getPageSize());

        var content = query.getResultList().stream().map(mapper::entityToDTO).toList();

        return new PagedModel<>(new PageImpl<>(content, pageable, totalElements));
    }

    private long count(String qlString, Map<String, Object> parameters) {
        var query = em.createQuery(qlString.toString());
        parameters.forEach(query::setParameter);
        return (long) query.getSingleResult();
    }
}
