package com.example.canteenautomation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileEditActivity extends AppCompatActivity {

    Button homeBtn, logoutBtn, saveBtn, menuBtn, cartBtn, deleteBtn;
    TextInputEditText nameText, phoneText;
    TextInputLayout nameLayout;
    ImageView profileImage;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // 1. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2. Binding Views
        saveBtn = findViewById(R.id.saveBtn);
        nameText = findViewById(R.id.profileName);
        phoneText = findViewById(R.id.profilePhone);
        nameLayout = findViewById(R.id.profileNameLayout);
        profileImage = findViewById(R.id.profileImage);

        homeBtn = findViewById(R.id.homeBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        menuBtn = findViewById(R.id.menuBtn);
        cartBtn = findViewById(R.id.cartBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        sp = getSharedPreferences("UserData", MODE_PRIVATE);

        // 3. Fetch Initial Data
        fetchCurrentUserData();

        // 4. Pencil Icon Click Logic
        nameLayout.setEndIconOnClickListener(v -> {
            nameText.setFocusable(true);
            nameText.setFocusableInTouchMode(true);
            nameText.setCursorVisible(true);
            nameText.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(nameText, InputMethodManager.SHOW_IMPLICIT);
            }
            Toast.makeText(this, "Editing Enabled", Toast.LENGTH_SHORT).show();
        });

        // 5. Save Logic
        saveBtn.setOnClickListener(v -> {
            String newName = nameText.getText().toString().trim();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (!newName.isEmpty() && user != null) {
                FirebaseDatabase.getInstance("https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("Users").child(user.getUid()).child("name").setValue(newName)
                        .addOnSuccessListener(aVoid -> {
                            sp.edit().putString("name", newName).apply();
                            setLetterAvatar(newName);
                            nameText.setFocusable(false);
                            nameText.setFocusableInTouchMode(false);
                            Toast.makeText(this, "Updated Everywhere ✅", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Name cannot be empty ⚠️", Toast.LENGTH_SHORT).show();
            }
        });

        // 6. Navigation Listeners
        homeBtn.setOnClickListener(v -> finish());

        menuBtn.setOnClickListener(v -> startActivity(new Intent(this, MenuActivity.class)));

        cartBtn.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        logoutBtn.setOnClickListener(v -> {
            sp.edit().clear().apply();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // 7. Delete Account Logic
        deleteBtn.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Delete Account?")
                    .setMessage("This action is permanent. Are you sure?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            FirebaseDatabase.getInstance("https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/")
                                    .getReference("Users").child(uid).removeValue()
                                    .addOnCompleteListener(task -> {
                                        user.delete().addOnCompleteListener(authTask -> {
                                            if (authTask.isSuccessful()) {
                                                sp.edit().clear().apply();
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            }
                                        });
                                    });
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    } // End of onCreate

    private void fetchCurrentUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance("https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users").child(user.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String name = snapshot.child("name").getValue(String.class);
                                String phone = snapshot.child("phone").getValue(String.class);

                                nameText.setText(name);
                                phoneText.setText(phone);
                                setLetterAvatar(name);

                                sp.edit().putString("name", name).putString("phone", phone).apply();
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void setLetterAvatar(String name) {
        if (name == null || name.isEmpty()) return;
        String letter = String.valueOf(name.charAt(0)).toUpperCase();
        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.parseColor("#005C97"));
        canvas.drawCircle(100, 100, 100, p);
        p.setColor(Color.WHITE);
        p.setTextSize(100);
        p.setTypeface(Typeface.DEFAULT_BOLD);
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(letter, 100, 135, p);
        profileImage.setImageBitmap(bitmap);
    }
}