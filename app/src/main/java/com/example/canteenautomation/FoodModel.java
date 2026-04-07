package com.example.canteenautomation;

public class FoodModel {

    public String id;
    public String name;
    public String price;
    public String category;
    public String imageUrl;
    public boolean available;
    public int quantity;

    // Empty constructor (Firebase requirement)
    public FoodModel() {
        this.available = true;
        this.quantity = 0;
    }

    // ✅ Constructor that matches HomePageActivity.java exactly
    public FoodModel(String id, String name, String price,
                     String category, String imageUrl,
                     boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
        this.quantity = 0; // default
    }

    // Optional constructor if you want to include quantity
    public FoodModel(String id, String name, String price,
                     String category, String imageUrl,
                     boolean available, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.available = available;
        this.quantity = quantity;
    }
}

