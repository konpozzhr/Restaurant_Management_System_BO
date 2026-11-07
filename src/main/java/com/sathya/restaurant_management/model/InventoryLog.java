package com.sathya.restaurant_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Audit trail of stock movements shown in "Recent activity" on the Inventory page. */
@Entity
@Table(name = "inventory_logs")
@Getter
@Setter
@NoArgsConstructor
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Item name at the time (kept even if the item is deleted later). */
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal delta;

    @Column(nullable = false, length = 20)
    private String unit;

    /** new | in | out */
    @Column(nullable = false, length = 10)
    private String kind;

    @Column(name = "logged_at", nullable = false)
    private Instant loggedAt = Instant.now();
}
