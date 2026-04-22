package com.example.canteenautomation;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText forgotEmailEdit;
    private Button resetPasswordButton;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();

        forgotEmailEdit = findViewById(R.id.forgotEmailEdit);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(v -> sendPasswordReset());
    }

    private void sendPasswordReset() {
        String email = forgotEmailEdit.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            forgotEmailEdit.setError(getString(R.string.email_required));
            forgotEmailEdit.requestFocus();
            return;
        }

        resetPasswordButton.setEnabled(false); // Disable button to prevent multiple requests
        Toast.makeText(this, "Sending reset link...", Toast.LENGTH_SHORT).show();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    resetPasswordButton.setEnabled(true); // Re-enable button

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset link sent to " + email,
                                Toast.LENGTH_LONG).show();
                        finish(); // Go back to LoginActivity
                    } else {
                        String error = task.getException() != null
                                ? task.getException().getMessage()
                                : "Failed to send reset email";
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Error: " + error,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
