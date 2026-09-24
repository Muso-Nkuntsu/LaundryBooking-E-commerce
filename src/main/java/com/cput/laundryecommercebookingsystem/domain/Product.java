/**
 * Product.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Represents a product available for purchase in the laundry e-commerce system.
 * Implements the Builder Pattern to enforce immutability and Spring Boot JPA for persistence.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    private Long productId;
    private String name;
    private String description;
    private double price;
    private String category;
    private int inventoryQuantity;

    /**
     * Default constructor required by JPA.
     */
    protected Product() {
    }

    /**
     * Private constructor to enforce object creation via the Builder.
     */
    private Product(Builder builder) {
        this.productId = builder.productId;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.category = builder.category;
        this.inventoryQuantity = builder.inventoryQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public int getInventoryQuantity() {
        return inventoryQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Double.compare(product.price, price) == 0 &&
                inventoryQuantity == product.inventoryQuantity &&
                Objects.equals(productId, product.productId) &&
                Objects.equals(name, product.name) &&
                Objects.equals(description, product.description) &&
                Objects.equals(category, product.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, description, price, category, inventoryQuantity);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", inventoryQuantity=" + inventoryQuantity +
                '}';
    }

    public static class Builder {

        private Long productId;
        private String name;
        private String description;
        private double price;
        private String category;
        private int inventoryQuantity;

        public Builder setProductId(Long productId) {
            this.productId = productId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setPrice(double price) {
            this.price = price;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setInventoryQuantity(int inventoryQuantity) {
            this.inventoryQuantity = inventoryQuantity;
            return this;
        }

        public Builder copy(Product product) {
            this.productId = product.productId;
            this.name = product.name;
            this.description = product.description;
            this.price = product.price;
            this.category = product.category;
            this.inventoryQuantity = product.inventoryQuantity;
            return this;
        }

        public Product build() {
            return new Product(this);
        }
    }
}
