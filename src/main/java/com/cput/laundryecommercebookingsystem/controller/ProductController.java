package com.cput.laundryecommercebookingsystem.controller;

import com.cput.laundryecommercebookingsystem.controller.dto.admin.AdminDtos.ProductRow;
import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
/**
 * Talent Nocuze
 * 230405886
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    private final IProductRepository products;

    public ProductController(IProductRepository products) {
        this.products = products;
    }

    @GetMapping("/getall")
    public List<ProductRow> getAll() {
        return products.findAll().stream().filter(Product::isActive).map(ProductRow::from).toList();
    }

    @GetMapping("/read/{id}")
    public ProductRow read(@PathVariable Long id) {
        return products.findById(id).filter(Product::isActive).map(ProductRow::from)
                .orElseThrow(() -> new NoSuchElementException("Product not found."));
    }
}
