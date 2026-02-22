package com.example.canteenautomation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartUpdateListener {

    RecyclerView recyclerView;
    TextView totalText;
    Button placeOrderBtn;

    List<FoodModel> cartList;
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // 🔥 Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Cart");
        }

        toolbar.setNavigationOnClickListener(v -> finish());

        // RecyclerView
        recyclerView = findViewById(R.id.cartRecyclerView);
        totalText = findViewById(R.id.totalAmountText);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        cartList = CartManager.getCartItems();

        adapter = new CartAdapter(cartList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        calculateTotal();

        // Optional: Place Order Click
        placeOrderBtn.setOnClickListener(v -> {
            CartManager.clearCart();
            adapter.notifyDataSetChanged();
            calculateTotal();
            finish();
        });
    }

    @Override
    public void onCartUpdated() {
        calculateTotal();
    }

    private void calculateTotal() {

        int total = 0;

        for (FoodModel item : cartList) {
            total += item.price * item.quantity;
        }

        totalText.setText("Total: ₹" + total);
    }
}