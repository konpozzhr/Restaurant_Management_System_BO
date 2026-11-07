package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    List<Customer> findAllByOrderByCreatedAtAscIdAsc();
}
