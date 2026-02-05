package com.paymentteamproject.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    ELECTRONICS("전자제품"),
    FOOD("음식");

    private final String description;
}
