package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private TextView totalAmountText;
    private Button placeOrderBtn, btnOkGotIt;
    private ArrayList<FoodModel> cartList;
    private CartAdapter adapter;
    private LinearLayout mainCartLayout, loadingLayout, successLayout, emptyCartLayout;
    private double finalTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // View Initialization
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalAmountText = findViewById(R.id.totalAmountText);
        placeOrderBtn = findViewById(R.id.placeOrderBtn);
        btnOkGotIt = findViewById(R.id.btnOkGotIt);
        mainCartLayout = findViewById(R.id.mainCartLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        successLayout = findViewById(R.id.successLayout);
        emptyCartLayout = findViewById(R.id.emptyCartLayout); // Added this line

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.cartToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        cartList = CartManager.getCartItems();
        if (cartList == null) cartList = new ArrayList<>();

        adapter = new CartAdapter(cartList, this::calculateTotal);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(adapter);

        calculateTotal();

        placeOrderBtn.setOnClickListener(v -> {
            if (cartList != null && !cartList.isEmpty()) {
                showConfirmBottomSheet();
            } else {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
            }
        });

        btnOkGotIt.setOnClickListener(v -> {
            CartManager.clearCart();
            Intent intent = new Intent(CartActivity.this, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showConfirmBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_order, null);

        TextView txtTotal = view.findViewById(R.id.txtConfirmTotal);
        EditText editInstructions = view.findViewById(R.id.editSpecialInstructions);
        Button btnConfirm = view.findViewById(R.id.btnConfirmPlaceOrder);
        TextView btnCancel = view.findViewById(R.id.btnCancelOrder);

        txtTotal.setText("Total Amount: ₹" + finalTotal);

        btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            saveOrder(editInstructions.getText().toString());
        });

        btnCancel.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }

    private void saveOrder(String instructions) {
        mainCartLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(View.VISIBLE);

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Orders");
        String orderId = orderRef.push().getKey();
        String userId = FirebaseAuth.getInstance().getUid();

        StringBuilder itemsSummary = new StringBuilder();
        for (FoodModel item : cartList) {
            itemsSummary.append(item.name).append(" (x").append(item.quantity).append("), ");
        }

        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("orderId", orderId);
        orderMap.put("userId", userId);
        orderMap.put("items", itemsSummary.toString());
        orderMap.put("total", finalTotal);
        orderMap.put("instructions", instructions);
        orderMap.put("status", "Pending");
        orderMap.put("timestamp", System.currentTimeMillis());

        new Handler().postDelayed(() -> {
            if (orderId != null) {
                orderRef.child(orderId).setValue(orderMap).addOnSuccessListener(unused -> {
                    loadingLayout.setVisibility(View.GONE);
                    successLayout.setVisibility(View.VISIBLE);
                }).addOnFailureListener(e -> {
                    loadingLayout.setVisibility(View.GONE);
                    mainCartLayout.setVisibility(View.VISIBLE);
                    Toast.makeText(CartActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }, 3000);
    }

    public void calculateTotal() {
        finalTotal = 0;

        if (cartList == null || cartList.isEmpty()) {
            // Show the empty state and hide cart contents
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.bottomSection).setVisibility(View.GONE);
        } else {
            // Hide the empty state and show cart contents
            if (emptyCartLayout != null) emptyCartLayout.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.bottomSection).setVisibility(View.VISIBLE);

            for (FoodModel item : cartList) {
                try {
                    String cleanPrice = item.price.replace("₹", "").replace(",", "").trim();
                    double priceValue = Double.parseDouble(cleanPrice);
                    finalTotal += (priceValue * item.quantity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        totalAmountText.setText("Total Amount: ₹" + finalTotal);
    }
}