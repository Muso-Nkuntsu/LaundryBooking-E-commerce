package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.service.admin.ProductImageStorage;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/api/public/product-images")
public class ProductImageController {

    private final ProductImageStorage storage;

    public ProductImageController(ProductImageStorage storage) {
        this.storage = storage;
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> get(@PathVariable String filename) throws IOException {
        Path path = storage.find(filename).orElseThrow(() -> new NoSuchElementException("Image not found."));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ProductImageStorage.contentTypeFor(filename)))
                .header("X-Content-Type-Options", "nosniff")
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(Files.readAllBytes(path));
    }
}
