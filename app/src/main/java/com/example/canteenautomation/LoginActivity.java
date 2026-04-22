package com.example.canteenautomation;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText emailEdit, passwordEdit;
    Button loginButton;
    TextView goToSignup, forgotPasswordBtn;

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance(
                "https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/"
        ).getReference("Users");

        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginButton = findViewById(R.id.loginButton);
        goToSignup = findViewById(R.id.goToSignup);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);

        loginButton.setOnClickListener(v -> {
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Step 1: Check Internet before trying to login
            if (isConnected()) {
                performLogin(email, password);
            } else {
                // Step 2: Show the same alert and go back to splash
                showNoInternetAlert();
            }
        });

        goToSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        forgotPasswordBtn.setOnClickListener(v -> {
            // This starts your friend's activity
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }


    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showNoInternetAlert() {
        new AlertDialog.Builder(this)
                .setTitle("No Internet Access")
                .setMessage("BMS Bites requires an internet connection. Redirecting to splash...")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    // Take user back to SplashActivity and clear the stack
                    Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void performLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = auth.getCurrentUser().getUid();
                databaseReference.child(uid).child("password").setValue(password);
                databaseReference.child(uid).get().addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.child("role").getValue(String.class);
                        if ("admin".equals(role)) {
                            startActivity(new Intent(this, AdminActivity.class));
                        } else {
                            startActivity(new Intent(this, HomePageActivity.class));
                        }
                        finish();
                    }
                });
            } else {
                // Handle actual login errors (wrong password, etc.) while online
                Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}