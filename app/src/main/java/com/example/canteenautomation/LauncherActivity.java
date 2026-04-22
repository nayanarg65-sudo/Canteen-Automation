package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User has logged in before → go directly to Home
            startActivity(new Intent(this, HomePageActivity.class));
        } else {
            // User has never logged in → show Login screen
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}
