package com.kalalev.sports.service;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.User;
import java.util.List;

public interface ConstructionService {
    List<Construction> getAllConstructions();

    void createConstruction(Construction construction, User user, Integer fieldId);

    void deleteConstructionById(int constructionId);
}
