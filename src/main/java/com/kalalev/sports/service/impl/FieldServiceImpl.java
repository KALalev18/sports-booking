package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Field;
import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.service.FieldService;

import java.util.List;
import java.util.Optional;

@Service
public class FieldServiceImpl implements FieldService {
    @Autowired
    private FieldRepository fieldRepository;
    @Override
    public List<Field> getAllFields() {
        return fieldRepository.findAll();
    }

    @Override
    public Field getFieldById(int fieldId) {
        Optional<Field> field = fieldRepository.findById(fieldId);
        return field.orElse(null);
    }

    @Override
    public void createField(Field field) {
        fieldRepository.save(field);
    }

    @Override
    public void updateField(Field field) {
        fieldRepository.save(field);
    }

    @Override
    public void deleteFieldById(int fieldId) {
        fieldRepository.deleteById(fieldId);
    }
}
