package com.kalalev.sports.service.impl;

import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.Field;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.service.FieldService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FieldServiceImpl implements FieldService {
    private final FieldRepository fieldRepository;
    private final ConstructionRepository constructionRepository;

    public FieldServiceImpl(FieldRepository fieldRepository, ConstructionRepository constructionRepository) {
        this.fieldRepository = fieldRepository;
        this.constructionRepository = constructionRepository;
    }

    @Override
    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    @Override
    public Field getFieldById(int fieldId) {
        return fieldRepository
                .findById(fieldId)
                .orElseThrow(() -> new NotFoundException("Facility " + fieldId + " not found"));
    }

    @Override
    public List<Field> searchFields(String query) {
        return fieldRepository.findByFieldNameContainingIgnoreCaseOrFieldLocationContainingIgnoreCase(query, query);
    }

    @Override
    public void saveField(Field field) {
        fieldRepository.save(field);
    }

    // Construction requests for this facility are kept but no longer point to it
    @Override
    @Transactional
    public void deleteFieldById(int fieldId) {
        Field field = getFieldById(fieldId);
        for (Construction construction : constructionRepository.findByField(field)) {
            construction.setField(null);
        }
        fieldRepository.delete(field);
    }
}
