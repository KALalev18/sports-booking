package com.kalalev.sports.service;

import com.kalalev.sports.model.Field;
import java.util.List;

public interface FieldService {
    List<Field> getAllFields();

    Field getFieldById(int fieldId);

    List<Field> searchFields(String query);

    void saveField(Field field);

    void deleteFieldById(int fieldId);
}
