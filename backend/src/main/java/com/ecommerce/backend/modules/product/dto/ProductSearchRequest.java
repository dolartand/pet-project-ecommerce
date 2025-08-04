package com.ecommerce.backend.modules.product.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchRequest {
    @Size(max = 100)
    private String search;
    private Long categoryId;
    @DecimalMin("0")
    private BigDecimal minPrice;
    @DecimalMin("0")
    private BigDecimal maxPrice;
    private Boolean available;
    private String sortBy;
    private String sortOrder;

    public boolean hasSearch() {
        return search != null && !search.isEmpty();
    }

    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasPriceRange() {
        return minPrice != null && maxPrice != null;
    }

    public boolean hasAvailabilityFilter() {
        return available != null && available;
    }

    public Sort getSort() {
        return Sort.by(sortBy, sortOrder);
    }
}
