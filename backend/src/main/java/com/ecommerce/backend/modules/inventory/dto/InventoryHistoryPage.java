package com.ecommerce.backend.modules.inventory.dto;

import com.ecommerce.backend.shared.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryHistoryPage {
    private List<InventoryHistoryDto> content;
    private PageInfo page;
}
