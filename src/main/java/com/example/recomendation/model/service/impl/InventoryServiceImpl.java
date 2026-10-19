package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.BusinessException;
import com.example.recomendation.exception.OptimisticLockConflictException;
import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.InventoryMovement;
import com.example.recomendation.model.entity.Lot;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.enums.MovementType;
import com.example.recomendation.model.form.InventoryMovementFormDto;
import com.example.recomendation.model.repository.InventoryMovementRepository;
import com.example.recomendation.model.repository.LotRepository;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.repository.UserRepository;
import com.example.recomendation.model.service.interfaces.InventoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository movementRepository;
    private final UserRepository userRepository;
    private final LotRepository lotRepository;

    @Override
    public Page<Product> findStock(Pageable pageable, Long categoryId) {
        if (categoryId != null) {
            return productRepository.findByActiveTrueAndCategoryId(categoryId, pageable);
        }
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public Product findStockByProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));
    }

    @Override
    @Transactional
    public InventoryMovement registerMovement(InventoryMovementFormDto form, String username) {
        Product product = productRepository.findById(form.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        LocalDate movementDay = form.getMovementDate() != null ? form.getMovementDate() : LocalDate.now();
        if (movementDay.isAfter(LocalDate.now())) {
            throw new BusinessException("No se pueden registrar movimientos con una fecha futura");
        }
        LocalDateTime movementDateTime = movementDay.isEqual(LocalDate.now())
                ? LocalDateTime.now()
                : movementDay.atTime(LocalTime.NOON);

        int currentStock = product.getCurrentStock();
        int newStock;

        if (form.getType() == MovementType.IN) {
            newStock = currentStock + form.getQuantity();
        } else {
            newStock = currentStock - form.getQuantity();
            if (newStock < 0) {
                throw new BusinessException("Stock insuficiente para realizar la operación");
            }
        }

        product.setCurrentStock(newStock);

        try {
            productRepository.save(product); // aquí puede saltar el conflicto de @Version
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new OptimisticLockConflictException(
                    "El stock del producto cambió mientras procesabas esta operación. Verifica el stock actual e intenta de nuevo.");
        }

        Lot lot = resolveLot(product, form, movementDay);

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .type(form.getType())
                .quantity(form.getQuantity())
                .date(movementDateTime)
                .lot(lot)
                .user(user)
                .description(form.getDescription())
                .build();

        return movementRepository.save(movement);
    }

    /**
     * Crea o actualiza el lote asociado a un movimiento, cuando se indica número de lote.
     * - En una ENTRADA (IN): si el lote ya existe (activo) se le suma la cantidad y se
     *   actualiza la fecha de vencimiento si se indicó una nueva; si no existe, se crea.
     * - En una SALIDA (OUT): si el lote existe, se le resta cantidad (sin bajar de 0).
     * Si no se indica número de lote, no se toca nada (movimiento sin lote asociado).
     */
    private Lot resolveLot(Product product, InventoryMovementFormDto form, LocalDate movementDay) {
        if (!StringUtils.hasText(form.getLotNumber())) {
            return null;
        }

        Lot lot = lotRepository.findByProductIdAndLotNumberAndActiveTrue(product.getId(), form.getLotNumber())
                .orElse(null);

        if (form.getType() == MovementType.IN) {
            if (lot == null) {
                lot = Lot.builder()
                        .product(product)
                        .lotNumber(form.getLotNumber())
                        .quantity(form.getQuantity())
                        .entryDate(movementDay)
                        .expirationDate(form.getExpirationDate())
                        .active(true)
                        .build();
            } else {
                lot.setQuantity(lot.getQuantity() + form.getQuantity());
                if (form.getExpirationDate() != null) {
                    lot.setExpirationDate(form.getExpirationDate());
                }
            }
        } else if (lot != null) {
            lot.setQuantity(Math.max(0, lot.getQuantity() - form.getQuantity()));
        }

        return lot != null ? lotRepository.save(lot) : null;
    }

    @Override
    public Page<InventoryMovement> findHistoryByProduct(Long productId, Pageable pageable) {
        return movementRepository.findByProductIdOrderByDateDesc(productId, pageable);
    }
}