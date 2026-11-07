package com.sathya.restaurant_management.config;

import com.sathya.restaurant_management.model.Category;
import com.sathya.restaurant_management.model.Counter;
import com.sathya.restaurant_management.model.Customer;
import com.sathya.restaurant_management.model.InventoryItem;
import com.sathya.restaurant_management.model.Product;
import com.sathya.restaurant_management.repository.CategoryRepository;
import com.sathya.restaurant_management.repository.CounterRepository;
import com.sathya.restaurant_management.repository.CustomerRepository;
import com.sathya.restaurant_management.repository.InventoryItemRepository;
import com.sathya.restaurant_management.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads the starter menu, customers and ingredients the frontend used to ship in seedData.js.
 * Each table is only seeded when it is completely empty, so it never overwrites real data
 * (and you can delete a table's rows to get its sample data back on next start).
 * Set app.seed.enabled=false in application.properties to switch seeding off entirely.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final InventoryItemRepository inventoryRepository;
    private final CounterRepository counterRepository;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        Instant base = Instant.now().minus(1, ChronoUnit.DAYS);

        // Walk-in customer must always exist - orders default to it.
        if (customerRepository.count() == 0) {
            customerRepository.saveAll(List.of(
                    new Customer("c_walkin", "Walk-in customer", "", base),
                    new Customer("c_1", "Somban Phat", "012 345 678", base.plusMillis(1)),
                    new Customer("c_2", "Sokha Chan", "017 222 913", base.plusMillis(2)),
                    new Customer("c_3", "Dara Vong", "096 810 224", base.plusMillis(3)),
                    new Customer("c_4", "Ratana Kim", "089 456 771", base.plusMillis(4))));
            log.info("Seeded customers");
        } else if (!customerRepository.existsById(Customer.WALK_IN_ID)) {
            customerRepository.save(new Customer(Customer.WALK_IN_ID, "Walk-in customer", "", base));
        }

        if (categoryRepository.count() == 0 && productRepository.count() == 0) {
            String[] names = {"Starters", "Mains", "Pizza & Pasta", "Desserts", "Drinks"};
            Map<String, Category> cats = new HashMap<>();
            for (int i = 0; i < names.length; i++) {
                Category c = new Category("cat_" + names[i].toLowerCase().replaceAll("[^a-z]+", ""), names[i], base.plusMillis(i));
                cats.put(names[i], categoryRepository.save(c));
            }
            List<Product> products = new ArrayList<>();
            // id, name, category, price, stock, icon, color, cost
            Object[][] rows = {
                {"p_bruschetta", "Tomato Bruschetta", "Starters", "7.5", 40, "Sandwich", "#E5484D", "2.4"},
                {"p_soup", "Roasted Pumpkin Soup", "Starters", "6.5", 35, "Soup", "#F26B1D", "1.9"},
                {"p_greeksalad", "Greek Salad", "Starters", "8.0", 30, "Salad", "#1FA37A", "2.6"},
                {"p_salmon", "Grilled Salmon", "Mains", "21.0", 18, "Fish", "#F26B1D", "8.4"},
                {"p_steak", "Ribeye Steak", "Mains", "28.5", 14, "Beef", "#E5484D", "20.5"},
                {"p_chicken", "Herb Roast Chicken", "Mains", "18.0", 22, "Drumstick", "#B4862B", "6.2"},
                {"p_margherita", "Margherita Pizza", "Pizza & Pasta", "13.5", 45, "Pizza", "#E5484D", "3.9"},
                {"p_carbonara", "Spaghetti Carbonara", "Pizza & Pasta", "15.0", 32, "UtensilsCrossed", "#B4862B", "4.6"},
                {"p_tiramisu", "Tiramisu", "Desserts", "7.0", 20, "Cake", "#7A5AF0", "2.3"},
                {"p_gelato", "Pistachio Gelato", "Desserts", "5.5", 28, "IceCreamCone", "#1FA37A", "1.8"},
                {"p_latte", "Iced Latte", "Drinks", "4.5", 60, "Coffee", "#B4862B", "1.1"},
                {"p_lemonade", "Fresh Lemonade", "Drinks", "3.5", 55, "CupSoda", "#F26B1D", "0.8"},
                {"p_wine", "House Red Wine (glass)", "Drinks", "9.0", 36, "Wine", "#7A5AF0", "4.2"},
            };
            for (int i = 0; i < rows.length; i++) {
                Object[] r = rows[i];
                Product p = new Product();
                p.setId((String) r[0]);
                p.setName((String) r[1]);
                p.setCategory(cats.get((String) r[2]));
                p.setPrice(new BigDecimal((String) r[3]));
                p.setStock((Integer) r[4]);
                p.setIcon((String) r[5]);
                p.setColor((String) r[6]);
                p.setCost(new BigDecimal((String) r[7]));
                p.setCreatedAt(base.plusMillis(i));
                products.add(p);
            }
            productRepository.saveAll(products);
            log.info("Seeded {} categories and {} products", names.length, products.size());
        }

        if (inventoryRepository.count() == 0) {
            Instant now = Instant.now();
            // id, name, category, unit, stock, par, critical, cost, expires in (days), updated (minutes ago)
            Object[][] rows = {
                {"i_tomatoes", "Tomatoes", "Produce", "kg", "12", "15", "5", "2.2", 3, 90},
                {"i_chicken", "Chicken Breast", "Meat", "kg", "8", "12", "4", "6.5", 3, 150},
                {"i_lettuce", "Lettuce", "Produce", "kg", "25", "10", "4", "1.6", 2, 200},
                {"i_mozzarella", "Mozzarella Cheese", "Dairy", "kg", "4", "8", "2", "9", 9, 220},
                {"i_oliveoil", "Olive Oil", "Dry Goods", "L", "15", "8", "3", "8", 200, 270},
                {"i_salmon", "Fresh Salmon", "Seafood", "kg", "3", "6", "2", "18", 2, 290},
                {"i_onions", "Onions", "Produce", "kg", "20", "10", "4", "1.1", 20, 330},
                {"i_basil", "Basil", "Produce", "kg", "2", "5", "2", "12", 2, 360},
                {"i_eggs", "Eggs", "Dairy", "pack", "30", "15", "5", "3.2", 12, 380},
                {"i_ribeye", "Beef Ribeye", "Meat", "kg", "14", "10", "4", "17", 5, 400},
                {"i_spaghetti", "Spaghetti", "Dry Goods", "kg", "22", "10", "4", "1.8", 300, 420},
                {"i_parmesan", "Parmesan", "Dairy", "kg", "6", "4", "1.5", "14", 40, 450},
                {"i_shrimp", "Shrimp", "Seafood", "kg", "5", "6", "2", "12", 2, 500},
                {"i_flour", "Flour", "Dry Goods", "kg", "0", "15", "5", "0.9", 180, 600},
            };
            List<InventoryItem> items = new ArrayList<>();
            for (int i = 0; i < rows.length; i++) {
                Object[] r = rows[i];
                InventoryItem it = new InventoryItem();
                it.setId((String) r[0]);
                it.setName((String) r[1]);
                it.setCategory((String) r[2]);
                it.setUnit((String) r[3]);
                it.setStock(new BigDecimal((String) r[4]));
                it.setPar(new BigDecimal((String) r[5]));
                it.setCritical(new BigDecimal((String) r[6]));
                it.setCost(new BigDecimal((String) r[7]));
                it.setExpiresAt(now.plus((Integer) r[8], ChronoUnit.DAYS));
                it.setUpdatedAt(now.minus((Integer) r[9], ChronoUnit.MINUTES));
                // The list is shown newest-first, so give the first row the newest createdAt.
                it.setCreatedAt(base.minusMillis(i));
                items.add(it);
            }
            inventoryRepository.saveAll(items);
            log.info("Seeded {} inventory items", items.size());
        }

        // The old app started its order numbers at #24.
        if (!counterRepository.existsById(Counter.ORDER_NUMBER)) {
            counterRepository.save(new Counter(Counter.ORDER_NUMBER, 23L));
        }
    }
}
