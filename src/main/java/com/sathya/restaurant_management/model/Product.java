package com.sathya.restaurant_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    /** lucide-react icon name, e.g. "Pizza". */
    @Column(length = 60)
    private String icon = "Package";

    /** Hex colour used for the product tile, e.g. "#F26B1D". */
    @Column(length = 20)
    private String color = "#F26B1D";

    /** Optional photo as a (downscaled) data URL. */
    @Column(columnDefinition = "text")
    private String photo;

    /** Food cost per portion (Food Cost & Profit page). Null until someone records it. */
    @Column(precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
