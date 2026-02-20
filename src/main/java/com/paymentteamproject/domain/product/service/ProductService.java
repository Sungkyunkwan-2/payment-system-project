package com.paymentteamproject.domain.product.service;

import com.paymentteamproject.domain.product.dto.GetProductResponse;
import com.paymentteamproject.domain.product.entity.Product;
import com.paymentteamproject.domain.product.exception.ProductNotFoundException;
import com.paymentteamproject.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<GetProductResponse> getAllProducts() {
        List<Product> productList = productRepository.findAll();
        return productList.stream()
                .map(p -> new GetProductResponse(
                        p.getId(),
                        p.getName(),
                        p.getPrice(),
                        p.getStock(),
                        p.getContent(),
                        p.getStatus(),
                        p.getCategory(),
                        p.getCreatedAt()
                ))
                .toList();
    }
    @Transactional(readOnly=true)
    public GetProductResponse getProduct(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductNotFoundException("상품이 존재하지 않습니다.")
        );
        return new GetProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getContent(),
                product.getStatus(),
                product.getCategory(),
                product.getCreatedAt()
        );
    }
}
