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
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
public class Customer {

    public static final String WALK_IN_ID = "c_walkin";

    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(length = 40)
    private String phone = "";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Customer(String id, String name, String phone, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.createdAt = createdAt;
    }
}
