package com.sang3jeom.order_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DirectOrderRequest {
    private Integer goodsId;
    private int quantity;
    private long price;
    private String memo; // optional
    private String address; // 배송지 주소
} 