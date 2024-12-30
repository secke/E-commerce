package com.secke.product_service.Service;


import com.secke.product_service.Error.BadRequestException;
import com.secke.product_service.Error.ResourceNotFoundException;
import com.secke.product_service.Feign.MediaClient;
import com.secke.product_service.Model.Media;
import com.secke.product_service.Model.Product;
import com.secke.product_service.Model.ProductEvent;
import com.secke.product_service.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.secke.product_service.kafka.Producer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;


@Service
public class ProductService {
    @Autowired
    ProductRepository productRepo;

//    @Autowired
//    UserService userService;

    @Autowired
    Producer producer;

    @Autowired
    MediaClient mediaClient;

    public List<Product> getAllProducts() {
        List<Product> products = productRepo.findAll();
        if (products != null) {
            products.forEach(product -> {
                List<Media> images = mediaClient.getImagesByProductId(product.getId());
                System.out.println("Images for product " + product.getId() + ": " + images);
                product.setImages(images);
            });
//            products.forEach(product -> {
//                product.getImages().forEach(image -> {
//                    image.setImagePath("http://localhost:4200/" + Paths.get(image.getImagePath()).getFileName());
//                });
//            });
        }
        return products;
    }

public Product addProduct(String userId, Product productDetails, MultipartFile[] files) {
    try {
        Product product = new Product();
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setQuantity(productDetails.getQuantity());
        product.setUserId(userId);

        Product savedProduct = productRepo.save(product);

        List<ProductEvent.FileData> fileDataList = new ArrayList<>();
        for (MultipartFile file : files) {
            ProductEvent.FileData fileData = new ProductEvent.FileData();
            fileData.setFileName(file.getOriginalFilename());
            fileData.setMimeType(file.getContentType());
            fileData.setContent(file.getBytes());
            fileDataList.add(fileData);
        }

        ProductEvent event = new ProductEvent();
        event.setId(savedProduct.getId());
        // event.setName(savedProduct.getName());
        // event.setDescription(savedProduct.getDescription());
        // event.setPrice(savedProduct.getPrice());
        // event.setQuantity(savedProduct.getQuantity());
        // event.setUserId(savedProduct.getUserId());
        event.setFiles(fileDataList);

        producer.sendNewProductEvent(event); //fi la envoyer il fallait save avant pour ham le vrai id 

        return savedProduct;

    } catch (IOException ex) {
        throw new RuntimeException("Error processing files: " + ex.getMessage());
    }
}


    private boolean validProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()
                || product.getPrice() == null || product.getPrice() < 0
        ){
            return false;
        }
        return true;
    }

    public Product getProductById(String id) {
        Product product = productRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("Product not found"));
        if (product != null) {
            List<Media> images = mediaClient.getImagesByProductId(product.getId());
            System.out.println("Images for product " + product.getId() + ": " + images);
            product.setImages(images);

        }
        return  product;
    }

    public void deleteProduct(String id) {
        productRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("The product to delete is not found"));
        productRepo.deleteById(id);
    }

    public Product updateProduct(String id, Product productDetails) {
        try {

            if (!validProduct(productDetails)) {
                throw new BadRequestException("The product's name and/or price are incorrect");
            }
            Product product = getProductById(id);
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());
            product.setQuantity(productDetails.getQuantity());
            return productRepo.save(product);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public List<Product> getUserProducts(String userId) {
        // System.out.println("Calliiing getProductByUserId with userId: " + userId);
        try {
            // List<Product> products = productRepo.findAll().stream()
            //     .filter(product -> product.getUserId().equals(userId))
            //     .toList();
            List<Product> products = productRepo.findByUserId(userId);

            if (products.isEmpty()) {
                throw new ResourceNotFoundException("No products found for user: " + userId);
            }

            products.forEach(product -> {
                List<Media> images = mediaClient.getImagesByProductId(product.getId());
                System.out.println("Images for product " + product.getId() + ": " + images);
                product.setImages(images);
            });

            return products;
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user products", e);
        }
    }
}
