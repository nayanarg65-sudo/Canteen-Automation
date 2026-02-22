package com.example.canteenautomation;

public class FoodModel {

    String name;
    int price;
    int image;
    int quantity = 0;

    public FoodModel(String name, int price, int image) {
        this.name = name;
        this.price = price;
        this.image = image;
    }
}
