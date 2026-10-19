package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.DemandPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DemandPredictionRepository extends JpaRepository<DemandPrediction, Long> {

    List<DemandPrediction> findByProductIdOrderByCreatedAtDesc(Long productId);

    Optional<DemandPrediction> findFirstByProductIdOrderByCreatedAtDesc(Long productId);
}