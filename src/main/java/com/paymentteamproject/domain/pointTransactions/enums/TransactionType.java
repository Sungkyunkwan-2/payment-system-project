package com.paymentteamproject.domain.pointTransactions.enums;

public enum TransactionType {
    USED, //구매 시 포인트 사용
    RECOVERED, //구매 취소로 인한 포인트 복구
    ADDED, //포인트 적립
    CANCELLED,//구매 취소로 인한 적립 포인트 취소
    EXPIRED //포인트 만료
}
