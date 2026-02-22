package com.example.canteenautomation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CartManager {

    private static final List<FoodModel> cartList = new ArrayList<>();

    public static void updateCart(FoodModel food) {

        boolean found = false;

        for (FoodModel item : cartList) {
            if (item.name.equals(food.name)) {
                if (food.quantity == 0) {
                    cartList.remove(item);
                } else {
                    item.quantity = food.quantity;
                }
                found = true;
                break;
            }
        }

        if (!found && food.quantity > 0) {
            cartList.add(food);
        }
    }

    public static List<FoodModel> getCartItems() {
        return cartList;
    }

    public static void clearCart() {
        cartList.clear();
    }
}