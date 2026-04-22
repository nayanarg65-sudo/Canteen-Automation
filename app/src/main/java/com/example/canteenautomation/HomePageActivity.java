package com.example.canteenautomation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    CardView cardOrderNow, cardHistory;
    TextView welcomeText;
    BottomNavigationView bottomNavigation;
    Chip chipBreakfast, chipLunch, chipSnacks, chipDrinks;
    ViewPager2 viewPagerSlider;

    RecyclerView rvBreakfast, rvLunch, rvChats;
    HomeFoodAdapter breakfastAdapter, lunchAdapter, chatsAdapter;
    ArrayList<FoodModel> breakfastList = new ArrayList<>();
    ArrayList<FoodModel> lunchList = new ArrayList<>();
    ArrayList<FoodModel> chatsList = new ArrayList<>();

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ImageView toolbarProfileImage;

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
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbarProfileImage = findViewById(R.id.toolbarProfileImage);

        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        toolbarProfileImage.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));

        rvBreakfast = findViewById(R.id.rvBreakfast);
        rvLunch = findViewById(R.id.rvLunch);
        rvChats = findViewById(R.id.rvChats);

        View header = navigationView.getHeaderView(0);
        TextView editProfile = header.findViewById(R.id.editProfile);

        // Pencil click opens full Edit screen
        editProfile.setOnClickListener(v -> {
            startActivity(new Intent(HomePageActivity.this, ProfileEditActivity.class));
        });

        setupHorizontalRecyclerViews();
        fetchUserName();
        setupSlider();
        loadFoodFromFirebase();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_menu) startActivity(new Intent(this, MenuActivity.class));
            else if (id == R.id.nav_cart) startActivity(new Intent(this, CartActivity.class));
            else if (id == R.id.nav_track) startActivity(new Intent(this, TrackOrderActivity.class));
            else if (id == R.id.nav_history) startActivity(new Intent(this, OrderHistoryActivity.class));
            else if (id == R.id.nav_logout) {
                auth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        chipBreakfast.setOnClickListener(v -> openMenuWithCategory("Breakfast"));
        chipLunch.setOnClickListener(v -> openMenuWithCategory("Lunch"));
        chipSnacks.setOnClickListener(v -> openMenuWithCategory("Chats"));
        chipDrinks.setOnClickListener(v -> openMenuWithCategory("Drinks"));

        cardOrderNow.setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));
        cardHistory.setOnClickListener(v -> startActivity(new Intent(this, OrderHistoryActivity.class)));

        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_menu) { startActivity(new Intent(this, MenuActivity.class)); return true; }
            if (id == R.id.nav_cart) { startActivity(new Intent(this, CartActivity.class)); return true; }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (bottomNavigation != null) bottomNavigation.setSelectedItemId(R.id.nav_home);
        sliderHandler.postDelayed(sliderRunnable, 3000);
        fetchUserName(); // Refresh data and avatar when coming back
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void fetchUserName() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.child("name").getValue(String.class);
                        String phoneNum = snapshot.child("phone").getValue(String.class);

                        welcomeText.setText("Hello, " + (name != null ? name : "Student") + "! 👋");

                        View header = navigationView.getHeaderView(0);
                        TextView headerName = header.findViewById(R.id.username);
                        TextView headerPhone = header.findViewById(R.id.userPhone);
                        ImageView headerImage = header.findViewById(R.id.profileImage);

                        if (name != null) {
                            headerName.setText(name);
                            setHeaderAvatar(toolbarProfileImage, name);
                            setHeaderAvatar(headerImage, name);
                        }
                        if (phoneNum != null) headerPhone.setText(phoneNum);
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void setHeaderAvatar(ImageView imageView, String name) {
        if (name == null || name.isEmpty()) return;
        String letter = String.valueOf(name.charAt(0)).toUpperCase();
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.parseColor("#005C97"));
        canvas.drawCircle(50, 50, 50, p);
        p.setColor(Color.WHITE);
        p.setTextSize(50);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(letter, 50, 67, p);
        imageView.setImageBitmap(bitmap);
    }

    private void loadFoodFromFirebase() {
        foodRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                breakfastList.clear(); lunchList.clear(); chatsList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    FoodModel item = data.getValue(FoodModel.class);
                    if (item != null) {
                        item.id = data.getKey();
                        if ("Breakfast".equals(item.category)) breakfastList.add(item);
                        else if ("Lunch".equals(item.category)) lunchList.add(item);
                        else if ("Chats".equals(item.category)) chatsList.add(item);
                    }
                }
                breakfastAdapter.notifyDataSetChanged();
                lunchAdapter.notifyDataSetChanged();
                chatsAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setupSlider() {
        int[] images = {R.drawable.canteenpic, R.drawable.canteenpic};
        SliderAdapter adapter = new SliderAdapter(images);
        viewPagerSlider.setAdapter(adapter);
    }

    private Runnable sliderRunnable = () -> viewPagerSlider.setCurrentItem(viewPagerSlider.getCurrentItem() + 1);

    private void openMenuWithCategory(String category) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("SELECTED_CATEGORY", category);
        startActivity(intent);
    }
}