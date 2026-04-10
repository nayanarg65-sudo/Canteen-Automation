package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    CardView cardOrderNow, cardHistory;
    TextView welcomeText, txtItemCount;
    LinearLayout cartLayout;
    BottomNavigationView bottomNavigation;
    Chip chipBreakfast, chipLunch, chipSnacks, chipDrinks;
    ViewPager2 viewPagerSlider;

    RecyclerView rvBreakfast, rvLunch, rvChats;
    HomeFoodAdapter breakfastAdapter, lunchAdapter, chatsAdapter;
    ArrayList<FoodModel> breakfastList = new ArrayList<>();
    ArrayList<FoodModel> lunchList = new ArrayList<>();
    ArrayList<FoodModel> chatsList = new ArrayList<>();

    FirebaseAuth auth;
    DatabaseReference userRef, foodRef;

    private Handler sliderHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_home_page);

        // Firebase Initialization
        auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/");
        userRef = database.getReference("Users");
        foodRef = database.getReference("FoodItems");

        // View Binding
        welcomeText = findViewById(R.id.welcomeText);
        cardOrderNow = findViewById(R.id.cardOrderNow);
        cardHistory = findViewById(R.id.cardHistory);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        chipBreakfast = findViewById(R.id.chipBreakfast);
        chipLunch = findViewById(R.id.chipLunch);
        chipSnacks = findViewById(R.id.chipSnacks);
        chipDrinks = findViewById(R.id.chipDrinks);
        viewPagerSlider = findViewById(R.id.viewPagerSlider);

        // Horizontal RecyclerViews
        rvBreakfast = findViewById(R.id.rvBreakfast);
        rvLunch = findViewById(R.id.rvLunch);
        rvChats = findViewById(R.id.rvChats);

        setupHorizontalRecyclerViews();
        fetchUserName();
        setupSlider();
        loadFoodFromFirebase();

        // Listeners
        chipBreakfast.setOnClickListener(v -> openMenuWithCategory("Breakfast"));
        chipLunch.setOnClickListener(v -> openMenuWithCategory("Lunch"));
        chipSnacks.setOnClickListener(v -> openMenuWithCategory("Chats"));
        chipDrinks.setOnClickListener(v -> openMenuWithCategory("Drinks"));

        cardOrderNow.setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));
        cardHistory.setOnClickListener(v -> Toast.makeText(this, "Opening Orders...", Toast.LENGTH_SHORT).show());

        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_menu) {
                startActivity(new Intent(this, MenuActivity.class));
                return true;
            }
            if (id == R.id.nav_cart) {
                startActivity(new Intent(this, CartActivity.class));
                return true;
            }
            return false;
        });
    }

    private void setupHorizontalRecyclerViews() {
        rvBreakfast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvLunch.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvChats.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        breakfastAdapter = new HomeFoodAdapter(breakfastList);
        lunchAdapter = new HomeFoodAdapter(lunchList);
        chatsAdapter = new HomeFoodAdapter(chatsList);

        rvBreakfast.setAdapter(breakfastAdapter);
        rvLunch.setAdapter(lunchAdapter);
        rvChats.setAdapter(chatsAdapter);
    }

    private void loadFoodFromFirebase() {
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                breakfastList.clear();
                lunchList.clear();
                chatsList.clear();

                // FIXED: Changed children() to getChildren()
                for (DataSnapshot data : snapshot.getChildren()) {
                    FoodModel item = data.getValue(FoodModel.class);
                    if (item != null) {
                        item.id = data.getKey();

                        // Categorize based on your Firebase "category" field
                        if ("Breakfast".equals(item.category)) {
                            breakfastList.add(item);
                        } else if ("Lunch".equals(item.category)) {
                            lunchList.add(item);
                        } else if ("Chats".equals(item.category)) {
                            chatsList.add(item);
                        }
                    }
                }

                // Refresh the horizontal scrolling lists
                breakfastAdapter.notifyDataSetChanged();
                lunchAdapter.notifyDataSetChanged();
                chatsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomePageActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        welcomeText.setText("Hello, " + (name != null ? name : "Student") + "! 👋");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setupSlider() {
        int[] images = {R.drawable.canteenpic, R.drawable.canteenpic};
        SliderAdapter adapter = new SliderAdapter(images);
        viewPagerSlider.setAdapter(adapter);
        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private Runnable sliderRunnable = () -> viewPagerSlider.setCurrentItem(viewPagerSlider.getCurrentItem() + 1);

    private void openMenuWithCategory(String category) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("SELECTED_CATEGORY", category);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(breakfastAdapter != null) breakfastAdapter.notifyDataSetChanged();
        if(lunchAdapter != null) lunchAdapter.notifyDataSetChanged();
        if(chatsAdapter != null) chatsAdapter.notifyDataSetChanged();
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}