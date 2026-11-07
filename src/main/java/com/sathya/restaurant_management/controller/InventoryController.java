package com.sathya.restaurant_management.controller;

import com.sathya.restaurant_management.dto.InventoryAdjustRequest;
import com.sathya.restaurant_management.dto.InventoryItemDto;
import com.sathya.restaurant_management.dto.InventoryItemRequest;
import com.sathya.restaurant_management.dto.InventoryLogDto;
import com.sathya.restaurant_management.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public List<InventoryItemDto> getAll() {
        return inventoryService.findAll();
    }

    /** Recent stock movements (latest 30). */
    @GetMapping("/log")
    public List<InventoryLogDto> getLog() {
        return inventoryService.recentLog();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemDto create(@Valid @RequestBody InventoryItemRequest request) {
        return inventoryService.create(request);
    }

    /** Body: { "delta": 5 } for stock in, { "delta": -2 } for stock out. */
    @PostMapping("/{id}/adjust")
    public InventoryItemDto adjust(@PathVariable String id, @Valid @RequestBody InventoryAdjustRequest request) {
        return inventoryService.adjust(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
