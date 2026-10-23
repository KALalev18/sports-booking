package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.ContactRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.service.ContactService;

import java.util.List;
import java.util.Optional;

@Service
public class ContactSericeImpl implements ContactService {
    @Autowired
    private ContactRepository contactRepository;
    @Override
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    @Override
    public Contact getContactById(int contactId) {
        Optional<Contact> contact = contactRepository.findById(contactId);
        return contact.orElse(null);
    }

    @Override
    public void createContact(Contact contact) {
        contactRepository.save(contact);
    }

    @Override
    public void updateContact(Contact contact) {
        contactRepository.save(contact);
    }

    @Override
    public void deleteContactById(int contactId) {
        contactRepository.deleteById(contactId);
    }
}
