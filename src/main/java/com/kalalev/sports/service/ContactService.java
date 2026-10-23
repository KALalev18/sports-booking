package com.kalalev.sports.service;

import com.kalalev.sports.model.Contact;
import com.kalalev.sports.model.User;
import java.util.List;

public interface ContactService {
    List<Contact> getAllContacts();

    void createContact(Contact contact, User user);

    void deleteContactById(int contactId);
}
