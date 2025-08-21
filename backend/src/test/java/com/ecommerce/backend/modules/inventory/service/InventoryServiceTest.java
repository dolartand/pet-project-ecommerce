package com.ecommerce.backend.modules.inventory.service;

import com.ecommerce.backend.modules.inventory.entity.Inventory;
import com.ecommerce.backend.modules.inventory.entity.InventoryHistory;
import com.ecommerce.backend.modules.inventory.repository.InventoryHistoryRepository;
import com.ecommerce.backend.modules.inventory.repository.InventoryRepository;
import com.ecommerce.backend.shared.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceTest {

    @Mock
    private InventoryRepository  inventoryRepository;
    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory inventory;
    private Long productId = 1L;
    private Long orderId = 100L;
    private String username = "test";

    @BeforeEach
    public void setUp() {
        inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(5);
    }

    @Test
    void reserveProduct_shouldSucceedWhenStockIsSufficient() {
        Map<Long, Integer> productQuantities = Map.of(productId, 3);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.reserveProduct(orderId, productQuantities, username);

        ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(captor.capture());
        Inventory savedInventory = captor.getValue();

        assertThat(savedInventory.getAvailableQuantity()).isEqualTo(7);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(8);

        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    void reserveProduct_shouldFailWhenStockIsNotSufficient() {
        Map<Long, Integer> productQuantities = Map.of(productId, 11);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThatThrownBy(() -> inventoryService.reserveProduct(orderId, productQuantities, username))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Out of stock");

        verify(inventoryRepository, never()).save(any());
        verify(inventoryHistoryRepository, never()).save(any());
    }

    @Test
    void cancelReservation_shouldReturnItemsToStock() {
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setQuantity(3);
        history.setChangeType(InventoryHistory.ChangeType.RESERVE);

        when(inventoryHistoryRepository.findByOrderId(orderId)).thenReturn(List.of(history));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.cancelReservation(orderId, username);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory savedInventory = inventoryCaptor.getValue();

        assertThat(savedInventory.getAvailableQuantity()).isEqualTo(13);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(2);

        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }

    @Test
    void confirmReservation_shouldDecreaseReservedQuantity() {
        InventoryHistory history = new InventoryHistory();
        history.setProductId(productId);
        history.setQuantity(3);
        history.setChangeType(InventoryHistory.ChangeType.RESERVE);

        when(inventoryHistoryRepository.findByOrderId(orderId)).thenReturn(List.of(history));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        inventoryService.confirmReservation(orderId, username);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory savedInventory = inventoryCaptor.getValue();

        assertThat(savedInventory.getAvailableQuantity()).isEqualTo(10);
        assertThat(savedInventory.getReservedQuantity()).isEqualTo(2);

        verify(inventoryHistoryRepository).save(any(InventoryHistory.class));
    }
}
