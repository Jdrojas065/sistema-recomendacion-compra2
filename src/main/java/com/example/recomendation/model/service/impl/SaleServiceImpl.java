package com.example.recomendation.model.service.impl;


import com.example.recomendation.exception.BusinessException;
import com.example.recomendation.exception.OptimisticLockConflictException;
import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.*;
import com.example.recomendation.model.enums.MovementType;
import com.example.recomendation.model.form.SaleCartFormDto;
import com.example.recomendation.model.repository.*;
import com.example.recomendation.model.service.interfaces.SaleService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryMovementRepository movementRepository;
    private final com.example.recomendation.model.service.interfaces.AnomalyDetectionService anomalyDetectionService;
    private final com.example.recomendation.model.service.interfaces.RecommendationService recommendationService;
    

    @Override
    @Transactional
    public Sale confirmSale(List<SaleCartFormDto> cartItems, String username) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("El carrito está vacío, agrega al menos un producto");
        }

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Sale sale = Sale.builder()
                .user(user)
                .date(LocalDateTime.now())
                .total(BigDecimal.ZERO)
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (SaleCartFormDto item : cartItems) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductId()));

            if (product.getCurrentStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Stock insuficiente para el producto: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);

            SaleDetail detail = SaleDetail.builder()
                    .sale(sale)
                    .product(product)
                    .quantity(item.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();
            sale.getDetails().add(detail);

            product.setCurrentStock(product.getCurrentStock() - item.getQuantity());
            try {
                productRepository.save(product);
            } catch (ObjectOptimisticLockingFailureException ex) {
                throw new OptimisticLockConflictException(
                        "El stock de '" + product.getName() + "' cambió mientras confirmabas la venta. Verifica el carrito e intenta de nuevo.");
            }

            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .type(MovementType.OUT)
                    .quantity(item.getQuantity())
                    .date(LocalDateTime.now())
                    .user(user)
                    .description("Salida automática por venta")
                    .build();
            movementRepository.save(movement);
            // RF13: evalúa si esta venta se aleja del patrón histórico del producto.
            anomalyDetectionService.evaluateSale(product, item.getQuantity());

            // Recalcula la recomendación de este producto con el stock y las
            // anomalías más recientes. No debe hacer fallar la venta si algo sale mal.
            try {
                recommendationService.generateRecommendation(product.getId());
            } catch (Exception ex) {
                org.slf4j.LoggerFactory.getLogger(getClass())
                        .warn("No se pudo recalcular la recomendación del producto {}: {}",
                                product.getId(), ex.getMessage());
            }
        }

        sale.setTotal(total);
        return saleRepository.save(sale);
    }

    @Override
    public Page<Sale> findAll(Pageable pageable, Long productId, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return saleRepository.findByDateBetweenOrderByDateDesc(
                    startDate.atStartOfDay(), endDate.atTime(23, 59, 59), pageable);
        }
        return saleRepository.findAllByOrderByDateDesc(pageable);
        // Nota: el filtro por productId se aplica en el controller sobre el resultado,
        // o se puede extender el repository con una consulta @Query si se requiere a nivel de BD.
    }

    @Override
    public Page<Sale> findByUser(String username, Pageable pageable) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return saleRepository.findByUserIdOrderByDateDesc(user.getId(), pageable);
    }

    @Override
    public Sale findById(Long id) {
        return saleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con id: " + id));
    }
    
    @Override
    public java.util.List<DailySalesTotal> getMonthlySales(java.time.YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.atEndOfMonth().atTime(23, 59, 59);
        List<Sale> sales = saleRepository.findByDateBetween(start, end);

        java.util.Map<Integer, BigDecimal> totalsByDay = sales.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getDate().getDayOfMonth(),
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, Sale::getTotal, BigDecimal::add)));

        int daysInMonth = month.lengthOfMonth();
        java.util.List<DailySalesTotal> result = new java.util.ArrayList<>();
        for (int day = 1; day <= daysInMonth; day++) {
            result.add(new DailySalesTotal(day, totalsByDay.getOrDefault(day, BigDecimal.ZERO)));
        }
        return result;
    }

    @Override
    public java.util.List<MonthlySalesTotal> getAnnualSales(int year) {
        LocalDateTime start = java.time.LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = java.time.LocalDate.of(year, 12, 31).atTime(23, 59, 59);
        List<Sale> sales = saleRepository.findByDateBetween(start, end);

        java.util.Map<Integer, BigDecimal> totalsByMonth = sales.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getDate().getMonthValue(),
                        java.util.stream.Collectors.reducing(BigDecimal.ZERO, Sale::getTotal, BigDecimal::add)));

        java.util.List<MonthlySalesTotal> result = new java.util.ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(new MonthlySalesTotal(m, totalsByMonth.getOrDefault(m, BigDecimal.ZERO)));
        }
        return result;
    }

    @Override
    public boolean canView(Sale sale, String username, boolean isAdmin) {
        if (isAdmin) {
            return true;
        }
        return sale.getUser().getEmail().equalsIgnoreCase(username);
    }
}