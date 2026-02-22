package com.example.canteenautomation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText nameEdit, rollEdit, phoneEdit, emailEdit, passwordEdit;
    Button signupButton;

    FirebaseAuth auth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_signup);

        // 🔥 Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // 🔥 Initialize Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://canteenapp-61e30-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );

        databaseReference = database.getReference("Students");

        // Connect XML views
        nameEdit = findViewById(R.id.nameEdit);
        rollEdit = findViewById(R.id.rollEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {

            String name = nameEdit.getText().toString().trim();
            String roll = rollEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();

            // 1️⃣ Empty Check
            if (name.isEmpty() || roll.isEmpty() || phone.isEmpty()
                    || email.isEmpty() || password.isEmpty()) {

                Toast.makeText(SignupActivity.this,
                        "All fields are required!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // 2️⃣ Mobile Validation (10 digits only)
            if (!phone.matches("\\d{10}")) {
                phoneEdit.setError("Enter valid 10-digit mobile number");
                phoneEdit.requestFocus();
                return;
            }

            // 3️⃣ Email Validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEdit.setError("Enter valid email address");
                emailEdit.requestFocus();
                return;
            }

            // 4️⃣ Roll Number Validation
            if (!roll.matches("^U\\d{2}[A-Z]{2}\\d{2}[A-Z]\\d{4}$")) {
                rollEdit.setError("Invalid roll number length");
                rollEdit.requestFocus();
                return;
            }

            // 5️⃣ Password Validation (minimum 6 characters)
            if (password.length() < 6) {
                passwordEdit.setError("Password must be at least 6 characters");
                passwordEdit.requestFocus();
                return;
            }

            // 🔥 If all validations pass → continue signup
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            String uid = auth.getCurrentUser().getUid();

                            StudentModel student = new StudentModel(
                                    name, roll, phone, email
                            );

                            databaseReference.child(uid).setValue(student)
                                    .addOnSuccessListener(unused -> {

                                        Toast.makeText(SignupActivity.this,
                                                "Signup Successful ✅",
                                                Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(
                                                SignupActivity.this,
                                                LoginActivity.class
                                        ));
                                        finish();
                                    });

                        } else {

                            Toast.makeText(SignupActivity.this,
                                    "Signup Failed: " +
                                            task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        });

    }
}
