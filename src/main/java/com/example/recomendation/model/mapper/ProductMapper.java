package com.example.recomendation.model.mapper;

import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.form.ProductFormDto;

public class ProductMapper {

    private ProductMapper() {
        // clase utilitaria, no se instancia
    }

    public static ProductFormDto toForm(Product product) {
        if (product == null) {
            return null;
        }
        ProductFormDto form = new ProductFormDto();
        form.setId(product.getId());
        form.setName(product.getName());
        form.setPrice(product.getPrice());
        form.setCategoryId(product.getCategory() != null ? product.getCategory().getId() : null);
        form.setMinimumStock(product.getMinimumStock());
        form.setBarcode(product.getBarcode());
        return form;
    }

    /**
     * Aplica los datos del formulario sobre una entidad existente (o nueva).
     * No toca currentStock ni version — esos campos los gestiona InventoryService,
     * nunca el formulario de edición de producto.
     */
    public static void applyForm(Product product, ProductFormDto form) {
        product.setName(form.getName());
        product.setPrice(form.getPrice());
        product.setMinimumStock(form.getMinimumStock());
        product.setBarcode(
                org.springframework.util.StringUtils.hasText(form.getBarcode()) ? form.getBarcode().trim() : null);
        // category se asigna en el service, donde sí se tiene acceso al CategoryRepository
    }
}