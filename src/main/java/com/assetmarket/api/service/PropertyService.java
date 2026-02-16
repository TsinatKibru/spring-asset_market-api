package com.assetmarket.api.service;

import com.assetmarket.api.dto.PropertyDTO;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.repository.PropertyRepository;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.entity.Category;
import com.assetmarket.api.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<PropertyDTO> getAllProperties(String category, Pageable pageable) {
        Page<Property> properties;
        if (category != null && !category.isEmpty()) {
            properties = propertyRepository.findByCategoryName(category, pageable);
        } else {
            properties = propertyRepository.findAll(pageable);
        }
        return properties.map(this::convertToDTO);
    }

    @Transactional
    public PropertyDTO createProperty(PropertyDTO propertyDTO) {
        Category category = categoryRepository.findByName(propertyDTO.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .name(propertyDTO.getCategoryName())
                            .tenantId(TenantContext.getCurrentTenant())
                            .build();
                    return categoryRepository.save(newCategory);
                });

        Property property = Property.builder()
                .title(propertyDTO.getTitle())
                .description(propertyDTO.getDescription())
                .price(propertyDTO.getPrice())
                .location(propertyDTO.getLocation())
                .category(category)
                .tenantId(TenantContext.getCurrentTenant())
                .build();

        Property savedProperty = propertyRepository.save(property);
        return convertToDTO(savedProperty);
    }

    private PropertyDTO convertToDTO(Property property) {
        PropertyDTO dto = new PropertyDTO();
        dto.setId(property.getId());
        dto.setTitle(property.getTitle());
        dto.setDescription(property.getDescription());
        dto.setPrice(property.getPrice());
        dto.setLocation(property.getLocation());
        if (property.getCategory() != null) {
            dto.setCategoryName(property.getCategory().getName());
        }
        return dto;
    }
}
