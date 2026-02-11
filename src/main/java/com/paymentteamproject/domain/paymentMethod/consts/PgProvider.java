package com.paymentteamproject.domain.paymentMethod.consts;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PgProvider {
    TOSS_PAYMENTS ("TOSS_PAYMENTS"),
    NHN_KCP ("NHN_KCP"),
    KG_INICIS ("KG_INICIS"),
    NICE_PAYMENTS ("NICE_PAYMENTS"),
    DANAL ("DANAL"),
    KG_MOBILIANS ("KG_MOBILIANS");

    private final String description;
}

