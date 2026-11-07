package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, String> {

    /** Newest first - the same order the app showed (new orders were prepended). */
    List<SalesOrder> findAllByOrderByNumberDesc();
}
