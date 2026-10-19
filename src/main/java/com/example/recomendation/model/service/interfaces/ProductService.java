package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.Product;
import com.example.recomendation.model.form.ProductFormDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {

    Page<Product> findAll(Pageable pageable, String name, Long categoryId);

    Product findById(Long id);

    Product findByBarcode(String barcode);

    Product create(ProductFormDto form, String username);

    Product update(Long id, ProductFormDto form);

    void deactivate(Long id);

    List<Product> findLowStock();
}