package com.example.canteenautomation;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.util.Log;

import java.util.HashMap; // Ensure this import is present if you use it for userData in other parts

import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.Button;
import android.content.Intent;

import android.view.View;

public class OrderSuccessActivity extends AppCompatActivity {
    private static final String TAG = "OrderSuccessActivity";

    ImageView tickImage;

    ProgressBar loadingProgress;
    TextView statusTitle, subTitle;

    FirebaseAuth auth;
    DatabaseReference ordersRef;
    DatabaseReference tokenRef;

    Button okButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // 1. Link all Java variables to XML IDs
        tickImage = findViewById(R.id.tickImage);
        loadingProgress = findViewById(R.id.loadingProgress);
        statusTitle = findViewById(R.id.statusTitle);
        subTitle = findViewById(R.id.statusSubtitle);
        okButton = findViewById(R.id.okButton);

        // 2. INITIAL STATE: Hide the tick, the text, and the button
        // We only want the spinning ring (loadingProgress) to show at the start
        tickImage.setVisibility(android.view.View.GONE);
        statusTitle.setVisibility(android.view.View.GONE);
        subTitle.setVisibility(android.view.View.GONE);
        okButton.setVisibility(android.view.View.GONE);

        // 3. Firebase Initialization
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("Orders");
        tokenRef = database.getReference("OrderToken");

        // 4. Run your database saving logic
        saveOrderWithToken();

        // 5. THE 3-SECOND TIMER LOGIC
        new android.os.Handler().postDelayed(() -> {
            // A. Hide the loading ring
            loadingProgress.setVisibility(android.view.View.GONE);

            // B. Show and animate the big tick mark
            tickImage.setVisibility(android.view.View.VISIBLE);
            tickImage.setScaleX(0f);
            tickImage.setScaleY(0f);
            tickImage.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(500)
                    .start();

            // C. Show the success messages
            statusTitle.setVisibility(android.view.View.VISIBLE);
            statusTitle.setText("Order Placed!");

            subTitle.setVisibility(android.view.View.VISIBLE);
            subTitle.setText("Please collect it from the canteen counter.");

            // D. Show the OK button
            okButton.setVisibility(android.view.View.VISIBLE);

        }, 3000); // 3000 milliseconds = 3 seconds

        // 6. Set the click listener to go back to the previous screen
        okButton.setOnClickListener(v -> {
            // This starts the Home Activity and clears the previous screens (like the Cart)
            Intent intent = new Intent(OrderSuccessActivity.this, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
    private void saveOrderWithToken() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in. Order cannot be placed.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final String uid = auth.getCurrentUser().getUid();
        final int total = getIntent().getIntExtra("total", 0);
        final String items = getIntent().getStringExtra("items") != null
                ? getIntent().getStringExtra("items") : "No items";

        tokenRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer current = currentData.getValue(Integer.class);
                if (current == null) {
                    current = 0;
                }
                current++;
                currentData.setValue(current);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null || !committed) {
                    Toast.makeText(OrderSuccessActivity.this, "Transaction Failed: " + (error != null ? error.getMessage() : "Unknown"), Toast.LENGTH_SHORT).show();
                    return;
                }

                // 🚩 FIND THIS SECTION AND CHANGE TO THIS:
                Integer newTokenInteger = snapshot.getValue(Integer.class);
                final int newToken = newTokenInteger;

                // 1. Initialize the model
                OrderModel order = new OrderModel(
                        items,
                        total,
                        newToken,
                        uid,
                        "Paid",
                        "Pending"
                );

                // 2. Define newOrderRef BEFORE using it so it's in scope
                DatabaseReference newOrderRef = ordersRef.push();

                // 3. Set the IDs
                order.setOrderId(String.valueOf(newToken));
                order.setTimestamp(ServerValue.TIMESTAMP);

                // 4. Now save using the reference we just created
                newOrderRef.setValue(order)
                        .addOnSuccessListener(unused -> {
                            String successMessage = "Order Placed! Your Token Number is: " + newToken;
                            Toast.makeText(OrderSuccessActivity.this, successMessage, Toast.LENGTH_LONG).show();
                            CartManager.clearCart();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrderSuccessActivity.this, "Order failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

                // --- KEY CHANGES START HERE ---

                // Set the orderId field in the OrderModel
                order.setOrderId(String.valueOf(newToken)); // Use the setter or direct assignment if public

                // Set the timestamp using ServerValue.TIMESTAMP
                order.setTimestamp(ServerValue.TIMESTAMP); // Use the setter or direct assignment if public

                newOrderRef.setValue(order)
                        .addOnSuccessListener(unused -> {
                            // ✅ ADD THIS LINE HERE
                            String successMessage = "Order Placed! Your Token Number is: " + newToken;

                            Toast.makeText(OrderSuccessActivity.this, successMessage, Toast.LENGTH_LONG).show();

                            CartManager.clearCart();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(OrderSuccessActivity.this, "Order failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                // --- KEY CHANGES END HERE ---
            }
        });
    }
}
