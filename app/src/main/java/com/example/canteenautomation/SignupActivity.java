package com.example.canteenautomation;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    EditText nameEdit, rollEdit, phoneEdit, emailEdit, passwordEdit;
    Button signupButton;
    TextView backToLogin;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) getSupportActionBar().hide();
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        nameEdit = findViewById(R.id.nameEdit);
        rollEdit = findViewById(R.id.rollEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signupButton = findViewById(R.id.signupButton);
        backToLogin = findViewById(R.id.backToLogin);

        signupButton.setOnClickListener(v -> {
            String name = nameEdit.getText().toString().trim();
            String roll = rollEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            // 1. Check Empty Fields
            if (name.isEmpty() || roll.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Roll Number Validation (Exact length like U18BP23S0354 is 12 characters)
            if (roll.length() != 12) {
                rollEdit.setError("Roll number must be 12 characters!");
                rollEdit.requestFocus();
                return;
            }

            // 3. Phone Number Validation
            if (phone.length() != 10) {
                phoneEdit.setError("Enter a valid 10-digit phone number!");
                phoneEdit.requestFocus();
                return;
            }

            // --- DATABASE DUPLICATE CHECK ---
            FirebaseDatabase.getInstance().getReference("Users").get().addOnSuccessListener(snapshot -> {
                boolean isDuplicate = false;
                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                    String existingRoll = ds.child("roll").getValue(String.class);
                    String existingPhone = ds.child("phone").getValue(String.class);

                    if (roll.equalsIgnoreCase(existingRoll)) {
                        rollEdit.setError("Roll number already registered!");
                        rollEdit.requestFocus();
                        isDuplicate = true;
                        break;
                    }
                    if (phone.equals(existingPhone)) {
                        phoneEdit.setError("Phone number already in use!");
                        phoneEdit.requestFocus();
                        isDuplicate = true;
                        break;
                    }
                }

                if (!isDuplicate) {
                    // Final Validations before signup
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailEdit.setError("Please enter a valid email address!");
                        return;
                    }
                    if (password.length() < 6) {
                        passwordEdit.setError("Password must be at least 6 characters!");
                        return;
                    }

                    // Proceed to create account
                    performFirebaseSignup(name, roll, phone, email, password);
                }
            });

        });

        backToLogin.setOnClickListener(v -> finish());
    }

    private void performFirebaseSignup(String name, String roll, String phone, String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String uid = auth.getCurrentUser().getUid();
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("name", name);
                userMap.put("roll", roll);
                userMap.put("phone", phone);
                userMap.put("email", email);
                userMap.put("role", "student"); // Assigned automatically

                FirebaseDatabase.getInstance().getReference("Users").child(uid).setValue(userMap)
                        .addOnSuccessListener(unused -> {
                            // Save to SharedPreferences for the profile page
                            SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", name);
                            editor.putString("phone", phone);
                            editor.apply();

                            Toast.makeText(this, "Account Created ✅", Toast.LENGTH_SHORT).show();
                            finish();
                        });
            } else {
                // Show alert if the email is already in use by another account
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Signup Error")
                        .setMessage(task.getException().getMessage())
                        .setPositiveButton("OK", null).show();
            }
        });
    }
}