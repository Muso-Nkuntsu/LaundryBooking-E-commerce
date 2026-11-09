package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.ProductAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductAdminService productService;

    public AdminProductController(ProductAdminService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductRow> list() {
        return productService.listAll();
    }

    @GetMapping("/{id}")
    public ProductRow get(@PathVariable Long id) {
        return productService.get(id);
    }

    @PostMapping
    public ResponseEntity<ProductRow> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ProductRow update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PutMapping("/{id}/stock")
    public ProductRow setStock(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
        return productService.setStock(id, request.inventoryQuantity());
    }

    @PutMapping("/{id}/active")
    public ProductRow setActive(@PathVariable Long id, @Valid @RequestBody ActiveRequest request) {
        return productService.setActive(id, request.active());
    }

    @PostMapping(path = "/{id}/image", consumes = "multipart/form-data")
    public ProductRow uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return productService.uploadImage(id, file);
    }

    @DeleteMapping("/{id}")
    public DeleteResult delete(@PathVariable Long id) {
        return productService.delete(id);
    }
}
