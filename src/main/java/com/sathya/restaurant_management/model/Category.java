package com.sathya.restaurant_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {

    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Keeps categories in the order they were created (the tabs on the Products page). */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Category(String id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }
}
