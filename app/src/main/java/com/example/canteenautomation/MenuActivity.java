package com.example.canteenautomation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity implements FoodAdapter.CartUpdateListener {

    RecyclerView recyclerView;
    FoodAdapter adapter;
    ArrayList<FoodModel> allFoodList;
    ArrayList<FoodModel> foodList;
    SearchView searchView;
    LinearLayout cartLayout;
    TextView txtItemCount, txtNoInternet;
    ProgressBar progressBar;
    DatabaseReference database;
    BottomNavigationView bottomNavigation;
    Button btnBreakfast, btnLunch, btnChats ,btnDrinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Menu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(Color.WHITE);
            }
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerViewMenu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressBar = findViewById(R.id.progressBar);
        txtNoInternet = findViewById(R.id.txtNoInternet);

        foodList = new ArrayList<>();
        allFoodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, this);
        recyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { filterSearch(query); return false; }
            @Override
            public boolean onQueryTextChange(String newText) { filterSearch(newText); return false; }
        });

        btnBreakfast = findViewById(R.id.btnBreakfast);
        btnLunch = findViewById(R.id.btnLunch);
        btnChats = findViewById(R.id.btnChats);
        btnDrinks = findViewById(R.id.btnDrinks);

        btnBreakfast.setOnClickListener(v -> loadCategory("Breakfast"));
        btnLunch.setOnClickListener(v -> loadCategory("Lunch"));
        btnChats.setOnClickListener(v -> loadCategory("Chats"));
        btnDrinks.setOnClickListener(v -> loadCategory("Drinks"));

        cartLayout = findViewById(R.id.cartLayout);
        txtItemCount = findViewById(R.id.txtItemCount);
        cartLayout.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, CartActivity.class)));

        database = FirebaseDatabase.getInstance().getReference("FoodItems");

        checkNetworkAndLoad();

        bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setSelectedItemId(R.id.nav_menu);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) { startActivity(new Intent(this, HomePageActivity.class)); return true; }
            if (id == R.id.nav_menu) return true;
            if (id == R.id.nav_cart) { startActivity(new Intent(this, CartActivity.class)); return true; }
            return false;
        });
    }

    private void checkNetworkAndLoad() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            txtNoInternet.setVisibility(View.GONE);
            loadMenuFromFirebase();
        } else {
            txtNoInternet.setVisibility(View.VISIBLE);
            Toast.makeText(this, "No internet connection!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
        updateCartBar();
    }

    private void filterSearch(String text) {
        ArrayList<FoodModel> filteredList = new ArrayList<>();
        for (FoodModel item : allFoodList) {
            if (item.name.toLowerCase().contains(text.toLowerCase())) filteredList.add(item);
        }
        foodList.clear();
        foodList.addAll(filteredList);
        adapter.notifyDataSetChanged();
    }

    private void loadMenuFromFirebase() {
        progressBar.setVisibility(View.VISIBLE);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                allFoodList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FoodModel item = data.getValue(FoodModel.class);
                    if (item != null) {
                        item.id = data.getKey();
                        allFoodList.add(item);
                    }
                }

                String categoryFromHome = getIntent().getStringExtra("SELECTED_CATEGORY");
                if (categoryFromHome != null) {
                    loadCategory(categoryFromHome);
                } else {
                    loadCategory("Breakfast");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MenuActivity.this, "Database Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategory(String category) {
        foodList.clear();
        for (FoodModel item : allFoodList) {
            if (item.category != null && item.category.equals(category)) foodList.add(item);
        }
        updateButtonUI(category);
        adapter.notifyDataSetChanged();
        updateCartBar();
    }

    private void updateButtonUI(String selected) {
        // Fetching colors from colors.xml
        int selectedBg = ContextCompat.getColor(this, R.color.primaryColor);
        int unselectedBg = ContextCompat.getColor(this, R.color.backgroundColor);
        int white = ContextCompat.getColor(this, R.color.white);

        btnBreakfast.setBackgroundColor(selected.equals("Breakfast") ? selectedBg : unselectedBg);
        btnBreakfast.setTextColor(selected.equals("Breakfast") ? white : selectedBg);

        btnLunch.setBackgroundColor(selected.equals("Lunch") ? selectedBg : unselectedBg);
        btnLunch.setTextColor(selected.equals("Lunch") ? white : selectedBg);

        btnChats.setBackgroundColor(selected.equals("Chats") ? selectedBg : unselectedBg);
        btnChats.setTextColor(selected.equals("Chats") ? white : selectedBg);

        btnDrinks.setBackgroundColor(selected.equals("Drinks") ? selectedBg : unselectedBg);
        btnDrinks.setTextColor(selected.equals("Drinks") ? white : selectedBg);
    }

    @Override
    public void onCartUpdated() { updateCartBar(); }

    private void updateCartBar() {
        int totalItems = 0;
        if (CartManager.getCartItems() != null) {
            for (FoodModel item : CartManager.getCartItems()) {
                totalItems += item.quantity;
            }
        }

        if (totalItems > 0) {
            cartLayout.setVisibility(View.VISIBLE);
            txtItemCount.setText(totalItems + (totalItems == 1 ? " Item" : " Items"));
            // Pulling primaryDark from colors.xml
            cartLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.primaryDark));
        } else {
            cartLayout.setVisibility(View.GONE);
        }
    }
}