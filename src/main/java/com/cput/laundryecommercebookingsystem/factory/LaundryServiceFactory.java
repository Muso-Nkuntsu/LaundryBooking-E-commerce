package com.cput.laundryecommercebookingsystem.factory;
/*
 * LaundryServiceFactory.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import java.util.UUID;

/**
 * Factory class for creating LaundryService instances with validation.
 */
public class LaundryServiceFactory {

    public static LaundryService createLaundryService(String serviceName, String description, double price) {
        if (serviceName == null || serviceName.isBlank() ||
            description == null || description.isBlank() ||
            price < 0) {
            return null;
        }

        String id = UUID.randomUUID().toString();

        return new LaundryService.Builder()
                .setId(id)
                .setServiceName(serviceName)
                .setDescription(description)
                .setPrice(price)
                .build();
    }
}

