/**
 * ProductFactoryTest.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.factory.ProductFactory;
import org.junit.jupiter.api.Test;
import com.cput.laundryecommercebookingsystem.domain.Product;

import static org.junit.jupiter.api.Assertions.*;

class ProductFactoryTest {

    @Test
    void testCreateProductSuccess() {
        Product product = ProductFactory.createProduct("Washing Powder", "500g Detergent", 45.00, "Supplies", 100);
        assertNotNull(product);
        assertNotNull(product.getProductId());
        assertEquals("Washing Powder", product.getName());
        assertEquals(45.00, product.getPrice());
        System.out.println("Product Created Successfully: " + product);
    }

    @Test
    void testCreateProductWithInvalidPrice() {
        Product product = ProductFactory.createProduct("Fabric Softener", "1L Softener", -10.00, "Supplies", 50);
        assertNull(product, "Product creation should fail when price is negative");
    }

    @Test
    void testCreateProductWithMissingName() {
        Product product = ProductFactory.createProduct("", "1L Softener", 30.00, "Supplies", 50);
        assertNull(product, "Product creation should fail when name is empty");
    }
}

