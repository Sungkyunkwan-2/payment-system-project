package com.paymentteamproject.domain.order.service;

import com.paymentteamproject.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final ProductRepository productRepository;

}
