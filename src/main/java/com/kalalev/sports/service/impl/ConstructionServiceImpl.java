package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.User;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.service.ConstructionService;
import com.kalalev.sports.service.FieldService;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ConstructionServiceImpl implements ConstructionService {
    private final ConstructionRepository constructionRepository;
    private final FieldService fieldService;
    private final Clock clock;

    public ConstructionServiceImpl(
            ConstructionRepository constructionRepository, FieldService fieldService, Clock clock) {
        this.constructionRepository = constructionRepository;
        this.fieldService = fieldService;
        this.clock = clock;
    }

    @Override
    public List<Construction> getAllConstructions() {
        return constructionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public void createConstruction(Construction construction, User user, Integer fieldId) {
        construction.setUser(user);
        construction.setField(fieldId == null ? null : fieldService.getFieldById(fieldId));
        construction.setCreatedAt(LocalDateTime.now(clock));
        constructionRepository.save(construction);
    }

    @Override
    public void deleteConstructionById(int constructionId) {
        constructionRepository.deleteById(constructionId);
    }
}
