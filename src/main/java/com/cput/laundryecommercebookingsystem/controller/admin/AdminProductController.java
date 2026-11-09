package com.cput.laundryecommercebookingsystem.controller.admin;

import com.cput.laundryecommercebookingsystem.dto.admin.AdminDtos.*;
import com.cput.laundryecommercebookingsystem.service.admin.AdminProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*AdminProductController.java
 * Admin only: shop products, prices, stock and pictures.
 */
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final AdminProductService productService;

    public AdminProductController(AdminProductService productService) {
        this.productService = productService;
    }

    /** Example: GET /api/admin/products?q=soap&status=low  (status: active, inactive or low) */
    @GetMapping
    public ResponseEntity<List<AdminProductView>> list(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) String category,
                                                       @RequestParam(required = false) String status) {
        return ResponseEntity.ok(productService.list(q, category, status));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(productService.categories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminProductView> get(@PathVariable Long id) {
        return ResponseEntity.ok(productService.get(id));
    }

    /** Body: { "name": "Washing powder 1kg", "description": "", "category": "Detergent", "price": 45.0, "inventoryQuantity": 20 } */
    @PostMapping
    public ResponseEntity<AdminProductView> create(@RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    /** Changes name, description, category and price (not stock). */
    @PutMapping("/{id}")
    public ResponseEntity<AdminProductView> update(@PathVariable Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    /** Body: { "inventoryQuantity": 40 } */
    @PatchMapping("/{id}/stock")
    public ResponseEntity<AdminProductView> stock(@PathVariable Long id, @RequestBody StockRequest request) {
        return ResponseEntity.ok(productService.setStock(id, request == null ? null : request.inventoryQuantity()));
    }

    /** Body: { "active": false } hides the product from the shop. */
    @PatchMapping("/{id}/active")
    public ResponseEntity<AdminProductView> active(@PathVariable Long id, @RequestBody ActiveRequest request) {
        if (request == null || request.active() == null) {
            throw new IllegalArgumentException("Send active: true or false");
        }
        return ResponseEntity.ok(productService.setActive(id, request.active()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Form upload with one field called "file" (JPEG, PNG or WEBP, at most 2 MB). */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminProductView> uploadImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(productService.uploadImage(id, file));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<AdminProductView> removeImage(@PathVariable Long id) {
        return ResponseEntity.ok(productService.removeImage(id));
    }
}
