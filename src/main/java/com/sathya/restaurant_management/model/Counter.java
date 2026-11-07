package com.sathya.restaurant_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Tiny key/value counter table - used to hand out gap-free, race-safe order numbers. */
@Entity
@Table(name = "counters")
@Getter
@Setter
@NoArgsConstructor
public class Counter {

    public static final String ORDER_NUMBER = "orderNumber";

    @Id
    @Column(length = 60)
    private String name;

    @Column(name = "counter_value", nullable = false)
    private Long value;

    public Counter(String name, Long value) {
        this.name = name;
        this.value = value;
    }
}
