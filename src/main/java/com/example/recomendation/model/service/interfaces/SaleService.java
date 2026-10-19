package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.Sale;
import com.example.recomendation.model.form.SaleCartFormDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SaleService {

    /**
     * Confirma una venta a partir del carrito acumulado en sesión.
     * Valida stock, descuenta inventario y registra el movimiento OUT,
     * todo dentro de una transacción.
     */
    Sale confirmSale(List<SaleCartFormDto> cartItems, String username);

    Page<Sale> findAll(Pageable pageable, Long productId, LocalDate startDate, LocalDate endDate);

    Page<Sale> findByUser(String username, Pageable pageable);

    Sale findById(Long id);

    /** Verifica si el usuario dado puede ver esta venta (dueño o ADMIN). */
    boolean canView(Sale sale, String username, boolean isAdmin);
    

    /** Total vendido por cada día del mes indicado (incluye días sin ventas, en 0). */
    java.util.List<DailySalesTotal> getMonthlySales(java.time.YearMonth month);

    /** Total vendido por cada mes del año indicado (incluye meses sin ventas, en 0). */
    java.util.List<MonthlySalesTotal> getAnnualSales(int year);

    record DailySalesTotal(int day, java.math.BigDecimal total) {}

    record MonthlySalesTotal(int month, java.math.BigDecimal total) {}
}