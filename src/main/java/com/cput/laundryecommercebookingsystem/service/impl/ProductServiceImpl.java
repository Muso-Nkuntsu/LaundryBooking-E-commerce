package com.cput.laundryecommercebookingsystem.service.impl;

import com.cput.laundryecommercebookingsystem.domain.Product;
import com.cput.laundryecommercebookingsystem.factory.ProductFactory;
import com.cput.laundryecommercebookingsystem.repository.IProductRepository;
import com.cput.laundryecommercebookingsystem.service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    private final IProductRepository productRepository;

    public ProductServiceImpl(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Returns null when the product details are invalid. */
    @Override
    @Transactional
    public Product create(Product product) {
        if (product == null) {
            return null;
        }

        Product validated = ProductFactory.createProduct(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getInventoryQuantity());

        return validated == null ? null : productRepository.save(validated);
    }

    @Override
    @Transactional(readOnly = true)
    public Product read(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /** Returns null when the product does not exist. */
    @Override
    @Transactional
    public Product update(Product product) {
        if (product == null || product.getProductId() == null
                || !productRepository.existsById(product.getProductId())) {
            return null;
        }
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAll() {
        return productRepository.findAll();
    }
}
