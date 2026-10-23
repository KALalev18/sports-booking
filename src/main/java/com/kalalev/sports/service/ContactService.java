package com.kalalev.sports.service;

import com.kalalev.sports.model.Contact;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ContactService {
    List<Contact> getAllContacts();
    Contact getContactById(int contactId);
    void createContact(Contact contact);
    void updateContact(Contact contact);
    void deleteContactById(int contactId);
}
