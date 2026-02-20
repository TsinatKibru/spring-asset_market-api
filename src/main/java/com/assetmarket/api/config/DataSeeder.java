package com.assetmarket.api.config;

import com.assetmarket.api.entity.Category;
import com.assetmarket.api.entity.Property;
import com.assetmarket.api.entity.PropertyStatus;
import com.assetmarket.api.repository.CategoryRepository;
import com.assetmarket.api.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final PropertyRepository propertyRepository;

    @Override
    public void run(String... args) throws Exception {
        String tenantId = "default";

        if (categoryRepository.count() == 0) {
            log.info("Seeding initial categories for tenant: {}", tenantId);

            // 1. Apartment
            Category apartment = Category.builder()
                    .name("Apartment")
                    .description("Modern urban living spaces")
                    .tenantId(tenantId)
                    .attributeSchema(List.of(
                            Map.of("name", "bedrooms", "type", "number", "required", true),
                            Map.of("name", "floor", "type", "number", "required", false),
                            Map.of("name", "furnished", "type", "boolean", "required", true)))
                    .build();

            // 2. Villa
            Category villa = Category.builder()
                    .name("Villa")
                    .description("Luxury private residences")
                    .tenantId(tenantId)
                    .attributeSchema(List.of(
                            Map.of("name", "bedrooms", "type", "number", "required", true),
                            Map.of("name", "pool", "type", "boolean", "required", true),
                            Map.of("name", "landArea", "type", "number", "required", false)))
                    .build();

            // 3. Commercial
            Category commercial = Category.builder()
                    .name("Commercial")
                    .description("Business and retail spaces")
                    .tenantId(tenantId)
                    .attributeSchema(List.of(
                            Map.of("name", "sqm", "type", "number", "required", true),
                            Map.of("name", "type", "type", "string", "required", true), // Office, Retail, Warehouse
                            Map.of("name", "parking", "type", "number", "required", false)))
                    .build();

            categoryRepository.saveAll(List.of(apartment, villa, commercial));

            log.info("Categories seeded. Now seeding properties...");

            // Seed sample properties
            if (propertyRepository.count() == 0) {
                List<Property> properties = new ArrayList<>();

                properties.add(Property.builder()
                        .title("Skyline Penthouse")
                        .description("Luxurious penthouse with panoramic city views.")
                        .price(new BigDecimal("1200000"))
                        .location("Downtown Metropolitan")
                        .status(PropertyStatus.AVAILABLE)
                        .category(apartment)
                        .tenantId(tenantId)
                        .attributes(Map.of("bedrooms", 3, "floor", 42, "furnished", true))
                        .imageUrls(List.of("https://images.unsplash.com/photo-1522708323590-d24dbb6b0267"))
                        .build());

                properties.add(Property.builder()
                        .title("Cozy Studio")
                        .description("Compact and efficient studio in the heart of the city.")
                        .price(new BigDecimal("250000"))
                        .location("North End")
                        .status(PropertyStatus.AVAILABLE)
                        .category(apartment)
                        .tenantId(tenantId)
                        .attributes(Map.of("bedrooms", 1, "floor", 5, "furnished", false))
                        .imageUrls(List.of("https://images.unsplash.com/photo-1502672260266-1c1ef2d93688"))
                        .build());

                properties.add(Property.builder()
                        .title("Oceanfront Villa")
                        .description("Stunning villa with direct beach access and private pool.")
                        .price(new BigDecimal("3500000"))
                        .location("Palm Shore")
                        .status(PropertyStatus.AVAILABLE)
                        .category(villa)
                        .tenantId(tenantId)
                        .attributes(Map.of("bedrooms", 5, "pool", true, "landArea", 800))
                        .imageUrls(List.of("https://images.unsplash.com/photo-1613490493576-7fde63acd811"))
                        .build());

                properties.add(Property.builder()
                        .title("Tech Hub Office")
                        .description("Premium office space in a high-traffic business district.")
                        .price(new BigDecimal("850000"))
                        .location("Innovation Park")
                        .status(PropertyStatus.AVAILABLE)
                        .category(commercial)
                        .tenantId(tenantId)
                        .attributes(Map.of("sqm", 250, "type", "Office", "parking", 4))
                        .imageUrls(List.of("https://images.unsplash.com/photo-1497366216548-37526070297c"))
                        .build());

                propertyRepository.saveAll(properties);
                log.info("Seeded {} properties.", properties.size());
            }
        }
    }
}
