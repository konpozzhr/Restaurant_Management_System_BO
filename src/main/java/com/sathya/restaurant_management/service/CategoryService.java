package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.CategoryDto;
import com.sathya.restaurant_management.dto.CategoryRequest;
import com.sathya.restaurant_management.exception.ConflictException;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.model.Category;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public List<CategoryDto> findAll() {
        return categoryRepository.findAllByOrderByCreatedAtAscIdAsc().stream().map(this::toDto).toList();
    }

    @Transactional
    public CategoryDto create(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("\"" + name + "\" already exists.");
        }
        Category saved = categoryRepository.save(new Category(Ids.next("cat"), name, Instant.now()));
        return toDto(saved);
    }

    /** Renaming is a single UPDATE - products point at the category row, so they follow automatically. */
    @Transactional
    public CategoryDto update(String id, CategoryRequest request) {
        Category category = find(id);
        String name = request.name().trim();
        boolean clash = categoryRepository.findByNameIgnoreCase(name)
                .filter(other -> !other.getId().equals(id))
                .isPresent();
        if (clash) {
            throw new ConflictException("\"" + name + "\" already exists.");
        }
        category.setName(name);
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(String id) {
        Category category = find(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Move or delete its products before removing this category.");
        }
        categoryRepository.delete(category);
    }

    public Category find(String id) {
        return categoryRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Category", id));
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(c.getId(), c.getName());
    }
}
