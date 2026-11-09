package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.domain.OrderItem;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;

import static com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.LOW_STOCK_LEVEL;
import com.cput.laundryecommercebookingsystem.repository.IOrderItemRepository;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/*AdminProductService.java
 * Admin dashboard: add, edit, price, stock, picture, and deactivate or delete shop products.
 * A product that appears in past orders can't be deleted (the orders still point at it);
 * deactivating it hides it from the shop instead.
 */
@Service
public class AdminProductService {

    private final IProductRepository productRepository;
    private final IOrderItemRepository orderItemRepository;
    private final ImageStorageService imageStorage;

    public AdminProductService(IProductRepository productRepository, IOrderItemRepository orderItemRepository,
                               ImageStorageService imageStorage) {
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.imageStorage = imageStorage;
    }

    /**
     * @param q        matches name, description or category (optional)
     * @param category exact category (optional)
     * @param status   "active", "inactive" or "low" for low stock (optional)
     */
    @Transactional(readOnly = true)
    public List<AdminProductView> list(String q, String category, String status) {
        String search = q == null ? "" : q.trim().toLowerCase();
        Map<Long, Long> sold = unitsSoldPerProduct();
        return productRepository.findAll().stream()
                .filter(p -> search.isEmpty() || contains(p.getName(), search)
                        || contains(p.getDescription(), search) || contains(p.getCategory(), search))
                .filter(p -> category == null || category.isBlank() || category.equalsIgnoreCase(p.getCategory()))
                .filter(p -> status == null || status.isBlank()
                        || ("active".equalsIgnoreCase(status) && p.isActive())
                        || ("inactive".equalsIgnoreCase(status) && !p.isActive())
                        || ("low".equalsIgnoreCase(status) && p.isActive()
                            && p.getInventoryQuantity() <= LOW_STOCK_LEVEL))
                .sorted(Comparator.comparing(Product::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(p -> AdminProductView.from(p, sold.getOrDefault(p.getProductId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminProductView get(Long id) {
        return view(find(id));
    }

    @Transactional
    public AdminProductView create(ProductRequest req) {
        validate(req, true);
        String name = req.name().trim();
        checkNameIsFree(name, null);
        Product product = new Product.Builder()
                .setName(name)
                .setDescription(trimOrEmpty(req.description()))
                .setCategory(trimOrEmpty(req.category()))
                .setPrice(round(req.price()))
                .setInventoryQuantity(req.inventoryQuantity())
                .setActive(true)
                .build();
        return view(productRepository.save(product));
    }

    /** Name, description, category and price. Stock has its own action so an edit can't overwrite a live count. */
    @Transactional
    public AdminProductView update(Long id, ProductRequest req) {
        Product existing = find(id);
        validate(req, false);
        String name = req.name().trim();
        checkNameIsFree(name, id);
        Product updated = new Product.Builder()
                .copy(existing)
                .setName(name)
                .setDescription(trimOrEmpty(req.description()))
                .setCategory(trimOrEmpty(req.category()))
                .setPrice(round(req.price())) // past orders keep the price they were bought at
                .build();
        return view(productRepository.save(updated));
    }

    @Transactional
    public AdminProductView setStock(Long id, Integer quantity) {
        if (quantity == null || quantity < 0 || quantity > 100_000) {
            throw new IllegalArgumentException("Stock must be between 0 and 100 000");
        }
        Product updated = new Product.Builder().copy(find(id)).setInventoryQuantity(quantity).build();
        return view(productRepository.save(updated));
    }

    @Transactional
    public AdminProductView setActive(Long id, boolean active) {
        Product updated = new Product.Builder().copy(find(id)).setActive(active).build();
        return view(productRepository.save(updated));
    }

    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        if (!orderItemRepository.findByProduct(product).isEmpty()) {
            throw new IllegalStateException("This product is in past orders, so it can't be deleted. "
                    + "Deactivate it to hide it from the shop.");
        }
        imageStorage.delete(product.getImageUrl());
        productRepository.delete(product);
    }

    @Transactional
    public AdminProductView uploadImage(Long id, MultipartFile file) {
        Product existing = find(id);
        String newUrl = imageStorage.storeProductImage(file);
        Product updated = new Product.Builder().copy(existing).setImageUrl(newUrl).build();
        Product saved = productRepository.save(updated);
        imageStorage.delete(existing.getImageUrl()); // the old picture is no longer used
        return view(saved);
    }

    @Transactional
    public AdminProductView removeImage(Long id) {
        Product existing = find(id);
        imageStorage.delete(existing.getImageUrl());
        Product updated = new Product.Builder().copy(existing).setImageUrl(null).build();
        return view(productRepository.save(updated));
    }

    // ---------- helpers ----------

    private AdminProductView view(Product p) {
        long sold = orderItemRepository.findByProduct(p).stream().mapToLong(OrderItem::getQuantity).sum();
        return AdminProductView.from(p, sold);
    }

    private Map<Long, Long> unitsSoldPerProduct() {
        return orderItemRepository.findAll().stream()
                .filter(i -> i.getProduct() != null)
                .collect(Collectors.groupingBy(i -> i.getProduct().getProductId(),
                        Collectors.summingLong(OrderItem::getQuantity)));
    }

    private void validate(ProductRequest req, boolean creating) {
        if (req == null || req.name() == null || req.name().isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (req.name().trim().length() > 100) {
            throw new IllegalArgumentException("Product name must be 100 characters or fewer");
        }
        if (req.price() == null || req.price() <= 0 || req.price() > 100_000) {
            throw new IllegalArgumentException("Price must be more than R0 and at most R100 000");
        }
        if (creating && (req.inventoryQuantity() == null || req.inventoryQuantity() < 0
                || req.inventoryQuantity() > 100_000)) {
            throw new IllegalArgumentException("Opening stock must be between 0 and 100 000");
        }
    }

    private void checkNameIsFree(String name, Long ignoreId) {
        boolean taken = productRepository.findAll().stream()
                .anyMatch(p -> p.getName() != null && p.getName().equalsIgnoreCase(name)
                        && (ignoreId == null || !ignoreId.equals(p.getProductId())));
        if (taken) {
            throw new IllegalStateException("A product called " + name + " already exists");
        }
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product not found with id: " + id));
    }

    private static double round(double value) {
        return Math.round(value * 100) / 100.0;
    }

    private static boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
