package com.example.canteenautomation;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast; // 🔹 ADDED

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class ContactUsActivity extends AppCompatActivity {

    TextView phoneText, emailText;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Or finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        // ⭐ Toolbar setup
        MaterialToolbar toolbar = findViewById(R.id.contactToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Contact Us");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // back arrow
        }

        // 🔥 ADDED: Smooth back navigation
        toolbar.setNavigationOnClickListener(v -> finish());

        // TextViews
        phoneText = findViewById(R.id.adminPhone);
        emailText = findViewById(R.id.adminEmail);

        // 📞 Call Admin
        phoneText.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:7411149342"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show();
            }
        });

        // 📧 Email Admin
        emailText.setOnClickListener(v -> {
            try {
                // "mailto:" ensures only email apps handle this intent
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:canteen@gmail.com"));

                // Optional: Add a default subject so the user doesn't have to type it
                intent.putExtra(Intent.EXTRA_SUBJECT, "Canteen Query - Student");

                // Check if there is an app to handle this before starting
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Fallback: This helps if you're on an emulator without Gmail
                    startActivity(Intent.createChooser(intent, "Send Email using:"));
                }
            } catch (Exception e) {
                Toast.makeText(this, "Could not find an email app", Toast.LENGTH_SHORT).show();
            }
        });
        }
    }


