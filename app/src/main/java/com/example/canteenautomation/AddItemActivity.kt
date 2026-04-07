package com.example.canteenautomation

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class AddItemActivity : AppCompatActivity() {

    private lateinit var etItemName: EditText
    private lateinit var etPrice: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var spCategory: Spinner
    private lateinit var btnAddItem: Button

    private val categories = arrayOf("Breakfast", "Lunch", "Chats", "Drinks")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)

        // ✅ Back button logic
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }

        etItemName = findViewById(R.id.etItemName)
        etPrice = findViewById(R.id.etPrice)
        etImageUrl = findViewById(R.id.etImageUrl)
        spCategory = findViewById(R.id.spCategory)
        btnAddItem = findViewById(R.id.btnAddItem)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter

        btnAddItem.setOnClickListener {
            val name = etItemName.text.toString().trim()
            val price = etPrice.text.toString().trim()
            val imageUrl = etImageUrl.text.toString().trim()
            val category = spCategory.selectedItem.toString()

            if (name.isEmpty() || price.isEmpty() || imageUrl.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance().getReference("FoodItems")
            val itemId = database.push().key ?: ""

            val foodItem = FoodModel(
                itemId,
                name,
                price,
                category,
                imageUrl,
                true
            )

            database.child(itemId).setValue(foodItem).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show()
                    etItemName.text.clear()
                    etPrice.text.clear()
                    etImageUrl.text.clear()
                } else {
                    Toast.makeText(this, "Failed to add item: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
