package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    List<Product> findAllByOrderByCreatedAtAscIdAsc();

    boolean existsByCategoryId(String categoryId);

    /** SELECT ... FOR UPDATE - used when deducting stock so two payments can't overwrite each other. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);
}
