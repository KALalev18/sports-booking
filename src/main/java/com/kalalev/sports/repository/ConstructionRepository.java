package com.kalalev.sports.repository;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.Field;
import com.kalalev.sports.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructionRepository extends JpaRepository<Construction, Integer> {
    List<Construction> findAllByOrderByCreatedAtDesc();

    List<Construction> findByField(Field field);

    void deleteByUser(User user);
}
