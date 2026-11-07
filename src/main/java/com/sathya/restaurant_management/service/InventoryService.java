package com.sathya.restaurant_management.service;

import com.sathya.restaurant_management.dto.InventoryAdjustRequest;
import com.sathya.restaurant_management.dto.InventoryItemDto;
import com.sathya.restaurant_management.dto.InventoryItemRequest;
import com.sathya.restaurant_management.dto.InventoryLogDto;
import com.sathya.restaurant_management.exception.ResourceNotFoundException;
import com.sathya.restaurant_management.model.InventoryItem;
import com.sathya.restaurant_management.model.InventoryLog;
import com.sathya.restaurant_management.repository.InventoryItemRepository;
import com.sathya.restaurant_management.repository.InventoryLogRepository;
import com.sathya.restaurant_management.util.Ids;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryLogRepository logRepository;

    public List<InventoryItemDto> findAll() {
        return itemRepository.findAllByOrderByCreatedAtDescIdAsc().stream().map(this::toDto).toList();
    }

    /** The 30 most recent stock movements, newest first. */
    public List<InventoryLogDto> recentLog() {
        return logRepository.findTop30ByOrderByLoggedAtDescIdDesc().stream()
                .map(l -> new InventoryLogDto(l.getId(), l.getLoggedAt(), l.getName(), l.getDelta(), l.getUnit(), l.getKind()))
                .toList();
    }

    @Transactional
    public InventoryItemDto create(InventoryItemRequest request) {
        BigDecimal par = orZero(request.par());
        Instant now = Instant.now();
        int shelfLife = request.shelfLife() == null ? 7 : request.shelfLife();

        InventoryItem item = new InventoryItem();
        item.setId(Ids.next("i"));
        item.setName(request.name().trim());
        item.setCategory(request.category());
        item.setUnit(request.unit());
        item.setStock(scale(orZero(request.stock())));
        item.setPar(scale(par));
        item.setCritical(scale(par.multiply(new BigDecimal("0.4"))));
        item.setCost(scale(orZero(request.cost())));
        item.setExpiresAt(now.plus(shelfLife, ChronoUnit.DAYS));
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        InventoryItem saved = itemRepository.save(item);

        writeLog(saved.getName(), saved.getStock(), saved.getUnit(), "new");
        return toDto(saved);
    }

    /** Stock in (positive delta) or out (negative). Stock never goes below zero. */
    @Transactional
    public InventoryItemDto adjust(String id, InventoryAdjustRequest request) {
        InventoryItem item = find(id);
        BigDecimal delta = request.delta();
        if (delta.signum() == 0) {
            return toDto(item);
        }
        BigDecimal before = item.getStock();
        BigDecimal after = scale(before.add(delta).max(BigDecimal.ZERO));
        item.setStock(after);
        item.setUpdatedAt(Instant.now());
        InventoryItem saved = itemRepository.save(item);

        writeLog(saved.getName(), after.subtract(before), saved.getUnit(), delta.signum() > 0 ? "in" : "out");
        return toDto(saved);
    }

    @Transactional
    public void delete(String id) {
        itemRepository.delete(find(id));
    }

    private void writeLog(String name, BigDecimal delta, String unit, String kind) {
        InventoryLog log = new InventoryLog();
        log.setName(name);
        log.setDelta(scale(delta));
        log.setUnit(unit);
        log.setKind(kind);
        log.setLoggedAt(Instant.now());
        logRepository.save(log);
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private InventoryItem find(String id) {
        return itemRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.forId("Inventory item", id));
    }

    private InventoryItemDto toDto(InventoryItem i) {
        return new InventoryItemDto(i.getId(), i.getName(), i.getCategory(), i.getUnit(), i.getStock(), i.getPar(),
                i.getCritical(), i.getCost(), i.getExpiresAt(), i.getUpdatedAt());
    }
}
