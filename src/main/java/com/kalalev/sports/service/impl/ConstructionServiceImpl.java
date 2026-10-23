package com.kalalev.sports.service.impl;

import com.kalalev.sports.model.Construction;
import com.kalalev.sports.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kalalev.sports.repository.ConstructionRepository;
import com.kalalev.sports.repository.OrderRepository;
import com.kalalev.sports.service.ConstructionService;

import java.util.List;
import java.util.Optional;

@Service
public class ConstructionServiceImpl implements ConstructionService {
    @Autowired
    private ConstructionRepository constructionRepository;
    @Override
    public List<Construction> getAllConstructions() {
        return constructionRepository.findAll();
    }

    @Override
    public Construction getConstructionById(int constructionId) {
        Optional<Construction> construction = constructionRepository.findById(constructionId);
        return construction.orElse(null);
    }

    @Override
    public void createConstruction(Construction construction) {
        constructionRepository.save(construction);
    }

    @Override
    public void updateConstruction(Construction construction) {
        constructionRepository.save(construction);
    }

    @Override
    public void deleteConstructionById(int constructionId) {
        constructionRepository.deleteById(constructionId);
    }
}
