package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {

    List<InventoryLog> findTop30ByOrderByLoggedAtDescIdDesc();
}
