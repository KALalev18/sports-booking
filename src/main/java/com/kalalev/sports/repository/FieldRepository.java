package com.kalalev.sports.repository;

import com.kalalev.sports.model.Field;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field, Integer> {
    List<Field> findByFieldNameContainingIgnoreCaseOrFieldLocationContainingIgnoreCase(String name, String location);
}
