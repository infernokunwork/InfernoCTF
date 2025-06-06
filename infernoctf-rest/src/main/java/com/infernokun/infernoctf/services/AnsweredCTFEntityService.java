package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.models.entities.AnsweredCTFEntity;
import com.infernokun.infernoctf.repositories.AnsweredCTFEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnsweredCTFEntityService {
    private final AnsweredCTFEntityRepository answeredCTFEntityRepository;

    public AnsweredCTFEntityService(AnsweredCTFEntityRepository answeredCTFEntityRepository) {
        this.answeredCTFEntityRepository = answeredCTFEntityRepository;
    }

    public Optional<AnsweredCTFEntity> findByUserIdAndCtfEntityId(String userId, String ctfEntityId) {
        return this.answeredCTFEntityRepository.findByUserIdAndCtfEntityId(userId, ctfEntityId);
    }

    public Optional<AnsweredCTFEntity> saveAnsweredCTFEntity(AnsweredCTFEntity answeredCTFEntity) {
        return Optional.of(this.answeredCTFEntityRepository.save(answeredCTFEntity));
    }
}
