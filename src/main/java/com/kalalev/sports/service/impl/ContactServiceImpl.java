package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.ContactRepository;
import com.kalalev.sports.service.ContactService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ContactServiceImpl implements ContactService {
    private final ContactRepository contactRepository;
    private final Clock clock;

    public ContactServiceImpl(ContactRepository contactRepository, Clock clock) {
        this.contactRepository = contactRepository;
        this.clock = clock;
    }

    @Override
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void createContact(Contact contact, User user) {
        contact.setUser(user);
        contact.setCreatedAt(LocalDateTime.now(clock));
        contactRepository.save(contact);
    }

    @Override
    public void deleteContactById(int contactId) {
        contactRepository.deleteById(contactId);
    }
}
