package com.kalalev.sports.repository;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
}
