package com.secke.product_service.Repository;

import com.secke.product_service.Model.Product;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
}
