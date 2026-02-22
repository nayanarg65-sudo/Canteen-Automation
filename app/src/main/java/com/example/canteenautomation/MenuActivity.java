package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FoodAdapter adapter;
    List<FoodModel> foodList;

    Button btnBreakfast, btnLunch, btnChats;

    LinearLayout viewCartLayout;
    TextView cartItemCountText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // ==============================
        // TOOLBAR
        // ==============================
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Menu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // ==============================
        // RECYCLER VIEW
        // ==============================
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ==============================
        // BUTTONS
        // ==============================
        btnBreakfast = findViewById(R.id.btnBreakfast);
        btnLunch = findViewById(R.id.btnLunch);
        btnChats = findViewById(R.id.btnChats);

        btnBreakfast.setOnClickListener(v -> loadBreakfast());
        btnLunch.setOnClickListener(v -> loadLunch());
        btnChats.setOnClickListener(v -> loadChats());

        // ==============================
        // VIEW CART
        // ==============================
        viewCartLayout = findViewById(R.id.viewCartLayout);
        cartItemCountText = findViewById(R.id.cartItemCountText);

        viewCartLayout.setOnClickListener(v ->
                startActivity(new Intent(this, CartActivity.class))
        );

        // ==============================
        // 🔥 RECEIVE CATEGORY FROM HOME
        // ==============================
        String category = getIntent().getStringExtra("category");

        if (category != null) {
            switch (category) {
                case "breakfast":
                    loadBreakfast();
                    break;
                case "lunch":
                    loadLunch();
                    break;
                case "chats":
                    loadChats();
                    break;
                default:
                    loadBreakfast();
            }
        } else {
            loadBreakfast(); // default
        }
    }

    private void updateCartBar() {

        List<FoodModel> cartItems = CartManager.getCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            viewCartLayout.setVisibility(View.GONE);
            return;
        }

        int itemCount = 0;

        for (FoodModel item : cartItems) {
            itemCount += item.quantity;
        }

        cartItemCountText.setText(itemCount + " ITEMS");
        viewCartLayout.setVisibility(View.VISIBLE);
    }

    private void loadBreakfast() {
        foodList = new ArrayList<>();
        foodList.add(new FoodModel("Idli", 30, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Dosa", 50, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Poori", 40, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Upma", 35, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Vada", 25, android.R.drawable.ic_menu_gallery));

        adapter = new FoodAdapter(foodList, this::updateCartBar);
        recyclerView.setAdapter(adapter);
        updateCartBar();
    }

    private void loadLunch() {
        foodList = new ArrayList<>();
        foodList.add(new FoodModel("Veg Meals", 90, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Fried Rice", 80, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Curd Rice", 60, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Biryani", 120, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Chapathi Combo", 70, android.R.drawable.ic_menu_gallery));

        adapter = new FoodAdapter(foodList, this::updateCartBar);
        recyclerView.setAdapter(adapter);
        updateCartBar();
    }

    private void loadChats() {
        foodList = new ArrayList<>();
        foodList.add(new FoodModel("Pani Puri", 30, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Bhel Puri", 35, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Samosa", 20, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Kachori", 25, android.R.drawable.ic_menu_gallery));
        foodList.add(new FoodModel("Dahi Puri", 40, android.R.drawable.ic_menu_gallery));

        adapter = new FoodAdapter(foodList, this::updateCartBar);
        recyclerView.setAdapter(adapter);
        updateCartBar();
    }
}