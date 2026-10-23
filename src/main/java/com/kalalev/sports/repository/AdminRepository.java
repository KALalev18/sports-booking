package com.kalalev.sports.repository;

import com.kalalev.sports.model.Admin;
import com.kalalev.sports.model.Construction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
}
