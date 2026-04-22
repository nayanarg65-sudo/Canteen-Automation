package com.example.canteenautomation;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class OrderHistoryActivity extends AppCompatActivity {

    // These variables manage the list and the professional "Card" look
    private RecyclerView recyclerView;
    private OrderHistoryAdapter adapter;
    private ArrayList<OrderHistoryModel> orderList;
    private TextView emptyText;

    private DatabaseReference ordersRef;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // 1. Toolbar Setup
        MaterialToolbar toolbar = findViewById(R.id.orderhistoryToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Orders");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Initialize UI components from activity_order_history.xml
        recyclerView = findViewById(R.id.rvOrderHistory);
        emptyText = findViewById(R.id.emptyText);

        auth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("Orders");

        // 3. Setup the RecyclerView (The Container for your cards)
        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(orderList);

        // This ensures the list scrolls vertically
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 4. Load data from Firebase
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        if (auth.getCurrentUser() == null) return;

        String currentUserId = auth.getCurrentUser().getUid();

        // Query only the orders belonging to the logged-in student
        Query query = ordersRef.orderByChild("userId").equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();

                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        // Map Firebase data to our Model
                        OrderHistoryModel order = ds.getValue(OrderHistoryModel.class);
                        if (order != null) {
                            // Save the unique Firebase key as our orderId
                            //order.orderId = ds.getKey();
                            orderList.add(order);
                        }
                    }

                    // 5. SORTING: This puts the NEWEST order at the TOP (Matches your image)
                    Collections.reverse(orderList);

                    recyclerView.setVisibility(View.VISIBLE);
                    if (emptyText != null) emptyText.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    if (emptyText != null) emptyText.setVisibility(View.VISIBLE);
                }

                // 6. REFRESH: Tell the Adapter to draw the cards on screen
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderHistoryActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}