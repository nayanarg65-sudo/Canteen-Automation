package com.example.canteenautomation;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class TrackOrderActivity extends AppCompatActivity {

    TextView tvStatus, tvOrderId;
    View dotPending, dotReady, dotDelivered;
    View line1, line2;

    FirebaseAuth auth;
    DatabaseReference ordersDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_track_order);

        // Initialize Views
        tvStatus = findViewById(R.id.tvStatus);
        tvOrderId = findViewById(R.id.tvOrderId);
        ImageView btnBackTrack = findViewById(R.id.btnBackTrack);
        btnBackTrack.setOnClickListener(v -> finish());

        dotPending = findViewById(R.id.dotPending);
        dotReady = findViewById(R.id.dotReady);
        dotDelivered = findViewById(R.id.dotDelivered);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);

        auth = FirebaseAuth.getInstance();
        ordersDb = FirebaseDatabase.getInstance().getReference("Orders");

        findLatestOrder();
    }

    private void findLatestOrder() {
        if (auth.getCurrentUser() == null) return;
        String uid = auth.getCurrentUser().getUid();

        // 1. Find the latest order for the logged-in student
        ordersDb.orderByChild("userId").equalTo(uid).limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // 2. Get the orderId (Token) which is used as the node key
                                String token = ds.child("orderId").getValue(String.class);

                                if (token != null) {
                                    tvOrderId.setText("Order ID: " + token);
                                    listenForStatusUpdates(token);
                                }
                            }
                        } else {
                            tvStatus.setText("No active orders found");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void listenForStatusUpdates(String token) {
        // 3. IMPORTANT: Your Admin app updates db.child(order.orderId)
        // So we listen directly to that specific node name
        ordersDb.child(token).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (status != null) {
                        updateUI(status);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TrackOrder", "Error: " + error.getMessage());
            }
        });
    }

    private void updateUI(String status) {
        int activeColor = ContextCompat.getColor(this, R.color.primaryColor);
        int inactiveColor = Color.parseColor("#D3D3D3");

        tvStatus.setText("Status: " + status);

        // Step 1: Pending (matches Admin option)
        if ("Pending".equalsIgnoreCase(status) || "New".equalsIgnoreCase(status)) {
            updateColors(activeColor, inactiveColor, inactiveColor, inactiveColor, inactiveColor);
            tvStatus.setText("Order Received! ⏳");
        }
        // Step 2: Ready (matches Admin option)
        else if ("Ready".equalsIgnoreCase(status)) {
            updateColors(activeColor, activeColor, activeColor, inactiveColor, inactiveColor);
            tvStatus.setText("Food is Ready! 🍕");
        }
        // Step 3: Delivered (matches Admin option)
        else if ("Delivered".equalsIgnoreCase(status)) {
            updateColors(activeColor, activeColor, activeColor, activeColor, activeColor);
            tvStatus.setText("Order Picked Up ✅");
        }
    }

    private void updateColors(int d1, int l1, int d2, int l2, int d3) {
        dotPending.setBackgroundTintList(ColorStateList.valueOf(d1));
        line1.setBackgroundColor(l1);
        dotReady.setBackgroundTintList(ColorStateList.valueOf(d2));
        line2.setBackgroundColor(l2);
        dotDelivered.setBackgroundTintList(ColorStateList.valueOf(d3));
    }
}