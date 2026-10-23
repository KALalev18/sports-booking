package com.kalalev.sports.repository;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    List<Contact> findAllByOrderByCreatedAtDesc();

    void deleteByUser(User user);
}
