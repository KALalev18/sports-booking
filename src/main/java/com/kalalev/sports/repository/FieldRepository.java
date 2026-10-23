package com.kalalev.sports.repository;

import com.kalalev.sports.model.Field;
import com.kalalev.sports.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldRepository extends JpaRepository<Field, Integer> {
}
