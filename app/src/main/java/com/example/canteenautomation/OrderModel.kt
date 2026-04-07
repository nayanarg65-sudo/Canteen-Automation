package com.example.canteenautomation

// This matches your existing Firebase structure (items: String, total: Int)
data class OrderModel(
    var orderId: String = "",
    val items: String = "",
    val total: Int = 0,
    val status: String = "Pending",
    val customerName: String = "Student" // Added for better UI appearance
)
