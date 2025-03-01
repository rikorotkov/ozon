package com.floda.home.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Order {

    private String orderId;
    private String totalAmount;
    private String productId;
    private String productName;
    private String productImageUrl;
    private int quantity;

}
