package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class OrderSuccessActivity extends AppCompatActivity {
    private static final String TAG = "OrderSuccessActivity";

    private ImageView tickImage;
    private ProgressBar loadingProgress;
    private TextView statusTitle, subTitle;
    private Button okButton;

    private FirebaseAuth auth;
    private DatabaseReference ordersRef;
    private DatabaseReference tokenRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // 1. Link UI elements
        tickImage = findViewById(R.id.tickImage);
        loadingProgress = findViewById(R.id.loadingProgress);
        statusTitle = findViewById(R.id.statusTitle);
        subTitle = findViewById(R.id.statusSubtitle);
        okButton = findViewById(R.id.okButton);

        // 2. Initial State: Show only loading spinner
        tickImage.setVisibility(View.GONE);
        statusTitle.setVisibility(View.GONE);
        subTitle.setVisibility(View.GONE);
        okButton.setVisibility(View.GONE);

        // 3. Firebase Setup
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("Orders");
        tokenRef = database.getReference("OrderToken");

        // 4. Save order to database
        saveOrderWithToken();

        // 5. Success Animation Timer (3 seconds)
        new Handler().postDelayed(() -> {
            loadingProgress.setVisibility(View.GONE);

            tickImage.setVisibility(View.VISIBLE);
            tickImage.setScaleX(0f);
            tickImage.setScaleY(0f);
            tickImage.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(500)
                    .start();

            statusTitle.setVisibility(View.VISIBLE);
            statusTitle.setText("Order Placed!");

            subTitle.setVisibility(View.VISIBLE);
            subTitle.setText("Please collect it from the canteen counter.");

            okButton.setVisibility(View.VISIBLE);
        }, 3000);

        // 6. Navigation
        okButton.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomePageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void saveOrderWithToken() {
        if (auth.getCurrentUser() == null) return;

        final String uid = auth.getCurrentUser().getUid();
        final int total = getIntent().getIntExtra("total", 0);
        final String items = getIntent().getStringExtra("items") != null ? getIntent().getStringExtra("items") : "No items";
        final String instructions = getIntent().getStringExtra("instructions") != null ? getIntent().getStringExtra("instructions") : "";

        // 1. First, fetch the Real Name from the Users node
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Get name from DB, fallback to "Student" if not found
                        String realName = snapshot.child("name").getValue(String.class);
                        if (realName == null) realName = "Student";

                        final String finalCustomerName = realName;

                        // 2. Now proceed with the Token Transaction
                        tokenRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                Integer current = currentData.getValue(Integer.class);
                                if (current == null) current = 0;
                                current++;
                                currentData.setValue(current);
                                return Transaction.success(currentData);
                            }

                            @Override
                            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                                if (committed) {
                                    int newToken = snapshot.getValue(Integer.class);
                                    DatabaseReference newOrderRef = ordersRef.push();
                                    String pushId = newOrderRef.getKey();

                                    // 3. Create Model with Real Name and NO paymentStatus
                                    OrderModel order = new OrderModel(
                                            pushId != null ? pushId : "",
                                            uid,
                                            finalCustomerName, // REAL NAME APPLIED HERE
                                            items,
                                            total,
                                            newToken,
                                            instructions
                                    );

                                    // 4. Final adjustments and Save
                                    order.setStatus("Pending");

                                    newOrderRef.setValue(order).addOnSuccessListener(unused -> {
                                        Toast.makeText(OrderSuccessActivity.this, "Order # " + newToken + " Placed!", Toast.LENGTH_SHORT).show();
                                        CartManager.clearCart();
                                    });
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}