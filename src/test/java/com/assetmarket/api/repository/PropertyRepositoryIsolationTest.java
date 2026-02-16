package com.assetmarket.api.repository;

import com.assetmarket.api.entity.Property;
import com.assetmarket.api.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import com.assetmarket.api.config.HibernateConfig;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(HibernateConfig.class)
@ActiveProfiles("test")
public class PropertyRepositoryIsolationTest {

    @Autowired
    private PropertyRepository propertyRepository;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void shouldOnlyShowPropertiesForCurrentTenant() {
        // Given
        Property p1 = Property.builder()
                .title("Tenant A Prop")
                .price(new BigDecimal("100"))
                .location("Loc A")
                .build();
        p1.setTenantId("tenant-a");
        propertyRepository.save(p1);

        Property p2 = Property.builder()
                .title("Tenant B Prop")
                .price(new BigDecimal("200"))
                .location("Loc B")
                .build();
        p2.setTenantId("tenant-b");
        propertyRepository.save(p2);

        // When
        TenantContext.setCurrentTenant("tenant-a");
        List<Property> tenantAProperties = propertyRepository.findAll();

        // Then
        assertThat(tenantAProperties).hasSize(1);
        assertThat(tenantAProperties.get(0).getTitle()).isEqualTo("Tenant A Prop");

        // When
        TenantContext.setCurrentTenant("tenant-b");
        List<Property> tenantBProperties = propertyRepository.findAll();

        // Then
        assertThat(tenantBProperties).hasSize(1);
        assertThat(tenantBProperties.get(0).getTitle()).isEqualTo("Tenant B Prop");
    }
}
