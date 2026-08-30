
package com.cput.laundryecommercebookingsystem.factory;

import com.cput.laundryecommercebookingsystem.factory.LaundryServiceFactory;
import org.junit.jupiter.api.Test;
import com.cput.laundryecommercebookingsystem.domain.LaundryService;

import static org.junit.jupiter.api.Assertions.*;
/**
 * LaundryServiceFactoryTest.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
class LaundryServiceFactoryTest {

    @Test
    void testCreateLaundryServiceSuccess() {
        LaundryService service = LaundryServiceFactory.createLaundryService("Wash & Fold", "Standard 5kg wash, dry, and fold service", 85.00);
        assertNotNull(service);
        assertNotNull(service.getId());
        assertEquals("Wash & Fold", service.getServiceName());
        assertEquals(85.00, service.getPrice());
        System.out.println("Laundry Service Created Successfully: " + service);
    }

    @Test
    void testCreateLaundryServiceWithInvalidPrice() {
        LaundryService service = LaundryServiceFactory.createLaundryService("Dry Cleaning", "Delicate suit dry cleaning", -20.00);
        assertNull(service, "Laundry Service creation should fail when price is negative");
    }

    @Test
    void testCreateLaundryServiceWithMissingName() {
        LaundryService service = LaundryServiceFactory.createLaundryService("", "Standard ironing service", 35.00);
        assertNull(service, "Laundry Service creation should fail when service name is empty");
    }

    @Test
    void testCreateLaundryServiceWithMissingDescription() {
        LaundryService service = LaundryServiceFactory.createLaundryService("Express Ironing", "", 35.00);
        assertNull(service, "Laundry Service creation should fail when description is empty");
    }
}

