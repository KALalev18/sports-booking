package com.kalalev.sports.repository;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
}
