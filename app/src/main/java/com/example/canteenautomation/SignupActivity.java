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

            // 3. Phone Number Validation (10 digits)
            if (phone.length() != 10) {
                phoneEdit.setError("Enter a valid 10-digit phone number!");
                phoneEdit.requestFocus();
                return;
            }

            // 4. Email Validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEdit.setError("Please enter a valid email address!");
                emailEdit.requestFocus();
                return;
            }

            // 5. Password Validation
            if (password.length() < 6) {
                passwordEdit.setError("Password must be at least 6 characters!");
                passwordEdit.requestFocus();
                return;
            }

            // Firebase Signup
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();
                    HashMap<String, Object> userMap = new HashMap<>();
                    userMap.put("name", name);
                    userMap.put("roll", roll);
                    userMap.put("phone", phone);
                    userMap.put("email", email);
                    userMap.put("role", "student");

                    FirebaseDatabase.getInstance().getReference("Users").child(uid).setValue(userMap)
                            .addOnSuccessListener(unused -> {

                                // --- ADD THESE 4 LINES ---
                                SharedPreferences sp = getSharedPreferences("UserData", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("name", name);  // Saves the name locally
                                editor.putString("phone", phone); // Saves the phone locally
                                editor.apply();
                                // -------------------------

                                Toast.makeText(this, "Account Created ✅", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                } else {
                    Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}