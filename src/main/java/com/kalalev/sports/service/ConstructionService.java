package com.kalalev.sports.service;

import com.kalalev.sports.model.Construction;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ConstructionService {
    List<Construction> getAllConstructions();
    Construction getConstructionById(int constructionId);
    void createConstruction(Construction construction);
    void updateConstruction(Construction construction);
    void deleteConstructionById(int constructionId);
}
