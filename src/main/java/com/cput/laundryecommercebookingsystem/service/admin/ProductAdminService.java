package com.cput.laundryecommercebookingsystem.service.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.repository.IOrderItemRepository;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Talent Nocuze
 * 230405886
 */
@Service
public class ProductAdminService {

    private static final Pattern EXTERNAL_URL = Pattern.compile("^https?://[^\\s<>\"']{1,490}$");

    private final IProductRepository products;
    private final IOrderItemRepository orderItems;
    private final ProductImageStorage images;

    public ProductAdminService(IProductRepository products, IOrderItemRepository orderItems,
                               ProductImageStorage images) {
        this.products = products;
        this.orderItems = orderItems;
        this.images = images;
    }

    @Transactional(readOnly = true)
    public List<ProductRow> listAll() {
        return products.findAll().stream()
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .map(ProductRow::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductRow get(Long id) {
        return ProductRow.from(find(id));
    }

    @Transactional
    public ProductRow create(ProductRequest req) {
        Product saved = products.save(new Product.Builder()
                .setName(req.name().trim())
                .setDescription(clean(req.description()))
                .setPrice(round2(req.price()))
                .setCategory(clean(req.category()))
                .setInventoryQuantity(req.inventoryQuantity())
                .setImageUrl(validImageUrl(req.imageUrl()))
                .setActive(req.active() == null || req.active())
                .build());
        return ProductRow.from(saved);
    }

    @Transactional
    public ProductRow update(Long id, ProductRequest req) {
        Product old = find(id);
        String newImage = validImageUrl(req.imageUrl());
        Product saved = products.save(new Product.Builder().copy(old)
                .setName(req.name().trim())
                .setDescription(clean(req.description()))
                .setPrice(round2(req.price()))
                .setCategory(clean(req.category()))
                .setInventoryQuantity(req.inventoryQuantity())
                .setImageUrl(newImage)
                .setActive(req.active() == null ? old.isActive() : req.active())
                .build());
        if (old.getImageUrl() != null && !old.getImageUrl().equals(newImage)) {
            images.deleteQuietly(old.getImageUrl());
        }
        return ProductRow.from(saved);
    }

    @Transactional
    public ProductRow setStock(Long id, int quantity) {
        return ProductRow.from(products.save(new Product.Builder().copy(find(id)).setInventoryQuantity(quantity).build()));
    }

    @Transactional
    public ProductRow setActive(Long id, boolean active) {
        return ProductRow.from(products.save(new Product.Builder().copy(find(id)).setActive(active).build()));
    }

    @Transactional
    public ProductRow uploadImage(Long id, MultipartFile file) throws IOException {
        Product old = find(id);
        String path = images.store(file);
        Product saved = products.save(new Product.Builder().copy(old).setImageUrl(path).build());
        images.deleteQuietly(old.getImageUrl());
        return ProductRow.from(saved);
    }

    /**
     * Deletes a product nobody has ordered. A product that appears in past orders is only
     * deactivated, so order history stays correct.
     */
    @Transactional
    public DeleteResult delete(Long id) {
        Product product = find(id);
        if (!orderItems.findByProduct(product).isEmpty()) {
            products.save(new Product.Builder().copy(product).setActive(false).build());
            return new DeleteResult(false, true,
                    "This product is part of existing orders, so it was deactivated instead of deleted.");
        }
        products.delete(product);
        images.deleteQuietly(product.getImageUrl());
        return new DeleteResult(true, false, "Product deleted.");
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new NoSuchElementException("Product not found."));
    }

    /** Accepts nothing, one of our own uploaded images, or a plain http(s) link - never javascript: or data: URLs. */
    static String validImageUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String trimmed = url.trim();
        if (trimmed.startsWith(ProductImageStorage.PUBLIC_PREFIX) && !trimmed.contains("..")) return trimmed;
        if (EXTERNAL_URL.matcher(trimmed).matches()) return trimmed;
        throw new IllegalArgumentException("Image URL must start with http:// or https://");
    }

    private static String clean(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static double round2(double v) {
        return java.math.BigDecimal.valueOf(v).setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
}
