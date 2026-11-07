package com.sathya.restaurant_management.repository;

import com.sathya.restaurant_management.model.Counter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CounterRepository extends JpaRepository<Counter, String> {

    /** SELECT ... FOR UPDATE, so two cashiers can never receive the same order number. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Counter c where c.name = :name")
    Optional<Counter> lockByName(@Param("name") String name);
}
