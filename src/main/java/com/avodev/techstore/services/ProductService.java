package com.avodev.techstore.services;


import com.avodev.techstore.entities.Brand;
import com.avodev.techstore.entities.Category;
import com.avodev.techstore.entities.Product;
import com.avodev.techstore.exceptions.AppException;
import com.avodev.techstore.exceptions.ErrorCode;
import com.avodev.techstore.mappers.ProductMapper;
import com.avodev.techstore.repositories.BrandRepository;
import com.avodev.techstore.repositories.CategoryRepository;
import com.avodev.techstore.repositories.ProductRepository;
import com.avodev.techstore.requests.ProductRequest;
import com.avodev.techstore.responses.ProductResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService {
    ProductRepository productRepository;
    CategoryRepository categoryRepository;
    BrandRepository brandRepository;
    ProductMapper productMapper;


    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED, Map.of("id", id)));
        return productMapper.toResponse(product);
    }

    public ProductResponse createProduct(ProductRequest req) {
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXISTED));

        Product product = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .category(category)
                .brand(brand)
                .build();

        return productMapper.toResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED, Map.of("id", id)));
        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new AppException(ErrorCode.BRAND_NOT_EXISTED));

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setCategory(category);
        product.setBrand(brand);

        return productMapper.toResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED, Map.of("id", id));
        }
        productRepository.deleteById(id);
    }


}
