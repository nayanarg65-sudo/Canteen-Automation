package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    BottomNavigationView bottomNavigation;

    RecyclerView popularRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        popularRecycler = findViewById(R.id.popularRecycler);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        toolbar,
                        R.string.app_name,
                        R.string.app_name
                );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 🔥 CATEGORY CARD CLICKS
        findViewById(R.id.breakfastCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("category", "breakfast");
            startActivity(intent);
        });

        findViewById(R.id.lunchCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("category", "lunch");
            startActivity(intent);
        });

        findViewById(R.id.chatsCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            intent.putExtra("category", "chats");
            startActivity(intent);
        });

        // Bottom Navigation
        bottomNavigation.setOnItemSelectedListener(item -> {

            if (item.getItemId() == R.id.nav_home) return true;

            if (item.getItemId() == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                return true;
            }

            if (item.getItemId() == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }

            return false;
        });

        setupPopularSection();
    }

    private void setupPopularSection() {

        popularRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<FoodModel> popularList = new ArrayList<>();
        popularList.add(new FoodModel("Biryani", 120, android.R.drawable.ic_menu_gallery));
        popularList.add(new FoodModel("Dosa", 50, android.R.drawable.ic_menu_gallery));
        popularList.add(new FoodModel("Fried Rice", 80, android.R.drawable.ic_menu_gallery));
        popularList.add(new FoodModel("Pani Puri", 30, android.R.drawable.ic_menu_gallery));

        popularRecycler.setAdapter(new HomeFoodAdapter(popularList));
    }
}