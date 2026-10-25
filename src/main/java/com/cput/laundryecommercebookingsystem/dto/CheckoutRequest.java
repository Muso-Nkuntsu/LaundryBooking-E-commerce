package com.cput.laundryecommercebookingsystem.dto;

import java.util.List;

/**
 * Body of POST /api/orders/checkout: the student's cart.
 * Only product IDs and quantities are sent; prices are always looked up on the server.
 *
 * { "studentId": 1, "items": [ { "productId": 3, "quantity": 2 } ] }
 */
public record CheckoutRequest(Long studentId, List<Item> items) {

    public record Item(Long productId, int quantity) {
    }
}
