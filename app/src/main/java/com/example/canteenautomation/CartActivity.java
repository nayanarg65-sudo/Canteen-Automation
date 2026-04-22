package com.example.canteenautomation;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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

        MaterialToolbar toolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Cart");
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.cartRecyclerView);
        totalText = findViewById(R.id.totalAmountText);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);

        cartList = CartManager.getCartItems();

        if (cartList == null || cartList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new CartAdapter(cartList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        calculateTotal();

        placeOrderBtn.setOnClickListener(v -> {
            int finalTotal = 0;
            StringBuilder itemsSummary = new StringBuilder();

            for (int i = 0; i < cartList.size(); i++) {
                FoodModel item = cartList.get(i);

                // Format: Burger (Rs.150) x1
                itemsSummary.append(item.name)
                        .append(" (Rs.")
                        .append(item.price)
                        .append(") x")
                        .append(item.quantity);

                if (i < cartList.size() - 1) itemsSummary.append(", ");

                try {
                    // Clean price string for math
                    String cleanPrice = item.price.replace("₹", "").replace("Rs.", "").trim();
                    finalTotal += (Integer.parseInt(cleanPrice) * item.quantity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (finalTotal > 0) {
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("total", finalTotal); // Pass as INT
                intent.putExtra("items", itemsSummary.toString()); // Pass as STRING
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid Total Amount", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartUpdated() {
        calculateTotal();
        if (cartList.isEmpty()) finish();
    }

    private void calculateTotal() {
        int total = 0;
        if (cartList == null || cartList.isEmpty()) {
            totalText.setText("Total: Rs. 0");
            placeOrderBtn.setEnabled(false);
            return;
        }

        for (FoodModel item : cartList) {
            try {
                // Remove everything except numbers (handles ₹, Rs., spaces, etc.)
                String cleanPrice = item.price.replaceAll("[^0-9]", "");
                int priceValue = Integer.parseInt(cleanPrice);

                total += (priceValue * item.quantity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Update the UI
        totalText.setText("Total: Rs. " + total);
        placeOrderBtn.setEnabled(total > 0);
    }
}