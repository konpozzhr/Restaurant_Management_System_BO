package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.CostRequest;
import com.sathya.restaurant_management.dto.ProductDto;
import com.sathya.restaurant_management.dto.ProductRequest;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.model.Category;
import com.sathya.restaurant_management.model.Product;
import com.sathya.restaurant_management.repository.CategoryRepository;
import com.sathya.restaurant_management.repository.ProductRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public List<ProductDto> findAll() {
        return productRepository.findAllByOrderByCreatedAtAscIdAsc().stream().map(this::toDto).toList();
    }

    public ProductDto getById(String id) {
        return toDto(find(id));
    }

    @Transactional
    public ProductDto create(ProductRequest request) {
        Product product = new Product();
        product.setId(Ids.next("p"));
        product.setCreatedAt(Instant.now());
        apply(product, request);
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto update(String id, ProductRequest request) {
        Product product = find(id);
        apply(product, request); // cost is intentionally untouched - editing a menu item never wipes it
        return toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto updateCost(String id, CostRequest request) {
        Product product = find(id);
        product.setCost(request.cost());
        return toDto(productRepository.save(product));
    }

    @Transactional
    public void delete(String id) {
        productRepository.delete(find(id));
    }

    private void apply(Product product, ProductRequest request) {
        Category category = categoryRepository.findByNameIgnoreCase(request.category().trim())
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + request.category()));
        product.setName(request.name().trim());
        product.setCategory(category);
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setIcon(blankToDefault(request.icon(), "Package"));
        product.setColor(blankToDefault(request.color(), "#F26B1D"));
        product.setPhoto(request.photo() == null || request.photo().isBlank() ? null : request.photo());
    }

    private static String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Product find(String id) {
        return productRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Product", id));
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(p.getId(), p.getName(), p.getCategory().getName(), p.getPrice(), p.getStock(),
                p.getIcon(), p.getColor(), p.getPhoto(), p.getCost());
    }
}
