package com.example.canteenautomation;

public class OrderHistoryModel {
    // These names must match your Firebase keys exactly!
    public String orderId;
    public String userId;
    public String items;
    public String status;
    public String customerName;
    public Object total; // Handles both whole numbers and decimals

    public long timestamp;
    // Empty constructor is MANDATORY for Firebase
    public OrderHistoryModel() {
    }

    // Constructor to help the app assign data
    public OrderHistoryModel(String orderId, String userId, String items, Object total, String status, String customerName) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.customerName = customerName;
    }
}