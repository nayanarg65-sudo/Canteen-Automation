package com.example.canteenautomation;

public class FoodModel {

    public String foodId;      // Changed from id
    public String name;
    public String price;
    public String category;
    public String imageUrl;
    public boolean isAvailable; // Changed from available (Boolean)
    public int quantity;

    // Empty constructor (Firebase requirement)
    public FoodModel() {
        this.isAvailable = true; // Default
        this.quantity = 0;
    }

    // Constructor for adding new items
    public FoodModel(String foodId, String name, String price,
                     String category, String imageUrl,
                     boolean isAvailable) {
        this.foodId = foodId;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.isAvailable = isAvailable;
        this.quantity = 0;
    }
}