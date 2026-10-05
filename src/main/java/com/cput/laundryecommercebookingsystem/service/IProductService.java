package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.Product;

/**
 * Service contract for products sold in the laundry shop.
 * Uses the same create / read / update / delete / getAll shape as ILaundryService.
 */
public interface IProductService extends IService<Product, Long> {
}
