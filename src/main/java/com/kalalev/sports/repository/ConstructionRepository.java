package com.kalalev.sports.repository;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConstructionRepository extends JpaRepository<Construction, Integer> {
}
