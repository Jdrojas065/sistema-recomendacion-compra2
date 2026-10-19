package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.Lot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findByProductIdAndActiveTrue(Long productId);

    boolean existsByProductIdAndLotNumber(Long productId, String lotNumber);

    Optional<Lot> findByProductIdAndLotNumberAndActiveTrue(Long productId, String lotNumber);
}