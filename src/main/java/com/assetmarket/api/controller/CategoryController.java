package com.assetmarket.api.controller;

import com.assetmarket.api.dto.CategoryDTO;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/categories")
@Tag(name = "Category Management", description = "Endpoints for managing property categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    @Operation(summary = "List all categories", description = "Returns a list of all categories for the current tenant")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Fetch details of a single category")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!category.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Category not found in this tenant");
        }

        return ResponseEntity.ok(convertToDTO(category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Allows administrators to create new property categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        if (categoryRepository.existsByNameAndTenantId(categoryDTO.getName(), TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException(
                    "Category with name '" + categoryDTO.getName() + "' already exists in this tenant.");
        }

        Category category = Category.builder()
                .name(categoryDTO.getName())
                .description(categoryDTO.getDescription())
                .attributeSchema(categoryDTO.getAttributeSchema() != null ? categoryDTO.getAttributeSchema().stream()
                        .map(s -> {
                            java.util.Map<String, Object> map = new java.util.HashMap<>();
                            map.put("name", s.getName());
                            map.put("type", s.getType());
                            map.put("required", s.isRequired());
                            return map;
                        }).collect(Collectors.toList()) : null)
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return new ResponseEntity<>(convertToDTO(savedCategory), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a category", description = "Update an existing category's name, description, and schema")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        // Tenant check is handled by Hibernate Filter, but good transparency here:
        if (!category.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Category not found in this tenant");
        }

        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setAttributeSchema(categoryDTO.getAttributeSchema() != null ? categoryDTO.getAttributeSchema().stream()
                .map(s -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("name", s.getName());
                    map.put("type", s.getType());
                    map.put("required", s.isRequired());
                    return map;
                }).collect(Collectors.toList()) : null);

        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(convertToDTO(updatedCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Delete a category. Warning: Will fail if properties are linked.")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (!category.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Category not found in this tenant");
        }

        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .attributeSchema(category.getAttributeSchema() != null ? category.getAttributeSchema().stream()
                        .map(m -> new com.assetmarket.api.dto.AttributeSchemaDTO(
                                (String) m.get("name"),
                                (String) m.get("type"),
                                m.get("required") != null && (boolean) m.get("required")))
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
