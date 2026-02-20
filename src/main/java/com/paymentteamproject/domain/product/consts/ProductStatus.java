package com.paymentteamproject.domain.product.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus {
    ONSALE("판매중"),
    SOLDOUT("품절"),
    DISCONTINUED("단종");

    private final String description;
}
