package com.kalalev.sports.service.impl;

import com.kalalev.sports.exception.NotFoundException;
import com.kalalev.sports.model.Field;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.service.FieldService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FieldServiceImpl implements FieldService {
    private final FieldRepository fieldRepository;

    public FieldServiceImpl(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
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

    @Override
    public void deleteFieldById(int fieldId) {
        fieldRepository.delete(getFieldById(fieldId));
    }
}
