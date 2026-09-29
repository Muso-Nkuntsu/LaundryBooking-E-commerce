package com.cput.laundryecommercebookingsystem.domain;

/*
 * LaundryService.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "laundry_services")
public class LaundryService {

    @Id
    private String id;
    private String serviceName;
    private String description;
    private double price;

    /**
     * Default constructor required by JPA.
     */
    protected LaundryService() {
    }

    /**
     * Private constructor to enforce object creation via the Builder.
     */
    private LaundryService(Builder builder) {
        this.id = builder.id;
        this.serviceName = builder.serviceName;
        this.description = builder.description;
        this.price = builder.price;
    }

    public String getId() {
        return id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaundryService that = (LaundryService) o;
        return Double.compare(that.price, price) == 0 &&
               Objects.equals(id, that.id) &&
               Objects.equals(serviceName, that.serviceName) &&
               Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceName, description, price);
    }

    @Override
    public String toString() {
        return "LaundryService{" +
                "id='" + id + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                '}';
    }

    public static class Builder {

        private String id;
        private String serviceName;
        private String description;
        private double price;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
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

        public Builder copy(LaundryService service) {
            this.id = service.id;
            this.serviceName = service.serviceName;
            this.description = service.description;
            this.price = service.price;
            return this;
        }

        public LaundryService build() {
            return new LaundryService(this);
        }
    }
}
