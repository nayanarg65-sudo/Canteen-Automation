package com.example.canteenautomation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    private static final String TAG = "PaymentActivity";
    private TextView itemsSummaryTextView;
    private TextView totalAmountTextView;
    private Button payNowButton;

    private android.widget.RadioGroup paymentMethodGroup;
    private com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog;
    private int totalAmount;
    private String orderedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Checkout.preload(getApplicationContext());

        itemsSummaryTextView = findViewById(R.id.itemsSummaryTextView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        payNowButton = findViewById(R.id.payNowButton);

        // 1. Find the RadioGroup from your activity_payment.xml
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);

        Intent intent = getIntent();
        totalAmount = intent.getIntExtra("total", 0);
        orderedItems = intent.getStringExtra("items");

        itemsSummaryTextView.setText(orderedItems);
        totalAmountTextView.setText("Total: Rs. " + totalAmount);

        // 2. Change button text based on selection
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOffline) {
                payNowButton.setText("Confirm Order");
            } else {
                payNowButton.setText("Pay Now with Razorpay");
            }
        });

        // 3. New Click Listener logic
        payNowButton.setOnClickListener(v -> {
            int selectedId = paymentMethodGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.radioOffline) {
                // Show your custom bottom sheet dialog
                showOfflineConfirmationDialog();
            } else {
                startPayment();
            }
        });
    }

    private void startPayment() {
        /*
          You need to pass the "Key ID" that you generated earlier from the Razorpay Dashboard.
          For a production application, you would generate a payment order on your server-side
          (e.g., Firebase Cloud Function) and get the order ID from there.
          The amount should be in paisa (e.g., 10000 for Rs. 100.00).
        */
        String razorpayKeyId = "rzp_test_SRmd7oRCp6ah78"; // **REPLACE THIS WITH YOUR ACTUAL TEST KEY ID**

        // Basic validation
        if (totalAmount <= 0) {
            Toast.makeText(this, "Cannot process payment for 0 or negative amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Canteen Automation"); // Your App Name
            options.put("description", "Order #" + System.currentTimeMillis()); // Order Description
            options.put("currency", "INR"); // Indian Rupee
            options.put("amount", totalAmount * 100); // Amount in paisa (e.g., Rs. 100 = 10000 paisa)
            options.put("key", razorpayKeyId); // Your Razorpay Key ID
            options.put("image", "https://your_app_logo_url.png"); // Optional: Your app logo URL

            JSONObject prefill = new JSONObject();
            prefill.put("email", "customer@example.com"); // Prefill customer email (optional)
            prefill.put("contact", "9876543210"); // Prefill customer phone (optional)
            options.put("prefill", prefill);

            Checkout checkout = new Checkout();
            checkout.setKeyID(razorpayKeyId); // Set key ID here as well, important
            checkout.open(this, options);

        } catch (Exception e) {
            Log.e(TAG, "Error in starting Razorpay Checkout", e);
            Toast.makeText(this, "Payment failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // --- Razorpay Payment Callbacks ---

    @Override
    public void onPaymentSuccess(String razorpayPaymentId) {
        // Payment was successful!
        // This `razorpayPaymentId` is a unique ID from Razorpay.
        // In a real app, you would send this ID to your server for verification.

        Toast.makeText(this, "Payment Successful! Payment ID: " + razorpayPaymentId, Toast.LENGTH_LONG).show();
        Log.d(TAG, "Payment Successful: " + razorpayPaymentId);

        // Now, proceed to your OrderSuccessActivity
        // You might want to pass the razorpayPaymentId to OrderSuccessActivity for display or record-keeping
        Intent successIntent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
        successIntent.putExtra("total", totalAmount);
        successIntent.putExtra("items", orderedItems);
        startActivity(successIntent);
        finish();
    }

    @Override
    public void onPaymentError(int code, String response) {
        String messageToShow;
        if (code == Checkout.PAYMENT_CANCELED) {
            // User intentionally cancelled the payment
            messageToShow = "Payment cancelled by you.";
            Log.d(TAG, "Payment Cancelled by User. Code: " + code + ", Response: " + response);
        } else {
            // A genuine payment error occurred
            messageToShow = "Payment Failed: " + response;
            Log.e(TAG, "Payment Error: Code: " + code + ", Response: " + response);
        }

        Toast.makeText(this, messageToShow, Toast.LENGTH_LONG).show();
        // *** CRITICAL CHANGE END ***

        finish(); // Finish PaymentActivity and return to CartActivity
    }
    private void showOfflineConfirmationDialog() {
        bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_confirm_order);

        // Link the IDs from your dialogconfirmorder.xml
        TextView txtConfirmTotal = bottomSheetDialog.findViewById(R.id.txtConfirmTotal);
        android.widget.EditText editSpecialInstructions = bottomSheetDialog.findViewById(R.id.editSpecialInstructions);
        com.google.android.material.button.MaterialButton btnConfirmPlaceOrder = bottomSheetDialog.findViewById(R.id.btnConfirmPlaceOrder);
        TextView btnCancelOrder = bottomSheetDialog.findViewById(R.id.btnCancelOrder);

        if (txtConfirmTotal != null) {
            txtConfirmTotal.setText("Total Amount: Rs. " + totalAmount);
        }

        if (btnConfirmPlaceOrder != null) {
            btnConfirmPlaceOrder.setOnClickListener(v -> {
                bottomSheetDialog.dismiss();

                // Go to your green Success Screen
                Intent successIntent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                successIntent.putExtra("total", totalAmount);
                successIntent.putExtra("items", orderedItems);
                successIntent.putExtra("mode", "OFFLINE");
                startActivity(successIntent);
                finish();
            });
        }

        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> bottomSheetDialog.dismiss());
        }

        bottomSheetDialog.show();
    }
}
