/**
 * ProductFactory.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.domain.Product;
import java.util.UUID;

/**
 * Factory class for creating Product instances with validation.
 */
public class ProductFactory {

    public static Product createProduct(String name, String description, double price, String category, int inventoryQuantity) {
        if (name == null || name.isBlank() ||
            description == null || description.isBlank() ||
            category == null || category.isBlank() ||
            price < 0 || inventoryQuantity < 0) {
            return null;
        }


        return new Product.Builder()
                .setName(name)
                .setDescription(description)
                .setPrice(price)
                .setCategory(category)
                .setInventoryQuantity(inventoryQuantity)
                .build();
    }
}

