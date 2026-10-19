package com.example.recomendation.web;

import com.example.recomendation.model.form.SaleCartFormDto;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mantiene el carrito de venta del usuario actual durante su sesión HTTP.
 * Se limpia automáticamente al confirmar la venta o al cerrar sesión.
 */
@Component
@SessionScope
public class SaleCartSession {

    private final List<SaleCartFormDto> items = new ArrayList<>();

    public List<SaleCartFormDto> getItems() {
        return items;
    }

    public void addItem(SaleCartFormDto item) {
        items.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(SaleCartFormDto::getSubtotal)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}