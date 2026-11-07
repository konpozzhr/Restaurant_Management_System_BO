package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    List<InventoryItem> findAllByOrderByCreatedAtDescIdAsc();
}
