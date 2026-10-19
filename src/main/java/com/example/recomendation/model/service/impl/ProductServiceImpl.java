package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.DuplicateBarcodeException;
import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.Category;
import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.enums.MovementType;
import com.example.recomendation.model.form.InventoryMovementFormDto;
import com.example.recomendation.model.form.ProductFormDto;
import com.example.recomendation.model.mapper.ProductMapper;
import com.example.recomendation.model.repository.CategoryRepository;
import com.example.recomendation.model.repository.ProductRepository;
import com.example.recomendation.model.service.interfaces.InventoryService;
import com.example.recomendation.model.service.interfaces.ProductService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryService inventoryService;

    @Override
    public Page<Product> findAll(Pageable pageable, String name, Long categoryId) {
        if (StringUtils.hasText(name)) {
            return productRepository.findByActiveTrueAndNameContainingIgnoreCase(name, pageable);
        }
        if (categoryId != null) {
            return productRepository.findByActiveTrueAndCategoryId(categoryId, pageable);
        }
        return productRepository.findByActiveTrue(pageable);
    }

    @Override
    public Product findById(Long id) {
        return productRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
    }

    @Override
    public Product findByBarcode(String barcode) {
        return productRepository.findByBarcodeAndActiveTrue(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("No hay ningún producto activo con ese código de barras"));
    }

    @Override
    @Transactional
    public Product create(ProductFormDto form, String username) {
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        validateBarcodeUniqueness(form.getBarcode(), null);

        Product product = new Product();
        ProductMapper.applyForm(product, form);
        product.setCategory(category);
        product.setCurrentStock(0);
        product.setActive(true);
        product = productRepository.save(product);

        int initialStock = form.getInitialStock() != null ? form.getInitialStock() : 0;
        if (initialStock > 0) {
            InventoryMovementFormDto movementForm = new InventoryMovementFormDto();
            movementForm.setProductId(product.getId());
            movementForm.setType(MovementType.IN);
            movementForm.setQuantity(initialStock);
            movementForm.setMovementDate(form.getEntryDate() != null ? form.getEntryDate() : LocalDate.now());
            movementForm.setLotNumber(form.getLotNumber());
            movementForm.setExpirationDate(form.getExpirationDate());
            movementForm.setDescription("Stock inicial registrado al crear el producto");

            inventoryService.registerMovement(movementForm, username);
            product = productRepository.findById(product.getId()).orElseThrow();
        }

        return product;
    }

    @Override
    @Transactional
    public Product update(Long id, ProductFormDto form) {
        Product product = findById(id);
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));

        validateBarcodeUniqueness(form.getBarcode(), id);

        ProductMapper.applyForm(product, form);
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    public List<Product> findLowStock() {
        return productRepository.findLowStockProducts();
    }

    private void validateBarcodeUniqueness(String barcode, Long currentProductId) {
        if (!StringUtils.hasText(barcode)) {
            return;
        }
        boolean duplicated = currentProductId == null
                ? productRepository.existsByBarcode(barcode)
                : productRepository.existsByBarcodeAndIdNot(barcode, currentProductId);

        if (duplicated) {
            throw new DuplicateBarcodeException("Ya existe otro producto registrado con el código de barras " + barcode);
        }
    }
}