package com.example.canteenautomation;

import java.util.ArrayList;
import java.util.Iterator;

public class CartManager {

    private static final ArrayList<FoodModel> cartItems = new ArrayList<>();

    // ✅ ADDED: This allows the Adapter to show the correct -/+ numbers
    public static int getItemQuantity(String itemId) {
        for (FoodModel food : cartItems) {
            if (food.id.equals(itemId)) {
                return food.quantity;
            }
        }
        return 0;
    }

    public static void addItem(FoodModel item) {
        for (FoodModel food : cartItems) {
            if (food.id.equals(item.id)) {
                food.quantity = item.quantity; // Sync quantity
                return;
            }
        }
        cartItems.add(item);
    }

    public static void removeItem(FoodModel item) {
        Iterator<FoodModel> iterator = cartItems.iterator();
        while (iterator.hasNext()) {
            FoodModel food = iterator.next();
            if (food.id.equals(item.id)) {
                if (item.quantity <= 0) {
                    iterator.remove();
                } else {
                    food.quantity = item.quantity; // Sync the reduced quantity
                }
                return;
            }
        }
    }

    public static ArrayList<FoodModel> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
        // Reset quantities of all items in menu if possible
    }
}
