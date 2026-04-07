package com.example.canteenautomation

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val btnAddItem = findViewById<Button>(R.id.btnAddItem)
        val btnManageFood = findViewById<Button>(R.id.btnManageFood)
        val btnViewOrders = findViewById<Button>(R.id.btnViewOrders)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // ✅ Open Add Item Screen
        btnAddItem.setOnClickListener {
            startActivity(Intent(this, AddItemActivity::class.java))
        }

        // ✅ Manage Inventory
        btnManageFood.setOnClickListener {
            startActivity(Intent(this, InventoryManagerActivity::class.java))
        }

        // ✅ Open Orders Screen
        btnViewOrders.setOnClickListener {
            startActivity(Intent(this, AdminOrdersActivity::class.java))
        }

        // ✅ Logout Logic with Alert Confirmation (UX Improvement)
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Logout")
        builder.setMessage("Are you sure you want to sign out of the Admin panel?")

        // Visual Design: Using a theme-appropriate icon if you have one
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton("Logout") { _, _ ->
            // Perform Logout
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
            finish()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}
