package com.example.management.mapper;

import org.mapstruct.Mapper;

import com.example.management.dto.DocumentCreateDTO;
import com.example.management.dto.DocumentReadDTO;
import com.example.management.dto.DocumentReadExtendedDTO;
import com.example.management.entity.DocumentEntity;
import com.example.management.entity.DocumentStatusHistoryEntity;
import com.example.management.entity.enums.Action;
import com.example.management.entity.enums.DocumentStatus;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocumentReadDTO entityToDTO(DocumentEntity documentEntity);

    DocumentReadExtendedDTO entityToExtendedDTO(DocumentEntity documentEntity);

    default DocumentEntity createDtoToEntity(DocumentCreateDTO createDto) {
        DocumentEntity entity = new DocumentEntity();
        entity.setAuthor(createDto.author());
        entity.setTitle(createDto.title());
        entity.setStatus(DocumentStatus.DRAFT);
        return entity;
    }

    default DocumentEntity changeStatus(DocumentEntity entity, DocumentStatus newStatus, String initiator, String comment) {

        Action action;
        switch (newStatus) {
            case SUBMITTED -> action = Action.SUBMIT;
            case APPROVED -> action = Action.APPROVE;
            default -> throw new IllegalArgumentException("Invalid status");
        }

        var history = new DocumentStatusHistoryEntity();
        history.setAction(action);
        history.setInitiator(initiator);
        history.setComment(comment);
        history.setDocument(entity);

        entity.setStatus(newStatus);
        entity.getStatusHistory().add(history);

        return entity;
    }

}
